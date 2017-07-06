package com.aalmeida.attachments.uploader;

import com.aalmeida.attachments.uploader.email.Email;
import com.aalmeida.attachments.uploader.email.EmailMonitor;
import com.aalmeida.attachments.uploader.tasks.Invoice;
import com.aalmeida.attachments.uploader.tasks.StorageTask;
import com.aalmeida.attachments.uploader.email.EmailListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
public class ApplicationConfig implements Loggable {

    @Value("${email.host}")
    private String emailHost;
    @Value("${email.username}")
    private String emailUsername;
    @Value("${email.password}")
    private String emailPassword;
    @Value("${email.monitor.folder}")
    private String emailMonitorFolder;
    @Value("${email.monitor.days.older}")
    private int emailMonitorDaysOld;
    @Value("${email.monitor.subject.pattern}")
    private String emailMonitorSubjectPattern;
    @Value("${email.attachments.temporary.folder}")
    private String emailAttachmentsTemporaryFolder;

    @Bean
    public FilterProperties filterProperties() {
        return new FilterProperties();
    }

    @Bean
    @Autowired
    public EmailListener emailListener(final FilterProperties filterProperties,
                                       final StorageTask storageTask) {
        return (id, email)  -> {
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
        };
    }

    @Bean
    @Autowired
    public EmailMonitor emailMonitor(final EmailListener emailListener) {
        final EmailMonitor emailMonitor = new EmailMonitor(emailHost, emailUsername, emailPassword, emailMonitorFolder,
                emailAttachmentsTemporaryFolder, emailMonitorDaysOld, emailMonitorSubjectPattern);
        emailMonitor.setListener(emailListener);
        return emailMonitor;
    }
}
