package com.aalmeida.attachments.uploader.email;

public interface EmailListener {

    void emailReceived(final String id, final Email pEmail);
}
