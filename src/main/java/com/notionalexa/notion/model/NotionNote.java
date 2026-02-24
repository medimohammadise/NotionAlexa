package com.notionalexa.notion.model;

public class NotionNote {

    private final String id;
    private final String title;
    /** Page database properties (status, tags, dates, etc.) */
    private final String content;
    /** Full page body text extracted from Notion blocks */
    private final String body;
    private final String createdTime;
    private final String lastEditedTime;

    public NotionNote(String id, String title, String content) {
        this(id, title, content, null, null, null);
    }

    public NotionNote(String id, String title, String content,
                      String createdTime, String lastEditedTime) {
        this(id, title, content, null, createdTime, lastEditedTime);
    }

    public NotionNote(String id, String title, String content, String body,
                      String createdTime, String lastEditedTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.body = body;
        this.createdTime = createdTime;
        this.lastEditedTime = lastEditedTime;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getBody() {
        return body;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public String getLastEditedTime() {
        return lastEditedTime;
    }

    @Override
    public String toString() {
        return "NotionNote{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", createdTime='" + createdTime + '\'' +
                ", lastEditedTime='" + lastEditedTime + '\'' +
                '}';
    }
}
