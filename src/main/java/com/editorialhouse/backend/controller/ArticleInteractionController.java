package com.editorialhouse.backend.controller;

import com.editorialhouse.backend.model.article.Comment;
import com.editorialhouse.backend.model.article.Rating;
import com.editorialhouse.backend.model.article.Report;
import com.editorialhouse.backend.service.article.ArticleInteractionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles/{articleId}/interactions")
public class ArticleInteractionController {

    private final ArticleInteractionService interactionService;

    public ArticleInteractionController(ArticleInteractionService interactionService) {
        this.interactionService = interactionService;
    }

    // All 4 roles can comment
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/comment")
    public ResponseEntity<Comment> addComment(@PathVariable Long articleId, @RequestBody Comment comment) {
        return new ResponseEntity<>(interactionService.addComment(articleId, comment), HttpStatus.CREATED);
    }

    // All 4 roles can rate
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/rate")
    public ResponseEntity<Rating> addRating(@PathVariable Long articleId, @RequestBody Rating rating) {
        return new ResponseEntity<>(interactionService.addRating(articleId, rating), HttpStatus.CREATED);
    }

    // Only 'USER' role can report
    @PreAuthorize("hasAuthority('REPORT_NEWS_PRIVILEGE')")
    @PostMapping("/report")
    public ResponseEntity<Report> reportArticle(@PathVariable Long articleId, @RequestBody Report report) {
        return new ResponseEntity<>(interactionService.reportArticle(articleId, report), HttpStatus.CREATED);
    }

    // Admin/Supervisor can review reports
    @PreAuthorize("hasAuthority('REVIEW_REPORT_PRIVILEGE')")
    @GetMapping("/reports")
    public ResponseEntity<List<Report>> getPendingReports() {
        return ResponseEntity.ok(interactionService.getPendingReports());
    }

    @PreAuthorize("hasAuthority('REVIEW_REPORT_PRIVILEGE')")
    @PostMapping("/reports/{reportId}/review")
    public ResponseEntity<Report> reviewReport(@PathVariable Long reportId, @RequestParam boolean actionTaken) {
        return ResponseEntity.ok(interactionService.reviewReport(reportId, actionTaken));
    }
}
