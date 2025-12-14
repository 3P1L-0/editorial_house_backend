package com.editorialhouse.backend.model.article;

import com.editorialhouse.backend.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String title;

    @NonNull
    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate = new Date();

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate = new Date();

    @Enumerated(EnumType.STRING)
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason; // For rejection workflow

    private boolean published = false;

    // Fields for multimedia content (simplified as URLs for now)
    private String imageUrl;
    private String audioUrl;
    private String videoUrl;
}
