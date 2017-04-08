package com.aalmeida.invoice.uploader;

import com.aalmeida.invoice.uploader.email.EmailListener;
import com.aalmeida.invoice.uploader.email.EmailMonitor;
import com.aalmeida.invoice.uploader.tasks.Invoice;
import com.aalmeida.invoice.uploader.tasks.StorageTask;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.stream.Stream;

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
    public EmailListener emailListener(final FilterProperties filterProperties, final StorageTask storageTask) {
        return email -> {
            logger().trace("Checking email. email={}", email);
            if (filterProperties == null || filterProperties.getTypes() == null) {
                return;
            }
            filterProperties.getTypes().forEach(filter -> {
                if (email.getFromAddress().matches(filter.getFrom())
                        && email.getSubject().matches(filter.getSubject())) {
                    email.getAttachments()
                            .forEach(file -> {
                                if (file.getName().matches(filter.getAttachments())) {
                                    storageTask.handleRequest(new Invoice(file, filter, email.getReceivedDate()));
                                } else {
                                    file.delete();
                                }
                            });
                }
            });
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
