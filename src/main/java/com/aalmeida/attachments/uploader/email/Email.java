package com.aalmeida.invoice.uploader.email;

import java.io.File;
import java.util.Date;
import java.util.List;

public class Email {

    private String subject;
    private String fromAddress;
    private long receivedDate;
    private List<File> attachments;

    public String getSubject() {
        return subject;
    }

    void setSubject(String subject) {
        this.subject = subject;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public long getReceivedDate() {
        return receivedDate;
    }

    void setReceivedDate(long receivedDate) {
        this.receivedDate = receivedDate;
    }

    public List<File> getAttachments() {
        return attachments;
    }

    void setAttachments(List<File> attachments) {
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
