package com.hogwai.batch.core.listener;

public interface ItemReadListener<T> {
    default void beforeRead() {}
    default void afterRead(T item) {}
    default void onReadError(Exception exception) {}
}
