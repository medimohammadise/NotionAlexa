package com.notionalexa.stickynotes.api;

import com.notionalexa.stickynotes.domain.StickyNote;
import com.notionalexa.stickynotes.service.StickyNoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * REST API controller for managing Sticky Notes.
 * Provides endpoints for Alexa to read, add, and update sticky notes.
 */
@RestController
@RequestMapping("/api/stickynotes")
public class StickyNoteController {

    private final StickyNoteService stickyNoteService;

    public StickyNoteController(StickyNoteService stickyNoteService) {
        this.stickyNoteService = stickyNoteService;
    }

    /**
     * Retrieves all sticky notes.
     * GET /api/stickynotes
     *
     * @return list of all sticky notes
     */
    @GetMapping
    public ResponseEntity<List<StickyNote>> getAllNotes() {
        List<StickyNote> notes = stickyNoteService.getAllNotes();
        return ResponseEntity.ok(notes);
    }

    /**
     * Retrieves a sticky note by ID.
     * GET /api/stickynotes/{id}
     *
     * @param id the sticky note ID
     * @return the sticky note or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<StickyNote> getNoteById(@PathVariable Long id) {
        return stickyNoteService.getNoteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new sticky note.
     * POST /api/stickynotes
     *
     * @param stickyNote the sticky note to create
     * @return the created sticky note with 201 status
     */
    @PostMapping
    public ResponseEntity<StickyNote> createNote(@Valid @RequestBody StickyNote stickyNote) {
        StickyNote createdNote = stickyNoteService.createNote(stickyNote);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }

    /**
     * Updates an existing sticky note.
     * PUT /api/stickynotes/{id}
     *
     * @param id          the sticky note ID
     * @param stickyNote  the updated sticky note data
     * @return the updated sticky note or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<StickyNote> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody StickyNote stickyNote) {
        return stickyNoteService.updateNote(id, stickyNote)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deletes a sticky note.
     * DELETE /api/stickynotes/{id}
     *
     * @param id the sticky note ID
     * @return 204 No Content if deleted, 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        if (stickyNoteService.deleteNote(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Retrieves all unread sticky notes (not yet read by Alexa).
     * GET /api/stickynotes/unread
     *
     * @return list of unread sticky notes
     */
    @GetMapping("/unread")
    public ResponseEntity<List<StickyNote>> getUnreadNotes() {
        List<StickyNote> unreadNotes = stickyNoteService.getUnreadNotes();
        return ResponseEntity.ok(unreadNotes);
    }

    /**
     * Marks a sticky note as read by Alexa.
     * PUT /api/stickynotes/{id}/read
     *
     * @param id the sticky note ID
     * @return the updated sticky note or 404 if not found
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<StickyNote> markAsRead(@PathVariable Long id) {
        return stickyNoteService.markAsRead(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Searches sticky notes by keyword.
     * GET /api/stickynotes/search?keyword=...
     *
     * @param keyword the search keyword
     * @return list of matching sticky notes
     */
    @GetMapping("/search")
    public ResponseEntity<List<StickyNote>> searchNotes(@RequestParam String keyword) {
        List<StickyNote> results = stickyNoteService.searchNotes(keyword);
        return ResponseEntity.ok(results);
    }
}
