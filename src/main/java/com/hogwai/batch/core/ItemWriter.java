package com.hogwai.batch.core;

import java.util.List;

public interface ItemWriter<O> {
    void write(List<O> items);
}
