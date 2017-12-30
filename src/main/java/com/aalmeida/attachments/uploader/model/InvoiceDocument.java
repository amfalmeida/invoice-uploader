/*
 * Copyright (c) 2017 WIT Software. All rights reserved.
 *
 * WIT Software Confidential and Proprietary information. It is strictly forbidden for 3rd parties to modify, decompile,
 * disassemble, defeat, disable or circumvent any protection mechanism; to sell, license, lease, rent, redistribute or
 * make accessible to any third party, whether for profit or without charge.
 *
 * aalmeida 2017/12/30
 */
package com.aalmeida.attachments.uploader.model;

import java.io.File;

public class InvoiceDocument {

    private final File file;
    private final String name;

    public InvoiceDocument(final File pFile, final String pName) {
        file = pFile;
        name = pName;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "InvoiceDocument{" +
                "file=" + file +
                ", name='" + name + '\'' +
                '}';
    }
}
