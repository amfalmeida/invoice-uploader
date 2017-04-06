package com.aalmeida.invoice.uploader;

import com.aalmeida.invoice.uploader.email.EmailListener;
import com.aalmeida.invoice.uploader.email.EmailMonitor;
import com.aalmeida.invoice.uploader.tasks.Invoice;
import com.aalmeida.invoice.uploader.tasks.StorageTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class ApplicationConfig {

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
    @Value("${email.attachments.temporary.folder}")
    private String emailAttachmentsTemporaryFolder;

    @Bean
    public FilterProperties filterProperties() {
        return new FilterProperties();
    }

    @Bean
    @Autowired
    public EmailListener emailListener(final FilterProperties filterProperties, final StorageTask storageTask) {
        final EmailListener emailListener = email -> {
            if (filterProperties == null || filterProperties.getTypes() == null) {
                return;
            }
            for (final FilterProperties.EmailFilter filter : filterProperties.getTypes()) {
                if (email.getFromAddress().matches(filter.getFrom())
                        || email.getSubject().matches(filter.getSubject())) {
                    if (email.getAttachments() == null) {
                        continue;
                    }
                    for (final File file : email.getAttachments()) {
                        if (file.getName().matches(filter.getAttachments())) {
                            storageTask.process(new Invoice(file, filter.getFileName(), filter,
                                    email.getReceivedDate()));
                        }
                    }
                }
            }
        };
        return emailListener;
    }

    @Bean
    @Autowired
    public EmailMonitor emailMonitor(final EmailListener emailListener) {
        final EmailMonitor emailMonitor = new EmailMonitor(emailHost, emailUsername, emailPassword, emailMonitorFolder,
                emailAttachmentsTemporaryFolder, emailMonitorDaysOld);
        emailMonitor.setListener(emailListener);
        return emailMonitor;
    }
}
