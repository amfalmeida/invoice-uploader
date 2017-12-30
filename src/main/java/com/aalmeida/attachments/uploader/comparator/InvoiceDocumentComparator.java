/*
 * Copyright (c) 2017 WIT Software. All rights reserved.
 *
 * WIT Software Confidential and Proprietary information. It is strictly forbidden for 3rd parties to modify, decompile,
 * disassemble, defeat, disable or circumvent any protection mechanism; to sell, license, lease, rent, redistribute or
 * make accessible to any third party, whether for profit or without charge.
 *
 * aalmeida 2017/12/30
 */
package com.aalmeida.attachments.uploader.comparator;

import com.aalmeida.attachments.uploader.model.InvoiceDocument;

import java.util.Comparator;

public class InvoiceDocumentComparator implements Comparator<InvoiceDocument> {

    public enum SortOrder {
        ASC,
        DESC
    }
    private final SortOrder sortOrder;

    public InvoiceDocumentComparator(final SortOrder pSortOrder) {
        sortOrder = pSortOrder;
    }

    @Override
    public int compare(final InvoiceDocument o1, final InvoiceDocument o2) {
        if (SortOrder.ASC.equals(sortOrder)) {
            return o1.getName().compareTo(o2.getName());
        } else {
            return o2.getName().compareTo(o1.getName());
        }

    }
}
