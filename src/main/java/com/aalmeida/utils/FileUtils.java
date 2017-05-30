package com.aalmeida.utils;

import java.io.*;
import java.util.List;

public final class FileUtils {

    private static final String PREFIX_SEPARATOR = "-#-";

    private FileUtils() { }

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
    public static File saveFile(final InputStream is, final String tempPath, final String fileName,
                                final String prefix) throws IOException {
        String strPath = tempPath;
        if (!strPath.endsWith(File.separator)) {
            strPath += File.separator;
        }
        final File path = new File(strPath);
        path.mkdirs();

        String filePath = String.format("%s%s", strPath, fileName);
        if (prefix != null) {
            filePath = String.format("%s%s%s%s%s", strPath, PREFIX_SEPARATOR, prefix, PREFIX_SEPARATOR, fileName);
        }
        final File f = new File(filePath);
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
     * Gets the name.
     *
     * @param fileName
     *            the file name
     * @return the name
     */
    public static String getName(final String fileName) {
        return removePrefix(splitFileName(fileName, 0));
    }

    /**
     * Gets the extension.
     *
     * @param fileName
     *            the file name
     * @return the extension
     */
    public static String getExtension(final String fileName) {
        return splitFileName(fileName, 1);
    }

    /**
     * Delete.
     *
     * @param files
     *            the files
     */
    public static void delete(final List<File> files) {
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private static String removePrefix(final String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        final String[] tokens = fileName.split(PREFIX_SEPARATOR);
        if (tokens.length < 3) {
            return null;
        }
        return tokens[2];
    }

    private static String splitFileName(String fileName, int position) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        final String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
        if (tokens.length < position) {
            return null;
        }
        return tokens[position];
    }
}
