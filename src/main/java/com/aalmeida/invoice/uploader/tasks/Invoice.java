package com.aalmeida.invoice.uploader.tasks;

import java.io.File;

public class Invoice {

    private final File file;
    private final String namePattern;
    private final String folder;
    private final long receivedDate;

    public Invoice(File file, String namePattern, String folder, long receivedDate) {
        this.file = file;
        this.namePattern = namePattern;
        this.folder = folder;
        this.receivedDate = receivedDate;
    }

    File getFile() {
        return file;
    }

    String getNamePattern() {
        return namePattern;
    }

    String getFolder() {
        return folder;
    }

    long getReceivedDate() {
        return receivedDate;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "file=" + file +
                ", namePattern='" + namePattern + '\'' +
                ", folder='" + folder + '\'' +
                ", receivedDate=" + receivedDate +
                '}';
    }
}
