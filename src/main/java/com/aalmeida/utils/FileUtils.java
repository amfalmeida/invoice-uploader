package com.aalmeida.utils;

import java.io.*;
import java.util.List;

public final class FileUtils {

    private FileUtils() { }

    public static File saveFile(final String content, final String path, final String fileName) throws IOException {
        return saveFile(new ByteArrayInputStream(content.getBytes()), path, fileName);
    }

    /**
     * Save file.
     *
     * @param is
     *            the is
     * @param tempPath
     *            the temp path
     * @param fileName
     *            the file name
     * @return the file
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static File saveFile(final InputStream is, final String tempPath, final String fileName) throws IOException {
        String strPath = tempPath;
        if (!strPath.endsWith(File.separator)) {
            strPath += File.separator;
        }
        final File path = new File(strPath);
        path.mkdirs();
        final File f = new File(strPath + fileName);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buf)) != -1) {
                fos.write(buf, 0, bytesRead);
            }
        }
        return f;
    }

    /**
     * Split file name.
     *
     * @param fileName
     *            the file name
     * @param position
     *            the position
     * @return the string
     */
    private static String splitFileName(String fileName, int position) {
        String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
        return tokens[position];
    }

    /**
     * Gets the name.
     *
     * @param fileName
     *            the file name
     * @return the name
     */
    public static String getName(String fileName) {
        return splitFileName(fileName, 0);
    }

    /**
     * Gets the extension.
     *
     * @param fileName
     *            the file name
     * @return the extension
     */
    public static String getExtension(String fileName) {
        return splitFileName(fileName, 1);
    }

    /**
     * Delete.
     *
     * @param files
     *            the files
     */
    public static void delete(List<File> files) {
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

}
