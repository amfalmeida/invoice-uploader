package com.aalmeida.attachments.uploader;

import com.aalmeida.attachments.uploader.email.EmailMonitor;
import com.aalmeida.attachments.uploader.tasks.Invoice;
import com.aalmeida.attachments.uploader.tasks.StorageTask;
import com.aalmeida.attachments.uploader.email.EmailListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        return email -> {
            logger().trace("Checking email. email={}", email);
            if (filterProperties == null || filterProperties.getTypes() == null) {
                return;
            }
            filterProperties.getTypes().forEach(filter -> {
                if (email.getFromAddress().matches(filter.getFrom())
                        && email.getSubject().matches(filter.getSubject())) {
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
                        storageTask.handleRequest(new Invoice(files, filter, email.getReceivedDate()));
                    }
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
