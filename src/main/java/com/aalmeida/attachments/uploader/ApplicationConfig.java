package com.aalmeida.attachments.uploader;

import com.aalmeida.attachments.uploader.email.EmailMonitor;
import com.aalmeida.attachments.uploader.events.EventBus;
import com.aalmeida.attachments.uploader.logging.Loggable;
import com.aalmeida.attachments.uploader.properties.FilterProperties;
import com.aalmeida.attachments.uploader.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public EmailService emailService() {
        return new EmailService();
    }

    @Bean
    public EmailMonitor emailMonitor() {
        return new EmailMonitor(emailHost, emailUsername, emailPassword, emailMonitorFolder,
                emailAttachmentsTemporaryFolder, emailMonitorDaysOld, emailMonitorSubjectPattern);
    }

    @Bean
    public EventBus eventBus() {
        return new EventBus();
    }
}
