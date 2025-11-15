package com.hogwai.batch.core.builder;

import com.hogwai.batch.core.ItemProcessor;
import com.hogwai.batch.core.ItemReader;
import com.hogwai.batch.core.ItemWriter;
import com.hogwai.batch.core.definition.ChunkOrientedStep;
import com.hogwai.batch.core.definition.Step;

public class StepBuilder {

    private final String name;

    public StepBuilder(String name) {
        this.name = name;
    }

    public <I, O> ChunkStepBuilder<I, O> chunk(int chunkSize) {
        return new ChunkStepBuilder<>(name, chunkSize);
    }

    public static class ChunkStepBuilder<I, O> {
        private final String name;
        private final int chunkSize;
        private ItemReader<? extends I> reader;
        private ItemProcessor<? super I, ? extends O> processor;
        private ItemWriter<? super O> writer;

        private ChunkStepBuilder(String name, int chunkSize) {
            this.name = name;
            this.chunkSize = chunkSize;
        }

        public ChunkStepBuilder<I, O> reader(ItemReader<? extends I> reader) {
            this.reader = reader;
            return this;
        }

        public ChunkStepBuilder<I, O> processor(ItemProcessor<? super I, ? extends O> processor) {
            this.processor = processor;
            return this;
        }

        public ChunkStepBuilder<I, O> writer(ItemWriter<? super O> writer) {
            this.writer = writer;
            return this;
        }

        public Step build() {
            if (reader == null) throw new IllegalStateException("reader is required");
            if (writer == null) throw new IllegalStateException("writer is required");

            return new ChunkOrientedStep<>(
                    name, chunkSize, reader, processor, writer
            );
        }
    }
}