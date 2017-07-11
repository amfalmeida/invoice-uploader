package com.aalmeida.attachments.uploader;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("filter")
public class FilterProperties {

    private List<EmailFilter> types;

    FilterProperties() {
        types = new ArrayList<>();
    }

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

        public enum MergeOrder {
            DESC,
            ASC;
        }

        private String type;
        private String from;
        private String subject;
        private String attachments;
        private String fileName;
        private String fileMimeType;
        private String folder;
        private String folderId;
        private MergeOrder mergeOrder = MergeOrder.ASC;

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

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getFileMimeType() {
            return fileMimeType;
        }

        public void setFileMimeType(String fileMimeType) {
            this.fileMimeType = fileMimeType;
        }

        public String getFolder() {
            return folder;
        }

        public void setFolder(String folder) {
            this.folder = folder;
        }

        public String getFolderId() {
            return folderId;
        }

        public void setFolderId(String folderId) {
            this.folderId = folderId;
        }

        public MergeOrder getMergeOrder() {
            return mergeOrder;
        }

        public void setMergeOrder(MergeOrder mergeOrder) {
            this.mergeOrder = mergeOrder;
        }

        @Override
        public String toString() {
            return "EmailFilter{" +
                    "type='" + type + '\'' +
                    ", from='" + from + '\'' +
                    ", subject='" + subject + '\'' +
                    ", attachments='" + attachments + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", fileMimeType='" + fileMimeType + '\'' +
                    ", folder='" + folder + '\'' +
                    ", folderId='" + folderId + '\'' +
                    ", mergeOrder=" + mergeOrder +
                    '}';
        }
    }
}
