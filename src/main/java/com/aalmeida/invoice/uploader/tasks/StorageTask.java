package com.aalmeida.invoice.uploader.tasks;

import com.aalmeida.invoice.uploader.Loggable;
import com.aalmeida.utils.DateUtils;
import com.aalmeida.utils.FileUtils;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Collections;

public class StorageTask extends AbstractTask implements Loggable {

    private Drive drive;

    @Autowired
    public StorageTask(final Drive pDrive) {
        this.drive = pDrive;
    }

    @Override
    public void process(final Invoice invoice) {
        Thread thread = new Thread(() -> {
            final String filename = getFileName(invoice.getFile(), invoice.getNamePattern(), invoice.getReceivedDate());
            try {
                final File folder = searchFolder(invoice.getEmailFilter().getFolderId());
                if (folder != null) {
                    logger().trace("Folder found. folder={}", folder);
                    final File file = searchFile(folder.getId(), filename, invoice.getEmailFilter().getFileMimeType());
                    if (file == null) {
                        logger().debug("File doesn't exist and will be uploaded. invoice={}", invoice);
                        final File uploadedFile = uploadFile(folder.getId(), invoice.getFile(), filename,
                                invoice.getEmailFilter().getFileMimeType());
                        logger().info("File uploaded. file={}", uploadedFile);
                    } else {
                        logger().trace("File exists. file={}", file);
                    }
                }
            } catch (IOException e) {
                logger().error("Failed to process the invoice. invoice={}", invoice, e);
            }
        });
        thread.run();
        processNext(invoice);
    }

    private File searchFolder(final String folderId) throws IOException {
        String pageToken = null;
        do {
            // https://developers.google.com/drive/v3/web/search-parameters
            FileList result = drive.files().list()
                    //.setQ("'root' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                    //.setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false and ")
                    .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, parents)")
                    .setPageToken(pageToken)
                    .execute();
            for (final File file: result.getFiles()) {
                if (file.getId().equals(folderId)) {
                    return file;
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return null;
    }

    private File searchFile(final String folderId, final String filename, final String mimeType) throws IOException {
        String pageToken = null;
        do {
            FileList result = drive.files().list()
                    .setQ(String.format("'%s' in parents and mimeType='%s' and trashed = false", folderId, mimeType))
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name, parents)")
                    .setPageToken(pageToken)
                    .execute();
            for (final File file: result.getFiles()) {
                if (file.getName().equals(filename)) {
                    return file;
                }
            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);
        return null;
    }

    private File uploadFile(final String folderId, final java.io.File fileToUpload, final String filename,
                            final String fileMimeType) throws IOException {
        final File fileMetadata = new File();
        fileMetadata.setName(filename);
        fileMetadata.setParents(Collections.singletonList(folderId));
        final FileContent mediaContent = new FileContent(fileMimeType, fileToUpload);
        return drive.files().create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute();
    }

    private String getFileName(final java.io.File file, final String namePattern, final long receivedDate) {
        return namePattern.replace("${receivedDate}", DateUtils.getDate(receivedDate))
                .replace("${originalName}", FileUtils.getName(file.getName()))
                .replace("${extension}", FileUtils.getExtension(file.getName()));
    }
}
