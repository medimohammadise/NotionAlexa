package com.notionalexa.stickynotes.service;

import com.notionalexa.stickynotes.domain.StickyNote;
import com.notionalexa.stickynotes.repository.StickyNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing Sticky Notes operations.
 * Provides business logic for creating, reading, updating, and deleting sticky notes.
 */
@Service
@Transactional
public class StickyNoteService {

    private final StickyNoteRepository stickyNoteRepository;

    public StickyNoteService(StickyNoteRepository stickyNoteRepository) {
        this.stickyNoteRepository = stickyNoteRepository;
    }

    /**
     * Retrieves all sticky notes ordered by creation date (newest first).
     *
     * @return list of all sticky notes
     */
    @Transactional(readOnly = true)
    public List<StickyNote> getAllNotes() {
        return stickyNoteRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Retrieves a sticky note by its ID.
     *
     * @param id the ID of the sticky note
     * @return optional containing the sticky note if found
     */
    @Transactional(readOnly = true)
    public Optional<StickyNote> getNoteById(Long id) {
        return stickyNoteRepository.findById(id);
    }

    /**
     * Creates a new sticky note.
     *
     * @param stickyNote the sticky note to create
     * @return the created sticky note
     */
    public StickyNote createNote(StickyNote stickyNote) {
        stickyNote.setCreatedAt(LocalDateTime.now());
        stickyNote.setUpdatedAt(LocalDateTime.now());
        stickyNote.setReadByAlexa(false);
        return stickyNoteRepository.save(stickyNote);
    }

    /**
     * Updates an existing sticky note.
     *
     * @param id          the ID of the sticky note to update
     * @param updatedNote the updated sticky note data
     * @return optional containing the updated sticky note if found
     */
    public Optional<StickyNote> updateNote(Long id, StickyNote updatedNote) {
        return stickyNoteRepository.findById(id)
                .map(existingNote -> {
                    existingNote.setTitle(updatedNote.getTitle());
                    existingNote.setContent(updatedNote.getContent());
                    existingNote.setUpdatedAt(LocalDateTime.now());
                    return stickyNoteRepository.save(existingNote);
                });
    }

    /**
     * Deletes a sticky note by its ID.
     *
     * @param id the ID of the sticky note to delete
     * @return true if the note was deleted, false if not found
     */
    public boolean deleteNote(Long id) {
        if (stickyNoteRepository.existsById(id)) {
            stickyNoteRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Retrieves all sticky notes that have not been read by Alexa.
     *
     * @return list of unread sticky notes
     */
    @Transactional(readOnly = true)
    public List<StickyNote> getUnreadNotes() {
        return stickyNoteRepository.findByIsReadByAlexaFalse();
    }

    /**
     * Marks a sticky note as read by Alexa.
     *
     * @param id the ID of the sticky note
     * @return optional containing the marked sticky note if found
     */
    public Optional<StickyNote> markAsRead(Long id) {
        return stickyNoteRepository.findById(id)
                .map(note -> {
                    note.setReadByAlexa(true);
                    note.setUpdatedAt(LocalDateTime.now());
                    return stickyNoteRepository.save(note);
                });
    }

    /**
     * Searches sticky notes by keyword in title or content.
     *
     * @param keyword the keyword to search for
     * @return list of matching sticky notes
     */
    @Transactional(readOnly = true)
    public List<StickyNote> searchNotes(String keyword) {
        return stickyNoteRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                keyword, keyword);
    }
}
