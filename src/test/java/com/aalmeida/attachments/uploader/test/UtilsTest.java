package com.aalmeida.attachments.uploader.test;

import com.aalmeida.utils.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(MockitoJUnitRunner.class)
public class UtilsTest {

    private static final String FILE_NAME_TEST = ".\\temp\\-#-0-#-2017052215SE790067403.pdf";

    @Test
    public void test_fileNameName() {
        final String result = FileUtils.getName(FILE_NAME_TEST);
        assertNotNull(result);
        assertEquals(result, "2017052215SE790067403");
    }

    @Test
    public void test_fileNameExtension() {
        final String result = FileUtils.getExtension(FILE_NAME_TEST);
        assertNotNull(result);
        assertEquals(result, "pdf");
    }
}
