package com.hogwai.example.simple.config;

import com.hogwai.batch.core.ItemReader;
import com.hogwai.example.simple.model.Person;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.io.ResourceLoader;
import jakarta.inject.Singleton;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Singleton
public class CsvPersonReader implements ItemReader<Person> {
    private final Iterator<Person> iterator;

    public CsvPersonReader(ResourceLoader resourceLoader,
                           @Value("${csv.path}") String csvPath) throws FileNotFoundException {
        iterator = resourceLoader.getResourceAsStream(csvPath)
                                 .map(stream -> {
                                     try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                                         CsvToBean<Person> csvToBean = new CsvToBeanBuilder<Person>(reader)
                                                 .withType(Person.class)
                                                 .withIgnoreLeadingWhiteSpace(true)
                                                 .withSeparator(';')
                                                 .build();
                                         return csvToBean.parse();
                                     } catch (IOException e) {
                                         throw new RuntimeException(e);
                                     }
                                 })
                                 .orElseThrow(() -> new FileNotFoundException("File not found"))
                                 .iterator();
    }

    @Override
    public Person read() {
        return iterator.hasNext() ? iterator.next() : null;
    }
}
