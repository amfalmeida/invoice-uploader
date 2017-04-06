package com.aalmeida.invoice.uploader.tasks;

public abstract class AbstractTask {

    private AbstractTask next;

    public void setNext(AbstractTask pNext) {
        next = pNext;
    }

    abstract protected void handleRequest(final Invoice invoice);

    void handleNext(final Invoice invoice) {
        if (next != null) {
            next.handleRequest(invoice);
        }
    }
}
