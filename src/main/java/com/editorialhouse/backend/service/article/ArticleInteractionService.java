package com.editorialhouse.backend.service.article;

import com.editorialhouse.backend.model.User;
import com.editorialhouse.backend.model.article.*;
import com.editorialhouse.backend.repository.article.ArticleRepository;
import com.editorialhouse.backend.repository.article.CommentRepository;
import com.editorialhouse.backend.repository.article.RatingRepository;
import com.editorialhouse.backend.repository.article.ReportRepository;
import com.editorialhouse.backend.service.CustomUserDetailsService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArticleInteractionService {

    private final ArticleRepository articleRepository;
    private final RatingRepository ratingRepository;
    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final CustomUserDetailsService userDetailsService;

    public ArticleInteractionService(ArticleRepository articleRepository, RatingRepository ratingRepository, CommentRepository commentRepository, ReportRepository reportRepository, CustomUserDetailsService userDetailsService) {
        this.articleRepository = articleRepository;
        this.ratingRepository = ratingRepository;
        this.commentRepository = commentRepository;
        this.reportRepository = reportRepository;
        this.userDetailsService = userDetailsService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // This is a simplification; in a real app, we'd fetch the full User object
        // from the repository based on the username.
        return (User) userDetailsService.loadUserByUsername(userDetails.getUsername());
    }

    @Transactional
    public Comment addComment(Long articleId, Comment comment) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        User currentUser = getCurrentUser();

        if (!article.isPublished()) {
            throw new IllegalStateException("Cannot comment on an unpublished article.");
        }

        comment.setArticle(article);
        comment.setUser(currentUser);
        return commentRepository.save(comment);
    }

    @Transactional
    public Rating addRating(Long articleId, Rating rating) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        User currentUser = getCurrentUser();

        if (!article.isPublished()) {
            throw new IllegalStateException("Cannot rate an unpublished article.");
        }

        // Check if user has already rated
        ratingRepository.findByArticleIdAndUserId(articleId, currentUser.getId()).ifPresent(r -> {
            throw new IllegalStateException("You have already rated this article.");
        });

        if (rating.getScore() < 1 || rating.getScore() > 5) {
            throw new IllegalArgumentException("Rating score must be between 1 and 5.");
        }

        rating.setArticle(article);
        rating.setUser(currentUser);
        return ratingRepository.save(rating);
    }

    @Transactional
    public Report reportArticle(Long articleId, Report report) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        User currentUser = getCurrentUser();

        if (!article.isPublished()) {
            throw new IllegalStateException("Cannot report an unpublished article.");
        }

        report.setArticle(article);
        report.setReporter(currentUser);
        return reportRepository.save(report);
    }

    public List<Report> getPendingReports() {
        return reportRepository.findByReviewedFalse();
    }

    @Transactional
    public Report reviewReport(Long reportId, boolean actionTaken) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setReviewed(true);
        report.setActionTaken(actionTaken);
        return reportRepository.save(report);
    }
}
