package com.oraclejava.demoboot.notes;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

	private final NoteRepository repository;

	public NoteController(NoteRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public List<Note> list() {
		return repository.findAll();
	}

	@GetMapping("/{id}")
	public Note get(@PathVariable Long id) {
		return repository.findById(id).orElseThrow(() -> notFound(id));
	}

	@PostMapping
	public ResponseEntity<Note> create(@RequestBody NoteRequest request) {
		validate(request);
		Instant now = Instant.now();
		Note saved = repository.save(new Note(null, request.title(), request.content(), now, now));
		return ResponseEntity.created(URI.create("/api/notes/" + saved.id())).body(saved);
	}

	@PutMapping("/{id}")
	public Note update(@PathVariable Long id, @RequestBody NoteRequest request) {
		validate(request);
		Note existing = repository.findById(id).orElseThrow(() -> notFound(id));
		return repository.save(new Note(id, request.title(), request.content(), existing.createdAt(), Instant.now()));
	}

	/**
	 * Partial update: only fields present (non-null) in the request are changed.
	 */
	@PatchMapping("/{id}")
	public Note patch(@PathVariable Long id, @RequestBody NoteRequest request) {
		Note existing = repository.findById(id).orElseThrow(() -> notFound(id));
		if (request == null) {
			return existing;
		}
		if (request.title() != null && request.title().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
		}
		String title = request.title() != null ? request.title() : existing.title();
		String content = request.content() != null ? request.content() : existing.content();
		return repository.save(new Note(id, title, content, existing.createdAt(), Instant.now()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		if (!repository.deleteById(id)) {
			throw notFound(id);
		}
		return ResponseEntity.noContent().build();
	}

	private static void validate(NoteRequest request) {
		if (request == null || request.title() == null || request.title().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title must not be blank");
		}
	}

	private static ResponseStatusException notFound(Long id) {
		return new ResponseStatusException(HttpStatus.NOT_FOUND, "Note " + id + " not found");
	}

}
