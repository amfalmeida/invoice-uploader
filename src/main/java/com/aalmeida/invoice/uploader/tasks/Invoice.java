package com.aalmeida.invoice.uploader.tasks;

import com.aalmeida.invoice.uploader.FilterProperties;

import java.io.File;

public class Invoice {

    private final File file;
    private final String namePattern;
    private final FilterProperties.EmailFilter emailFilter;
    private final long receivedDate;

    public Invoice(File file, String namePattern, FilterProperties.EmailFilter emailFilter, long receivedDate) {
        this.file = file;
        this.namePattern = namePattern;
        this.emailFilter = emailFilter;
        this.receivedDate = receivedDate;
    }

    File getFile() {
        return file;
    }

    String getNamePattern() {
        return namePattern;
    }

    public FilterProperties.EmailFilter getEmailFilter() {
        return emailFilter;
    }

    long getReceivedDate() {
        return receivedDate;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "file=" + file +
                ", namePattern='" + namePattern + '\'' +
                ", emailFilter=" + emailFilter +
                ", receivedDate=" + receivedDate +
                '}';
    }
}
