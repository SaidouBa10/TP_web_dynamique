package com.example.tp1.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private String author;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;
}