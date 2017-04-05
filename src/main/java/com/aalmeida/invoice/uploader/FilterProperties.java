package com.aalmeida.invoice.uploader;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("filter")
public class FilterProperties {

    private List<EmailFilter> types = new ArrayList<>();

    public List<EmailFilter> getTypes() {
        return types;
    }

    @Override
    public String toString() {
        return "FilterProperties{" +
                "types=" + types +
                '}';
    }

    public static class EmailFilter {

        private String type;
        private String from;
        private String subject;
        private String attachments;
        private String name;
        private String folder;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getAttachments() {
            return attachments;
        }

        public void setAttachments(String attachments) {
            this.attachments = attachments;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFolder() {
            return folder;
        }

        public void setFolder(String folder) {
            this.folder = folder;
        }

        @Override
        public String toString() {
            return "EmailFilter{" +
                    "type='" + type + '\'' +
                    ", from='" + from + '\'' +
                    ", subject='" + subject + '\'' +
                    ", attachments='" + attachments + '\'' +
                    ", name='" + name + '\'' +
                    ", folder='" + folder + '\'' +
                    '}';
        }
    }
}
