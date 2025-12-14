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
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @NonNull
    @Column(columnDefinition = "TEXT")
    private String reason;

    @Temporal(TemporalType.TIMESTAMP)
    private Date reportDate = new Date();

    private boolean reviewed = false;
    private boolean actionTaken = false;
}
