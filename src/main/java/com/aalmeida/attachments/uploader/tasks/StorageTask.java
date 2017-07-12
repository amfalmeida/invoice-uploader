package com.aalmeida.attachments.uploader.tasks;

import com.aalmeida.attachments.uploader.logging.Loggable;
import com.aalmeida.utils.FileUtils;
import com.google.api.services.drive.Drive;

public class StorageTask extends AbstractTask implements Loggable {

    private Drive drive;

    public StorageTask(final Drive pDrive) {
        this.drive = pDrive;
    }

    @Override
    public void handleRequest(final Invoice invoice) {
        if (logger().isDebugEnabled()) {
            logger().debug("Add invoice to executor. invoice={}", invoice);
        }
        try {
            new StorageTaskWorker(drive, invoice).call();
        } catch (Exception e) {
            logger().error("Fail to execute StorageTaskWorker. invoice={}", invoice, e);
        }
        if (logger().isDebugEnabled()) {
            logger().debug("Handling to next task. invoice={}", invoice);
        }
        handleNext(invoice);

        // delete processed files
        FileUtils.delete(invoice.getFiles());
    }
}
