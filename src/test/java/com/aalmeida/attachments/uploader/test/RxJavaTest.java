package com.aalmeida.attachments.uploader.test;

import io.reactivex.Flowable;
import org.junit.Test;

public class RxJavaTest {

    @Test
    public void test() {
        Flowable.just("Hello world").subscribe(System.out::println);
    }
}
