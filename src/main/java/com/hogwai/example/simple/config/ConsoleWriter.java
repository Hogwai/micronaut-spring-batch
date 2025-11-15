package com.hogwai.example.simple.config;

import com.hogwai.batch.core.ItemWriter;
import com.hogwai.example.simple.model.Person;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
public class ConsoleWriter implements ItemWriter<Person> {
    private static final Logger LOG = LoggerFactory.getLogger(ConsoleWriter.class);


    @Override
    public void write(List<Person> items) {
        LOG.info("Writing {} persons", items.size());
        items.forEach(p -> LOG.info("Person: {}, {}, {}", p.getId(), p.getName(), p.getEmail()));
    }
}
