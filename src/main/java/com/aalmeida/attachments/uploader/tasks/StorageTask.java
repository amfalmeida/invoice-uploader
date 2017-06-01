package com.aalmeida.attachments.uploader.tasks;

import com.aalmeida.attachments.uploader.Loggable;
import com.aalmeida.utils.FileUtils;
import com.google.api.services.drive.Drive;

import java.util.*;
import java.util.concurrent.*;

public class StorageTask extends AbstractTask implements Loggable {

    private static final int MAX_INBOUND = 1;

    private ExecutorService executor = Executors.newFixedThreadPool(5);
    private Queue<Invoice> inbound = new LinkedList<>();
    private Collection<Callable<Invoice>> tasks = new ArrayList<>();

    private Drive drive;

    public StorageTask(final Drive pDrive) {
        this.drive = pDrive;
    }

    @Override
    public void handleRequest(final Invoice invoice) {
        inbound.add(invoice);
        logger().debug("Add invoice to inbound. invoice={}, inbound={}", invoice, inbound.size());
        if (inbound.size() >= MAX_INBOUND) {
            for (int i = 0; i < MAX_INBOUND; i++){
                tasks.add(new StorageTaskWorker(drive, inbound.poll()));
            }
            List<Future<Invoice>> results = null;
            try {
                results = executor.invokeAll(tasks, 60, TimeUnit.SECONDS);
                tasks.clear();
            } catch (InterruptedException e) {
                logger().error("Fail to execute invoice. invoice={}", invoice, e);
            }
            for (final Future<Invoice> f : results) {
                Invoice outboundInvoice = null;
                try {
                    outboundInvoice = f.get();
                    logger().debug("Outbound invoice. invoice={}", outboundInvoice);
                    handleNext(invoice);

                    // delete processed files
                    FileUtils.delete(f.get().getFiles());
                } catch (InterruptedException | ExecutionException e) {
                    logger().error("Fail to outbound invoice. invoice={}", outboundInvoice, e);
                }
            }
        }
    }
}
