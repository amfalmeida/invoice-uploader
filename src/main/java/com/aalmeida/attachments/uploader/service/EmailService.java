package com.aalmeida.attachments.uploader.service;

import com.aalmeida.attachments.uploader.Constants;
import com.aalmeida.attachments.uploader.events.EventBus;
import com.aalmeida.attachments.uploader.model.Email;
import com.aalmeida.attachments.uploader.google.Storage;
import com.aalmeida.attachments.uploader.logging.Loggable;
import com.aalmeida.attachments.uploader.model.Invoice;
import com.aalmeida.attachments.uploader.model.InvoiceDocument;
import com.aalmeida.attachments.uploader.properties.FilterProperties;
import io.reactivex.Observable;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class EmailService implements Loggable {

    @Autowired
    private FilterProperties filterProperties;
    @Autowired
    private Storage storage;
    @Autowired
    private EventBus eventBus;

    public Observable<Invoice> emailReceived(final String id, final Email email) {
        return Observable.create(s -> {
            try {
                MDC.put(Constants.Logger.MDC_KEY_EMAIL_ID, id);

                if (logger().isTraceEnabled()) {
                    logger().trace("Going to check if email match any rule. email={}", email);
                }
                if (filterProperties == null || filterProperties.getTypes() == null) {
                    if (logger().isDebugEnabled()) {
                        logger().debug("No filter found.");
                    }
                    s.onComplete();
                }

                filterProperties.getTypes()
                        .stream().filter(filter -> email.getFromAddress().matches(filter.getFrom())
                                && email.getSubject().matches(filter.getSubject()))
                        .findFirst()
                        .ifPresent(filter -> {
                            if (email.getAttachments() != null) {
                                final List<InvoiceDocument> files = new ArrayList<>();
                                email.getAttachments()
                                        .forEach(file -> {
                                            if (file.getName().matches(filter.getAttachments())) {
                                                files.add(file);
                                            } else {
                                                file.getFile().delete();
                                            }
                                        });
                                if (!files.isEmpty()) {
                                    if (logger().isDebugEnabled()) {
                                        logger().debug("Going to delegate to process invoice. files={}, filter={}",
                                                files, filter);
                                    }
                                    try {
                                        MDC.put(Constants.Logger.MDC_KEY_TYPE, filter.getType());

                                        final Invoice invoice = new Invoice(files, filter, email.getReceivedDate());
                                        boolean fileUploaded = storage.upload(invoice);
                                        if (fileUploaded) {
                                            eventBus.send(invoice);
                                        }
                                        s.onNext(invoice);
                                    } catch (Exception e) {
                                        s.onError(e);
                                    } finally {
                                        MDC.remove(Constants.Logger.MDC_KEY_TYPE);
                                    }
                                } else if (logger().isDebugEnabled()) {
                                    logger().debug("No files matches.");
                                }
                            }
                            s.onComplete();
                        });
            } catch (Exception e) {
                logger().error("Failed to process received email. email={}", email, e);
                s.onError(e);
            } finally {
                MDC.remove(Constants.Logger.MDC_KEY_EMAIL_ID);
                s.onComplete();
            }
        });
    }
}
