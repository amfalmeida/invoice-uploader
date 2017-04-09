import com.aalmeida.invoice.uploader.FilterProperties;
import com.aalmeida.invoice.uploader.tasks.Invoice;
import com.aalmeida.invoice.uploader.tasks.StorageTask;
import com.google.api.services.drive.Drive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class TaskTest {

    @Mock
    private Drive drive;
    @Spy
    private Drive.Files driveFiles;
    @Spy
    private Invoice invoice;
    @Spy
    private FilterProperties.EmailFilter emailFilter;

    @Test
    public void test_StorageTask() {
        try {
            Mockito.when(drive.files().get(Mockito.anyString()).execute()).thenReturn(driveFiles);

            Mockito.doReturn(true).when(invoice).getFiles();
            Mockito.doReturn(emailFilter).when(invoice).getEmailFilter();
            Mockito.doReturn(System.currentTimeMillis()).when(invoice).getReceivedDate();

            StorageTask storageTask = new StorageTask(drive);
            storageTask.handleRequest(invoice);

        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }
}
