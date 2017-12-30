package com.aalmeida.attachments.uploader.model;

import com.aalmeida.attachments.uploader.properties.FilterProperties;

import java.io.File;
import java.util.List;

public class Invoice {

    private List<InvoiceDocument> files;
    private FilterProperties.EmailFilter emailFilter;
    private long receivedDate;

    public Invoice() {
    }

    public Invoice(List<InvoiceDocument> files, FilterProperties.EmailFilter emailFilter, long receivedDate) {
        this.files = files;
        this.emailFilter = emailFilter;
        this.receivedDate = receivedDate;
    }

    public List<InvoiceDocument> getFiles() {
        return files;
    }

    public void setFiles(List<InvoiceDocument> files) {
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
