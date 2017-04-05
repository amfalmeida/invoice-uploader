package com.aalmeida.invoice.uploader;

import com.aalmeida.invoice.uploader.email.EmailListener;
import com.aalmeida.invoice.uploader.email.EmailMonitor;
import com.aalmeida.invoice.uploader.tasks.Invoice;
import com.aalmeida.invoice.uploader.tasks.StorageTask;
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
    public StorageTask storageTask() {
        return new StorageTask();
    }

    @Bean
    public FilterProperties filterProperties() {
        return new FilterProperties();
    }

    @Bean
    public EmailListener emailListener(FilterProperties filterProperties) {
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
                            storageTask().process(new Invoice(file, filter.getName(), filter.getFolder(),
                                    email.getReceivedDate()));
                        }
                    }
                }
            }
        };
        return emailListener;
    }

    @Bean
    public EmailMonitor emailMonitor() {
        final EmailMonitor emailMonitor = new EmailMonitor(emailHost, emailUsername, emailPassword, emailMonitorFolder,
                emailAttachmentsTemporaryFolder, emailMonitorDaysOld);
        emailMonitor.setListener(emailListener(filterProperties()));
        return emailMonitor;
    }
}
