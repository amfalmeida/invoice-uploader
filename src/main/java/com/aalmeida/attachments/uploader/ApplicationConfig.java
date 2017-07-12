package com.aalmeida.attachments.uploader;

import com.aalmeida.attachments.uploader.config.FilterProperties;
import com.aalmeida.attachments.uploader.email.EmailListener;
import com.aalmeida.attachments.uploader.email.EmailMonitor;
import com.aalmeida.attachments.uploader.email.EmailWorker;
import com.aalmeida.attachments.uploader.logging.Loggable;
import com.aalmeida.attachments.uploader.tasks.Invoice;
import com.aalmeida.attachments.uploader.tasks.StorageTask;
import org.slf4j.MDC;
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
    public EmailListener emailListener(final FilterProperties filterProperties,
                                       final StorageTask storageTask) {
        return new EmailWorker(filterProperties, storageTask);
    }

    @Bean
    public EmailMonitor emailMonitor(final EmailListener emailListener) {
        final EmailMonitor emailMonitor = new EmailMonitor(emailHost, emailUsername, emailPassword, emailMonitorFolder,
                emailAttachmentsTemporaryFolder, emailMonitorDaysOld, emailMonitorSubjectPattern);
        emailMonitor.setListener(emailListener);
        return emailMonitor;
    }
}
