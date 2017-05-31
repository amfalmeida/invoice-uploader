package com.aalmeida.utils;

import java.io.*;
import java.util.List;

public final class FileUtils {

    private static final String SUFFIX_SEPARATOR = "-#-";

    private FileUtils() { }

    public static File saveFile(final byte[] content, final String tempPath, final String fileName,
                                final String suffix) throws IOException {
        String strPath = tempPath;
        if (!strPath.endsWith(File.separator)) {
            strPath += File.separator;
        }
        final File path = new File(strPath);
        path.mkdirs();

        String filePath = String.format("%s%s", strPath, fileName);
        if (suffix != null) {
            final String name = getName(fileName);
            final String extension = getExtension(fileName);
            filePath = String.format("%s%s%s%s%s.%s", strPath, name, SUFFIX_SEPARATOR, suffix, SUFFIX_SEPARATOR, extension);
        }
        final File f = new File(filePath);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            fos.write(content);
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
        if (fileName.indexOf(SUFFIX_SEPARATOR) > 0) {
            return splitFileName(removeSuffix(fileName), 0);
        }
        return splitFileName(fileName, 0);
    }

    /**
     * Gets the extension.
     *
     * @param fileName
     *            the file name
     * @return the extension
     */
    public static String getExtension(final String fileName) {
        if (fileName.indexOf(SUFFIX_SEPARATOR) > 0) {
            return splitFileName(removeSuffix(fileName), 1);
        }
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

    private static String removeSuffix(final String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return null;
        }
        final String[] tokens = fileName.split(SUFFIX_SEPARATOR);
        if (tokens.length < 3) {
            return null;
        }
        return String.format("%s%s", tokens[0], tokens[2]);
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
