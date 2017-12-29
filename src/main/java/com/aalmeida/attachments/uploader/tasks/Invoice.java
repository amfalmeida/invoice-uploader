package com.aalmeida.attachments.uploader.tasks;

import com.aalmeida.attachments.uploader.config.FilterProperties;

import java.io.File;
import java.util.List;

public class Invoice {

    private List<File> files;
    private FilterProperties.EmailFilter emailFilter;
    private long receivedDate;

    public Invoice() {
    }

    public Invoice(List<File> files, FilterProperties.EmailFilter emailFilter, long receivedDate) {
        this.files = files;
        this.emailFilter = emailFilter;
        this.receivedDate = receivedDate;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public FilterProperties.EmailFilter getEmailFilter() {
        return emailFilter;
    }

    public void setEmailFilter(FilterProperties.EmailFilter emailFilter) {
        this.emailFilter = emailFilter;
    }

    public long getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(long receivedDate) {
        this.receivedDate = receivedDate;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "files=" + files +
                ", emailFilter=" + emailFilter +
                ", receivedDate=" + receivedDate +
                '}';
    }
}
