package com.aalmeida.invoice.uploader.tasks;

public abstract class AbstractTask {

    private AbstractTask next;

    public void setNext(AbstractTask pNext) {
        next = pNext;
    }

    abstract protected void process(final Invoice invoice);

    void processNext(final Invoice invoice) {
        if (next != null) {
            next.process(invoice);
        }
    }
}
