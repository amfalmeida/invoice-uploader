package com.aalmeida.attachments.uploader.model;

import java.util.Date;
import java.util.List;

public class Email {

    private String subject;
    private String fromAddress;
    private long receivedDate;
    private List<InvoiceDocument> attachments;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public long getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(long receivedDate) {
        this.receivedDate = receivedDate;
    }

    public List<InvoiceDocument> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<InvoiceDocument> attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return "Email{" +
                "subject='" + subject + '\'' +
                ", fromAddress='" + fromAddress + '\'' +
                ", receivedDate=" + new Date(receivedDate) +
                ", attachments=" + attachments +
                '}';
    }
}
