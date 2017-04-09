package com.aalmeida.attachments.uploader.tasks;

import com.aalmeida.attachments.uploader.FilterProperties;
import com.aalmeida.attachments.uploader.Constants;
import com.aalmeida.attachments.uploader.Loggable;
import com.aalmeida.utils.DateUtils;
import com.aalmeida.utils.FileUtils;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class StorageTaskWorker implements Loggable, Callable<Invoice> {

    private Drive drive;
    private Invoice invoice;

    StorageTaskWorker(final Drive pDrive, final Invoice pInvoice) {
        drive = pDrive;
        invoice = pInvoice;
    }

    @Override
    public Invoice call() throws Exception {
        try {
            MDC.put(Constants.Logger.MDC_KEY_TYPE, invoice.getEmailFilter().getType());

            final String filename;
            final java.io.File fileToUpload;

            if (invoice.getEmailFilter().isMerge() && invoice.getFiles().size() > 1) {
                final List<java.io.File> orderedFiles = invoice.getFiles();
                if (invoice.getEmailFilter().getMergeOrder() == FilterProperties.EmailFilter.MergeOrder.ASC) {
                    orderedFiles.sort(NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
                } else {
                    orderedFiles.sort(NameFileComparator.NAME_INSENSITIVE_REVERSE);
                }

                filename = getFileName(orderedFiles.get(0), invoice.getEmailFilter().getFileName(),
                        invoice.getReceivedDate());

                mergeFiles(orderedFiles, filename);

                fileToUpload = new java.io.File(filename);
            } else {
                fileToUpload = invoice.getFiles().get(0);
                filename = getFileName(fileToUpload, invoice.getEmailFilter().getFileName(),
                        invoice.getReceivedDate());
            }

            final File folder = searchFolder(invoice.getEmailFilter().getFolderId());
            if (folder != null) {
                logger().trace("Folder found. folder={}", folder);
                final File file = searchFile(folder.getId(), filename, invoice.getEmailFilter().getFileMimeType());
                if (file == null) {
                    logger().debug("File doesn't exist and will be uploaded. filename={}, invoice={}", filename, invoice);
                    final File uploadedFile = uploadFile(folder.getId(), fileToUpload, filename,
                            invoice.getEmailFilter().getFileMimeType());
                    logger().info("File uploaded. filename={}, file={}", filename, uploadedFile);
                } else {
                    logger().trace("File already exists. filename={}, file={}", filename, file);
                }
            }
        } finally {
            MDC.remove(Constants.Logger.MDC_KEY_TYPE);
        }
        return invoice;
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
        return drive.files().create(fileMetadata, new FileContent(fileMimeType, fileToUpload))
                .setFields("id, parents")
                .execute();
    }

    private String mergeFiles(final List<java.io.File> files, final String finalName)
            throws IOException {
        final String path = String.format("%s%s%s", files.get(0).getParent(), "/", finalName);
        final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();
        for (final java.io.File file : files) {
            pdfMergerUtility.addSource(file);
        }
        pdfMergerUtility.setDestinationFileName(path);
        //pdfMergerUtility.setDestinationFileName(finalName);
        pdfMergerUtility.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
        return path;
    }

    private String getFileName(final java.io.File file, final String namePattern, final long receivedDate) {
        return namePattern.replace("${receivedDate}", DateUtils.getDate(receivedDate))
                .replace("${originalName}", FileUtils.getName(file.getName()))
                .replace("${extension}", FileUtils.getExtension(file.getName()));
    }
}
