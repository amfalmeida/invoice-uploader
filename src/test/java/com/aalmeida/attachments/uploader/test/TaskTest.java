package com.aalmeida.attachments.uploader.test;

import com.aalmeida.attachments.uploader.properties.FilterProperties;
import com.aalmeida.attachments.uploader.model.Invoice;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.FileList;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class TaskTest {

    @Mock
    private Drive drive;
    @Mock
    private FileList fileList;
    @Spy
    private Invoice invoice;

    @Test
    public void test_StorageTask() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();

            final List<java.io.File> dummyFiles = new ArrayList<>();
            for (int i = 1; i < 4; i++) {
                dummyFiles.add(new File(classLoader.getResource(String.format("invoices/invoice_page_%s.pdf", i)).toURI()));
            }

            final FilterProperties.EmailFilter emailFilter = new FilterProperties.EmailFilter();
            //emailFilter.setAttachments("(?i)(.*).pdf");
            emailFilter.setFileMimeType("application/pdf");
            emailFilter.setFileName("${receivedDate}_${originalName}.${extension}");
            emailFilter.setFolderId("0B9cL05zbIkdScnBOdGFaeXI4bkU");
            emailFilter.setFolder("Test");
            emailFilter.setMergeOrder(FilterProperties.EmailFilter.MergeOrder.ASC);

            Mockito.doReturn(dummyFiles).when(invoice).getFiles();
            Mockito.doReturn(emailFilter).when(invoice).getEmailFilter();
            Mockito.doReturn(System.currentTimeMillis()).when(invoice).getReceivedDate();

            /*StorageTask storageTask = new StorageTask(drive);
            storageTask.handleRequest(invoice);*/

            Mockito.when(drive.files().list().execute()).thenReturn(fileList);

        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
