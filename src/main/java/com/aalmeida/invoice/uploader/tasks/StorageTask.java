package com.aalmeida.invoice.uploader.tasks;

import com.aalmeida.utils.DateUtils;
import com.aalmeida.utils.FileUtils;

import java.io.File;
import java.util.Date;

public class StorageTask extends AbstractTask {

    @Override
    public void process(final Invoice invoice) {
        // upload
        System.out.println("################# UPLOAD " + getFileName(invoice.getFile(), invoice.getNamePattern(),
                invoice.getReceivedDate()));

        processNext(invoice);
    }

    private String getFileName(final File file, final String namePattern, final long receivedDate) {
        return namePattern.replace("${receivedDate}", DateUtils.getDate(receivedDate))
                .replace("${originalName}", FileUtils.getName(file.getName()))
                .replace("${extension}", FileUtils.getExtension(file.getName()));
    }
}
