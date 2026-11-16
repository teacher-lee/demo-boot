package com.oraclejava.demoboot.notes;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Sort;
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
		return repository.findAll(Sort.by("id"));
	}

	@GetMapping("/{id}")
	public Note get(@PathVariable Long id) {
		return repository.findById(id).orElseThrow(() -> notFound(id));
	}

	@PostMapping
	public ResponseEntity<Note> create(@RequestBody NoteRequest request) {
		validate(request);
		Note saved = repository.save(new Note(request.title(), request.content()));
		return ResponseEntity.created(URI.create("/api/notes/" + saved.getId())).body(saved);
	}

	@PutMapping("/{id}")
	public Note update(@PathVariable Long id, @RequestBody NoteRequest request) {
		validate(request);
		Note existing = repository.findById(id).orElseThrow(() -> notFound(id));
		existing.update(request.title(), request.content());
		return repository.save(existing);
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
		String title = request.title() != null ? request.title() : existing.getTitle();
		String content = request.content() != null ? request.content() : existing.getContent();
		existing.update(title, content);
		return repository.save(existing);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		if (!repository.existsById(id)) {
			throw notFound(id);
		}
		repository.deleteById(id);
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
