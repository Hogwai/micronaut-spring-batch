package com.hogwai.example.simple.model;

import com.opencsv.bean.CsvBindByName;

public class Person {

    @CsvBindByName(column = "id")
    private String id;

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "email")
    private String email;

    // Constructeur par défaut
    public Person() {
    }

    // Constructeur avec paramètres
    public Person(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

