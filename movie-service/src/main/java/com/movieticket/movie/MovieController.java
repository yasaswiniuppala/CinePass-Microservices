package com.movieticket.movie;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final MovieRepository repo;

    public MovieController(MovieRepository r) {
        repo = r;
    }

    @PostMapping
    public ResponseEntity<Movie> create(@Valid @RequestBody Movie m) {
        m.setTitle(m.getTitle().trim());
        return ResponseEntity.status(201).body(repo.save(m));
    }

    @GetMapping
    public List<Movie> all() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Movie one(@PathVariable("id") Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Movie not found: " + id));
    }

    @PutMapping("/{id}")
    public Movie update(
            @PathVariable("id") Long id,
            @Valid @RequestBody Movie input) {

        Movie m = one(id);
        m.setTitle(input.getTitle());
        m.setGenre(input.getGenre());
        m.setDurationMinutes(input.getDurationMinutes());
        m.setLanguage(input.getLanguage());
        m.setDescription(input.getDescription());

        return repo.save(m);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        repo.delete(one(id));
        return ResponseEntity.noContent().build();
    }
}