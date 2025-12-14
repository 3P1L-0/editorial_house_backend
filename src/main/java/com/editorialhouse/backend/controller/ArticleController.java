package com.editorialhouse.backend.controller;

import com.editorialhouse.backend.model.article.Article;
import com.editorialhouse.backend.service.article.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // Public endpoint: Web Users may only read published articles
    @GetMapping("/published")
    public ResponseEntity<List<Article>> getPublishedArticles() {
        return ResponseEntity.ok(articleService.getAllPublishedArticles());
    }

    // Clerk/Admin: Writes, edits, and publishes articles (WRITE_PRIVILEGE)
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PostMapping
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        return new ResponseEntity<>(articleService.createArticle(article), HttpStatus.CREATED);
    }

    // Clerk/Admin: Edits own articles (handled by service layer)
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        return ResponseEntity.ok(articleService.updateArticle(id, article));
    }

    // Clerk: Submits for approval
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PostMapping("/{id}/submit")
    public ResponseEntity<Article> submitForApproval(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.submitForApproval(id));
    }

    // Admin/Supervisor: Approves/Rejects articles (APPROVE_ARTICLE_PRIVILEGE)
    @PreAuthorize("hasAuthority('APPROVE_ARTICLE_PRIVILEGE')")
    @GetMapping("/pending")
    public ResponseEntity<List<Article>> getArticlesForApproval() {
        return ResponseEntity.ok(articleService.getArticlesForApproval());
    }

    @PreAuthorize("hasAuthority('APPROVE_ARTICLE_PRIVILEGE')")
    @PostMapping("/{id}/approve")
    public ResponseEntity<Article> approveArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.approveArticle(id));
    }

    @PreAuthorize("hasAuthority('APPROVE_ARTICLE_PRIVILEGE')")
    @PostMapping("/{id}/reject")
    public ResponseEntity<Article> rejectArticle(@PathVariable Long id, @RequestBody String reason) {
        return ResponseEntity.ok(articleService.rejectArticle(id, reason));
    }

    // Admin/Clerk: Publishes approved articles (PUBLISH_PRIVILEGE)
    @PreAuthorize("hasAuthority('PUBLISH_PRIVILEGE')")
    @PostMapping("/{id}/publish")
    public ResponseEntity<Article> publishArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.publishArticle(id));
    }

    // Admin/Supervisor: Removes news from the journal (DELETE_ANY_ARTICLE_PRIVILEGE)
    @PreAuthorize("hasAuthority('DELETE_ANY_ARTICLE_PRIVILEGE')")
    @PostMapping("/{id}/unpublish")
    public ResponseEntity<Article> unpublishArticle(@PathVariable Long id) {
        return ResponseEntity.ok(articleService.unpublishArticle(id));
    }

    // Article creator: Deletes own article (handled by service layer)
    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')") // Assuming only those who can write can delete their own
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }
}
