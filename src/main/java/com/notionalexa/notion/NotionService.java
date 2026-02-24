package com.notionalexa.notion;

import com.notionalexa.notion.model.NotionNote;
import notion.api.v1.NotionClient;
import notion.api.v1.http.JavaNetHttpClient;
import notion.api.v1.model.common.PropertyType;
import notion.api.v1.model.databases.QueryResults;
import notion.api.v1.model.databases.query.sort.QuerySort;
import notion.api.v1.model.databases.query.sort.QuerySortDirection;
import notion.api.v1.model.databases.query.sort.QuerySortTimestamp;
import notion.api.v1.model.blocks.Block;
import notion.api.v1.model.blocks.BlockType;
import notion.api.v1.model.blocks.Blocks;
import notion.api.v1.model.pages.Page;
import notion.api.v1.model.pages.PageProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotionService implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(NotionService.class);

    private final NotionClient notionClient;
    private final NotionConfig notionConfig;

    public NotionService(NotionConfig notionConfig) {
        this.notionConfig = notionConfig;
        String apiKey = notionConfig.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Notion API key is not configured — API calls will fail");
            apiKey = "";
        }
        this.notionClient = new NotionClient(apiKey);
        this.notionClient.setHttpClient(new JavaNetHttpClient());
    }

    public List<NotionNote> getRecentNotes() {
        if (notionConfig.getDatabaseId() == null || notionConfig.getDatabaseId().isBlank()) {
            log.warn("Notion database ID is not configured");
            return Collections.emptyList();
        }

        try {
            QuerySort sort = new QuerySort();
            sort.setTimestamp(QuerySortTimestamp.LastEditedTime);
            sort.setDirection(QuerySortDirection.Descending);

            QueryResults results = notionClient.queryDatabase(
                    notionConfig.getDatabaseId(),
                    null,
                    List.of(sort),
                    null,
                    notionConfig.getMaxResults());

            return parseNotes(results.getResults());
        } catch (Exception e) {
            log.error("Failed to fetch notes from Notion API", e);
            return Collections.emptyList();
        }
    }

    private List<NotionNote> parseNotes(List<Page> pages) {
        if (pages == null) {
            return Collections.emptyList();
        }
        List<NotionNote> notes = new ArrayList<>();
        for (Page page : pages) {
            NotionNote note = toNotionNote(page);
            if (note != null) {
                notes.add(note);
            }
        }
        return notes;
    }

    private NotionNote toNotionNote(Page page) {
        try {
            String id = page.getId();
            String title = extractTitle(page);
            String content = extractProperties(page);
            String body = getPageBody(id);
            return new NotionNote(id, title, content, body, page.getCreatedTime(), page.getLastEditedTime());
        } catch (Exception e) {
            log.debug("Could not convert page to NotionNote", e);
            return null;
        }
    }

    /**
     * Fetches all block children for a page and extracts plain text for Alexa to read.
     * Handles paragraphs, headings, lists, to-dos, quotes, and code blocks.
     * Follows pagination automatically.
     */
    String getPageBody(String pageId) {
        if (pageId == null || pageId.isBlank()) {
            return "";
        }
        try {
            StringBuilder sb = new StringBuilder();
            String cursor = null;
            do {
                Blocks blocks = notionClient.retrieveBlockChildren(pageId, cursor, 100);
                for (Block block : blocks.getResults()) {
                    String text = extractBlockText(block);
                    if (!text.isBlank()) {
                        sb.append(text).append(" ");
                    }
                }
                cursor = Boolean.TRUE.equals(blocks.getHasMore()) ? blocks.getNextCursor() : null;
            } while (cursor != null);
            return sb.toString().trim();
        } catch (Exception e) {
            log.warn("Could not fetch page blocks for {}", pageId, e);
            return "";
        }
    }

    private String extractBlockText(Block block) {
        if (block == null || block.getType() == null) {
            return "";
        }
        BlockType type = block.getType();
        if (BlockType.Paragraph.equals(type)) {
            var el = block.asParagraph().getParagraph();
            return el != null ? richTextToPlain(el.getRichText()) : "";
        } else if (BlockType.HeadingOne.equals(type)) {
            var el = block.asHeadingOne().getHeading1();
            return el != null ? richTextToPlain(el.getRichText()) : "";
        } else if (BlockType.HeadingTwo.equals(type)) {
            var el = block.asHeadingTwo().getHeading2();
            return el != null ? richTextToPlain(el.getRichText()) : "";
        } else if (BlockType.HeadingThree.equals(type)) {
            var el = block.asHeadingThree().getHeading3();
            return el != null ? richTextToPlain(el.getRichText()) : "";
        } else if (BlockType.BulletedListItem.equals(type)) {
            var el = block.asBulletedListItem().getBulletedListItem();
            return el != null ? richTextToPlain(el.getRichText()) : "";
        } else if (BlockType.NumberedListItem.equals(type)) {
            var el = block.asNumberedListItem().getNumberedListItem();
            return el != null ? richTextToPlain(el.getRichText()) : "";
        } else if (BlockType.ToDo.equals(type)) {
            var el = block.asToDo().getToDo();
            if (el == null) return "";
            String checkboxPrefix = el.getChecked() ? "Done: " : "To do: ";
            return checkboxPrefix + richTextToPlain(el.getRichText());
        } else if (BlockType.Quote.equals(type)) {
            var el = block.asQuote().getQuote();
            return el != null ? richTextToPlain(el.getRichText()) : "";
        } else if (BlockType.Code.equals(type)) {
            var el = block.asCode().getCode();
            return el != null ? richTextToPlain(el.getRichText()) : "";
        } else if (BlockType.Callout.equals(type)) {
            var el = block.asCallout().getCallout();
            return el != null ? richTextToPlain(el.getRichText()) : "";
        }
        return "";
    }

    private String richTextToPlain(List<PageProperty.RichText> richText) {
        if (richText == null || richText.isEmpty()) {
            return "";
        }
        return richText.stream()
                .map(PageProperty.RichText::getPlainText)
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.joining(" "))
                .trim();
    }

    private String extractTitle(Page page) {
        Map<String, PageProperty> properties = page.getProperties();
        if (properties == null) {
            return "Untitled";
        }
        for (PageProperty prop : properties.values()) {
            if (PropertyType.Title.equals(prop.getType())) {
                List<PageProperty.RichText> titleList = prop.getTitle();
                if (titleList != null && !titleList.isEmpty()) {
                    String text = titleList.stream()
                            .map(PageProperty.RichText::getPlainText)
                            .filter(t -> t != null && !t.isBlank())
                            .collect(Collectors.joining());
                    return text.isEmpty() ? "Untitled" : text;
                }
            }
        }
        return "Untitled";
    }

    private String extractProperties(Page page) {
        Map<String, PageProperty> properties = page.getProperties();
        if (properties == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, PageProperty> entry : properties.entrySet()) {
            String propName = entry.getKey();
            PageProperty prop = entry.getValue();
            String value = extractPropertyValue(propName, prop);
            if (value != null && !value.isBlank()) {
                parts.add(value);
            }
        }
        return String.join(" | ", parts);
    }

    private String extractPropertyValue(String propName, PageProperty prop) {
        if (prop.getType() == null) {
            return "";
        }
        return switch (prop.getType()) {
            case RichText -> extractRichText(propName, prop.getRichText());
            case Number -> prop.getNumber() != null ? propName + ": " + prop.getNumber() : "";
            case Select -> prop.getSelect() != null && prop.getSelect().getName() != null
                    ? propName + ": " + prop.getSelect().getName() : "";
            case MultiSelect -> extractMultiSelect(propName, prop.getMultiSelect());
            case Date -> extractDate(propName, prop.getDate());
            case Checkbox -> Boolean.TRUE.equals(prop.getCheckbox()) ? propName + ": Yes" : "";
            case Url -> prop.getUrl() != null ? propName + ": " + prop.getUrl() : "";
            case Email -> prop.getEmail() != null ? propName + ": " + prop.getEmail() : "";
            case PhoneNumber -> prop.getPhoneNumber() != null
                    ? propName + ": " + prop.getPhoneNumber() : "";
            default ->
                    // Handles Status and any other types not in the PropertyType enum for this SDK version
                    prop.getStatus() != null && prop.getStatus().getName() != null
                    ? propName + ": " + prop.getStatus().getName() : "";
        };
    }

    private String extractRichText(String propName, List<PageProperty.RichText> richText) {
        if (richText == null || richText.isEmpty()) {
            return "";
        }
        String text = richText.stream()
                .map(PageProperty.RichText::getPlainText)
                .filter(t -> t != null && !t.isBlank())
                .collect(Collectors.joining());
        return text.isBlank() ? "" : propName + ": " + text;
    }

    private String extractMultiSelect(String propName,
            List<? extends notion.api.v1.model.databases.DatabaseProperty.MultiSelect.Option> options) {
        if (options == null || options.isEmpty()) {
            return "";
        }
        String tags = options.stream()
                .map(notion.api.v1.model.databases.DatabaseProperty.MultiSelect.Option::getName)
                .filter(n -> n != null && !n.isBlank())
                .collect(Collectors.joining(", "));
        return tags.isBlank() ? "" : propName + ": " + tags;
    }

    private String extractDate(String propName, PageProperty.Date dateObj) {
        if (dateObj == null) {
            return "";
        }
        String start = dateObj.getStart();
        String end = dateObj.getEnd();
        if (start != null && end != null) {
            return propName + ": " + start + " to " + end;
        } else if (start != null) {
            return propName + ": " + start;
        }
        return "";
    }

    @Override
    public void close() {
        notionClient.close();
    }
}
