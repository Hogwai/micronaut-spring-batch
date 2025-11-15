package com.hogwai.batch.core;

import java.util.Optional;

public interface ItemReader<T> {
    T read();
}