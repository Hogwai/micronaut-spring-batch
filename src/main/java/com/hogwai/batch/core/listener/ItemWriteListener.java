package com.hogwai.batch.core.listener;

import java.util.List;

public interface ItemWriteListener<O> {
    default void beforeWrite(List<O> items) {}
    default void afterWrite(List<O> items) {}
    default void onWriteError(List<O> items, Exception exception) {}
}
