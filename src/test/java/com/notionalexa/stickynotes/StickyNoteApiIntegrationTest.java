package com.notionalexa.stickynotes;

import com.notionalexa.stickynotes.api.StickyNoteController;
import com.notionalexa.stickynotes.domain.StickyNote;
import com.notionalexa.stickynotes.repository.StickyNoteRepository;
import com.notionalexa.stickynotes.service.StickyNoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Sticky Notes API.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StickyNoteApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StickyNoteRepository stickyNoteRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        stickyNoteRepository.deleteAll();
        baseUrl = "http://localhost:" + port + "/api/stickynotes";
    }

    @Test
    void testCreateStickyNote() {
        StickyNote note = new StickyNote("Test Title", "Test Content");

        ResponseEntity<StickyNote> response = restTemplate.postForEntity(
                baseUrl, note, StickyNote.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals("Test Title", response.getBody().getTitle());
        assertEquals("Test Content", response.getBody().getContent());
        assertFalse(response.getBody().isReadByAlexa());
    }

    @Test
    void testGetAllStickyNotes() {
        stickyNoteRepository.save(new StickyNote("Note 1", "Content 1"));
        stickyNoteRepository.save(new StickyNote("Note 2", "Content 2"));

        ResponseEntity<StickyNote[]> response = restTemplate.getForEntity(
                baseUrl, StickyNote[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    void testGetStickyNoteById() {
        StickyNote savedNote = stickyNoteRepository.save(
                new StickyNote("Find Me", "Content to find"));

        ResponseEntity<StickyNote> response = restTemplate.getForEntity(
                baseUrl + "/" + savedNote.getId(), StickyNote.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Find Me", response.getBody().getTitle());
    }

    @Test
    void testGetStickyNoteByIdNotFound() {
        ResponseEntity<StickyNote> response = restTemplate.getForEntity(
                baseUrl + "/99999", StickyNote.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateStickyNote() {
        StickyNote savedNote = stickyNoteRepository.save(
                new StickyNote("Original Title", "Original Content"));

        StickyNote updateData = new StickyNote("Updated Title", "Updated Content");

        ResponseEntity<StickyNote> response = restTemplate.exchange(
                baseUrl + "/" + savedNote.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(updateData),
                StickyNote.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Title", response.getBody().getTitle());
        assertEquals("Updated Content", response.getBody().getContent());
    }

    @Test
    void testDeleteStickyNote() {
        StickyNote savedNote = stickyNoteRepository.save(
                new StickyNote("Delete Me", "Content to delete"));

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + savedNote.getId(),
                HttpMethod.DELETE,
                null,
                Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(stickyNoteRepository.existsById(savedNote.getId()));
    }

    @Test
    void testGetUnreadNotes() {
        StickyNote readNote = new StickyNote("Read Note", "Already read");
        readNote.setReadByAlexa(true);
        stickyNoteRepository.save(readNote);

        StickyNote unreadNote = new StickyNote("Unread Note", "Not read yet");
        unreadNote.setReadByAlexa(false);
        stickyNoteRepository.save(unreadNote);

        ResponseEntity<StickyNote[]> response = restTemplate.getForEntity(
                baseUrl + "/unread", StickyNote[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals("Unread Note", response.getBody()[0].getTitle());
    }

    @Test
    void testMarkAsRead() {
        StickyNote savedNote = stickyNoteRepository.save(
                new StickyNote("Mark Me", "Mark me as read"));

        ResponseEntity<StickyNote> response = restTemplate.exchange(
                baseUrl + "/" + savedNote.getId() + "/read",
                HttpMethod.PUT,
                null,
                StickyNote.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isReadByAlexa());
    }

    @Test
    void testSearchNotes() {
        stickyNoteRepository.save(new StickyNote("Shopping List", "Buy milk and bread"));
        stickyNoteRepository.save(new StickyNote("Meeting Notes", "Discuss project timeline"));

        ResponseEntity<StickyNote[]> response = restTemplate.getForEntity(
                baseUrl + "/search?keyword=shopping", StickyNote[].class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals("Shopping List", response.getBody()[0].getTitle());
    }
}
