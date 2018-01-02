package com.aalmeida.attachments.uploader.events;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class EventBus {

    private PublishSubject<Object> bus = PublishSubject.create();

    public void send(Object o) {
        bus.onNext(o);
    }

    public Observable<Object> toObservable() {
        return bus;
    }
}
