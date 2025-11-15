package com.hogwai.batch.core.definition;

import com.hogwai.batch.core.ItemProcessor;
import com.hogwai.batch.core.ItemReader;
import com.hogwai.batch.core.ItemWriter;

import java.util.ArrayList;
import java.util.List;

public class ChunkOrientedStep<I, O> implements Step {

    private final String name;
    private final int chunkSize;
    private final ItemReader<? extends I> reader;
    private final ItemProcessor<? super I, ? extends O> processor;
    private final ItemWriter<O> writer;

    public ChunkOrientedStep(
            String name,
            int chunkSize,
            ItemReader<? extends I> reader,
            ItemProcessor<? super I, ? extends O> processor,
            ItemWriter<O> writer
    ) {
        this.name = name;
        this.chunkSize = chunkSize;
        this.reader = reader;
        this.processor = processor;
        this.writer = writer;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void execute() {
        List<O> chunk = new ArrayList<>(chunkSize);
        I item;

        while ((item = reader.read()) != null) {
            O output = (processor != null) ? processor.process(item) : (O) item;
            chunk.add(output);

            if (chunk.size() >= chunkSize) {
                writer.write(chunk);
                chunk.clear();
            }
        }

        if (!chunk.isEmpty()) {
            writer.write(chunk);
        }
    }
}
