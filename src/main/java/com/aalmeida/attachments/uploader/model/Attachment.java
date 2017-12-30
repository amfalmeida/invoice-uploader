package com.aalmeida.attachments.uploader.model;

public class Attachment {

    private final byte[] content;
    private final String name;

    public Attachment(final byte[] content, final String name) {
        this.content = content;
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "name='" + name + '\'' +
                '}';
    }
}
