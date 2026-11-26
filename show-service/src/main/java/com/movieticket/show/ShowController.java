package com.movieticket.show;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowRepository shows;
    private final SeatRepository seats;

    public ShowController(ShowRepository s, SeatRepository r) {
        shows = s;
        seats = r;
    }

    public record ShowRequest(
            @NotNull Long movieId,
            @NotBlank String theatre,
            @NotBlank String screenName,
            @NotNull LocalDateTime startTime,
            @Min(1) @Max(500) int seatCapacity
    ) {}

    // 1. Create a new show
    @PostMapping
    @Transactional
    public ResponseEntity<MovieShow> create(
            @Valid @RequestBody ShowRequest b
    ) {

        if (b.startTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Show start time must be in the future"
            );
        }

        MovieShow s = new MovieShow();

        s.setMovieId(b.movieId());
        s.setTheatre(b.theatre());
        s.setScreenName(b.screenName());
        s.setStartTime(b.startTime());
        s.setSeatCapacity(b.seatCapacity());

        s = shows.save(s);

        // Automatically create seats for the show
        for (int i = 1; i <= s.getSeatCapacity(); i++) {
            seats.save(
                    new Seat(s.getId(), String.format("S%02d", i))
            );
        }

        return ResponseEntity.status(201).body(s);
    }

    // 2. Get all shows
    @GetMapping
    public List<MovieShow> all() {
        return shows.findAll();
    }

    // 3. Get a show by ID
    @GetMapping("/{id}")
    public MovieShow one(@PathVariable("id") Long id) {

        return shows.findById(id)
                .orElseThrow(() ->
                        new NoSuchElementException("Show not found: " + id)
                );
    }

    // 4. Update a show
    @PutMapping("/{id}")
    public MovieShow update(
            @PathVariable("id") Long id,
            @Valid @RequestBody ShowRequest b
    ) {

        MovieShow s = one(id);

        if (seats.findByShowIdOrderBySeatNumber(id)
                .stream()
                .anyMatch(Seat::isReserved)) {

            throw new IllegalArgumentException(
                    "Cannot edit a show after seats have been reserved"
            );
        }

        s.setMovieId(b.movieId());
        s.setTheatre(b.theatre());
        s.setScreenName(b.screenName());
        s.setStartTime(b.startTime());

        if (b.seatCapacity() != s.getSeatCapacity()) {
            throw new IllegalArgumentException(
                    "Seat capacity cannot change after show creation"
            );
        }

        return shows.save(s);
    }

    // 5. Delete a show
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id
    ) {

        one(id);

        if (seats.findByShowIdOrderBySeatNumber(id)
                .stream()
                .anyMatch(Seat::isReserved)) {

            throw new IllegalArgumentException(
                    "Cannot delete a show with reserved seats"
            );
        }

        seats.deleteByShowId(id);
        shows.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // 6. View all seats for a show
    @GetMapping("/{id}/seats")
    public List<Seat> listSeats(
            @PathVariable("id") Long id
    ) {

        one(id);

        return seats.findByShowIdOrderBySeatNumber(id);
    }

    // 7. Reserve a seat
    @PostMapping("/{showId}/seats/{seatNumber}/reserve")
    @Transactional
    public ResponseEntity<?> reserve(
            @PathVariable("showId") Long showId,
            @PathVariable("seatNumber") String seatNumber
    ) {

        one(showId);

        Seat s = seats.lockSeat(showId, seatNumber)
                .orElseThrow(() ->
                        new NoSuchElementException("Seat not found")
                );

        if (s.isReserved()) {
            return ResponseEntity.status(409)
                    .body(Map.of("error", "Seat already reserved"));
        }

        s.setReserved(true);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Seat reserved",
                        "showId", showId,
                        "seatNumber", seatNumber
                )
        );
    }

    // 8. Release a reserved seat
    @PostMapping("/{showId}/seats/{seatNumber}/release")
    @Transactional
    public ResponseEntity<?> release(
            @PathVariable("showId") Long showId,
            @PathVariable("seatNumber") String seatNumber
    ) {

        Seat s = seats.lockSeat(showId, seatNumber)
                .orElseThrow(() ->
                        new NoSuchElementException("Seat not found")
                );

        s.setReserved(false);

        return ResponseEntity.ok(
                Map.of("message", "Seat released")
        );
    }
}