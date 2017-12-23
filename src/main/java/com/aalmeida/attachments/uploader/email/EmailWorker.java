package com.aalmeida.attachments.uploader.email;

import com.aalmeida.attachments.uploader.Constants;
import com.aalmeida.attachments.uploader.config.FilterProperties;
import com.aalmeida.attachments.uploader.logging.Loggable;
import com.aalmeida.attachments.uploader.tasks.Invoice;
import com.aalmeida.attachments.uploader.tasks.StorageTask;
import org.slf4j.MDC;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EmailWorker implements Loggable, EmailListener {

    private final FilterProperties filterProperties;
    private final StorageTask storageTask;

    public EmailWorker(final FilterProperties filterProperties, final StorageTask storageTask) {
        this.filterProperties = filterProperties;
        this.storageTask = storageTask;
    }

    @Override
    public void emailReceived(final String id, final Email email) {
        try {
            MDC.put(Constants.Logger.MDC_KEY_ID, UUID.randomUUID().toString());

            if (logger().isTraceEnabled()) {
                logger().trace("Going to check if email match any rule. email={}", email);
            }
            if (filterProperties == null || filterProperties.getTypes() == null) {
                if (logger().isDebugEnabled()) {
                    logger().debug("No filter found.");
                }
                return;
            }

            final List<File> files = new ArrayList<>();
            filterProperties.getTypes().forEach(filter -> {
                if (email.getFromAddress().matches(filter.getFrom())
                        && email.getSubject().matches(filter.getSubject())) {
                    if (email.getAttachments() != null) {
                        email.getAttachments()
                                .forEach(file -> {
                                    if (file.getName().matches(filter.getAttachments())) {
                                        files.add(file);
                                    } else {
                                        file.delete();
                                    }
                                });
                        if (!files.isEmpty()) {
                            if (logger().isDebugEnabled()) {
                                logger().debug("Going to delegate to tasks. files={}, filter={}", files, filter);
                            }
                            storageTask.handleRequest(new Invoice(files, filter, email.getReceivedDate()));
                        } else if (logger().isDebugEnabled()) {
                            logger().debug("No files matches.");
                        }
                    }
                } else if (logger().isDebugEnabled()) {
                    logger().debug("From address and subject don't match. filter={}, email={}", filter, email);
                }
            });
        } catch (Exception e) {
            logger().error("Failed to process received email. email={}", email, e);
        } finally {
            MDC.remove(Constants.Logger.MDC_KEY_ID);
        }
    }
}
