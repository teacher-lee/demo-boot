package com.oraclejava.demoboot.notes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

/**
 * In-memory store for notes. Data is lost when the application restarts.
 */
@Repository
public class NoteRepository {

	private final Map<Long, Note> store = new ConcurrentHashMap<>();
	private final AtomicLong sequence = new AtomicLong();

	public List<Note> findAll() {
		return store.values().stream()
				.sorted(Comparator.comparing(Note::id))
				.toList();
	}

	public Optional<Note> findById(Long id) {
		return Optional.ofNullable(store.get(id));
	}

	public Note save(Note note) {
		Note toSave = note.id() == null
				? new Note(sequence.incrementAndGet(), note.title(), note.content(), note.createdAt(), note.updatedAt())
				: note;
		store.put(toSave.id(), toSave);
		return toSave;
	}

	public boolean deleteById(Long id) {
		return store.remove(id) != null;
	}

}
