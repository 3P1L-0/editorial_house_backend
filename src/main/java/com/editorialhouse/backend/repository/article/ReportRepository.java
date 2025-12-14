package com.editorialhouse.backend.repository.article;

import com.editorialhouse.backend.model.article.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReviewedFalse();
}
