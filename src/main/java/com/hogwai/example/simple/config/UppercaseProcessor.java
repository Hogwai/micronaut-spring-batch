package com.hogwai.example.simple.config;

import com.hogwai.batch.core.ItemProcessor;
import com.hogwai.example.simple.model.Person;
import jakarta.inject.Singleton;

@Singleton
public class UppercaseProcessor implements ItemProcessor<Person, Person> {
    @Override
    public Person process(Person person) {
        return new Person(person.getId(), person.getName().toUpperCase(), person.getEmail());
    }
}
