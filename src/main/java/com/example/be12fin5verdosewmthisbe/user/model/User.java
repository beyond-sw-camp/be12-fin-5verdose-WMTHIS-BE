package com.example.be12fin5verdosewmthisbe.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class User {
@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
}
        