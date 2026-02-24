package com.notionalexa.stickynotes.repository;

import com.notionalexa.stickynotes.domain.StickyNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for managing StickyNote entities.
 */
@Repository
public interface StickyNoteRepository extends JpaRepository<StickyNote, Long> {

    /**
     * Finds all sticky notes that have not been read by Alexa.
     *
     * @return list of unread sticky notes
     */
    List<StickyNote> findByIsReadByAlexaFalse();

    /**
     * Finds all sticky notes ordered by creation date descending.
     *
     * @return list of sticky notes ordered by newest first
     */
    List<StickyNote> findAllByOrderByCreatedAtDesc();

    /**
     * Finds sticky notes containing the given keyword in title or content.
     *
     * @param titleKeyword   keyword to search in title
     * @param contentKeyword keyword to search in content
     * @return list of matching sticky notes
     */
    List<StickyNote> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String titleKeyword, String contentKeyword);
}
