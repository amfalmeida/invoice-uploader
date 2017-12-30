package com.aalmeida.attachments.uploader.service;

import com.aalmeida.attachments.uploader.Constants;
import com.aalmeida.attachments.uploader.email.Email;
import com.aalmeida.attachments.uploader.google.Storage;
import com.aalmeida.attachments.uploader.logging.Loggable;
import com.aalmeida.attachments.uploader.model.Invoice;
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
                filterProperties.getTypes().forEach(filter -> {
                    if (email.getFromAddress().matches(filter.getFrom())
                            && email.getSubject().matches(filter.getSubject())) {
                        if (email.getAttachments() != null) {
                            final List<File> files = new ArrayList<>();
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
                                    logger().debug("Going to delegate to model. files={}, filter={}", files, filter);
                                }
                                try {
                                    MDC.put(Constants.Logger.MDC_KEY_TYPE, filter.getType());

                                    final Invoice invoice = new Invoice(files, filter, email.getReceivedDate());
                                    storage.upload(invoice);

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
                    } else if (logger().isDebugEnabled()) {
                        logger().debug("From address and subject don't match. filter={}, email={}", filter, email);
                    }
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
