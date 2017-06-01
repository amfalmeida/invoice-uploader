package com.aalmeida.attachments.uploader.email;

class Attachment {

    private final byte[] content;
    private final String name;

    Attachment(final byte[] content, final String name) {
        this.content = content;
        this.name = name;
    }

    byte[] getContent() {
        return content;
    }

    String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "name='" + name + '\'' +
                '}';
    }
}
