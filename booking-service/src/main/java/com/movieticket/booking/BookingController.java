package com.movieticket.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository repo;
    private final RestClient rest;

    public BookingController(
            BookingRepository r,
            RestClient.Builder builder
    ) {
        repo = r;
        rest = builder.build();
    }

    public record BookingRequest(
            @NotNull Long showId,
            @NotBlank String seatNumber
    ) {}

    // 1. Create a booking
    @PostMapping
    public ResponseEntity<?> book(
            @Valid @RequestBody BookingRequest req,
            @RequestHeader("X-Authenticated-User") String email
    ) {

        String seat = req.seatNumber()
                .trim()
                .toUpperCase(Locale.ROOT);

        try {
            // Reserve the seat through Show Service
            rest.post()
                    .uri(
                            "http://show-service/api/shows/{id}/seats/{seat}/reserve",
                            req.showId(),
                            seat
                    )
                    .retrieve()
                    .toBodilessEntity();

            try {
                Booking saved = repo.saveAndFlush(
                        new Booking(req.showId(), seat, email)
                );

                return ResponseEntity.status(201).body(saved);

            } catch (RuntimeException saveFailure) {

                // Release the seat if booking creation fails
                try {
                    rest.post()
                            .uri(
                                    "http://show-service/api/shows/{id}/seats/{seat}/release",
                                    req.showId(),
                                    seat
                            )
                            .retrieve()
                            .toBodilessEntity();

                } catch (Exception ignored) {
                }

                throw saveFailure;
            }

        } catch (org.springframework.web.client.HttpClientErrorException.Conflict e) {

            return ResponseEntity.status(409)
                    .body(Map.of("error", "Seat is already booked"));

        } catch (DataIntegrityViolationException e) {

            return ResponseEntity.status(409)
                    .body(Map.of("error", "Seat already has a booking"));

        } catch (Exception e) {

            return ResponseEntity.status(502)
                    .body(
                            Map.of(
                                    "error", "Booking could not be completed",
                                    "detail",
                                    Optional.ofNullable(e.getMessage())
                                            .orElse("downstream service error")
                            )
                    );
        }
    }

    // 2. Get all bookings belonging to the logged-in user
    @GetMapping
    public List<Booking> mine(
            @RequestHeader("X-Authenticated-User") String email
    ) {
        return repo.findByEmailOrderByBookedAtDesc(email);
    }

    // 3. Get a booking by ID
    @GetMapping("/{id}")
    public Booking one(
            @PathVariable("id") Long id,
            @RequestHeader("X-Authenticated-User") String email
    ) {

        return repo.findByIdAndEmail(id, email)
                .orElseThrow(
                        () -> new NoSuchElementException("Booking not found")
                );
    }

    // 4. Cancel a booking
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancel(
            @PathVariable("id") Long id,
            @RequestHeader("X-Authenticated-User") String email
    ) {

        Booking b = repo.findByIdAndEmail(id, email)
                .orElseThrow(
                        () -> new NoSuchElementException("Booking not found")
                );

        if (!"CONFIRMED".equals(b.getStatus())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Booking is not active"));
        }

        try {
            // Release the reserved seat
            rest.post()
                    .uri(
                            "http://show-service/api/shows/{showId}/seats/{seat}/release",
                            b.getShowId(),
                            b.getSeatNumber()
                    )
                    .retrieve()
                    .toBodilessEntity();

            b.setStatus("CANCELLED");

            repo.save(b);

            return ResponseEntity.ok(b);

        } catch (Exception e) {

            return ResponseEntity.status(502)
                    .body(Map.of("error", "Could not release seat"));
        }
    }
}