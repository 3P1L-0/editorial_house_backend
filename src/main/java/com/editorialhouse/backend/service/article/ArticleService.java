package com.editorialhouse.backend.service.article;

import com.editorialhouse.backend.model.User;
import com.editorialhouse.backend.model.article.Article;
import com.editorialhouse.backend.model.article.ArticleStatus;
import com.editorialhouse.backend.repository.article.ArticleRepository;
import com.editorialhouse.backend.service.CustomUserDetailsService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CustomUserDetailsService userDetailsService;

    public ArticleService(ArticleRepository articleRepository, CustomUserDetailsService userDetailsService) {
        this.articleRepository = articleRepository;
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

    public List<Article> getAllPublishedArticles() {
        return articleRepository.findByPublishedTrue();
    }

    public List<Article> getArticlesForApproval() {
        // Only Admins and Supervisors can see articles pending approval
        // This check should be done at the controller level with @PreAuthorize
        return articleRepository.findAll().stream()
                .filter(article -> article.getStatus() == ArticleStatus.PENDING_APPROVAL)
                .toList();
    }

    @Transactional
    public Article createArticle(Article article) {
        User currentUser = getCurrentUser();
        // Only Admin and Clerk have WRITE_PRIVILEGE by default
        // The security check is done at the controller level
        article.setAuthor(currentUser);
        article.setCreationDate(new Date());
        article.setLastModifiedDate(new Date());
        article.setStatus(ArticleStatus.DRAFT);
        return articleRepository.save(article);
    }

    @Transactional
    public Article updateArticle(Long id, Article updatedArticle) {
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        User currentUser = getCurrentUser();
        // Only the author can update their article
        if (!existingArticle.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to update this article.");
        }

        // Only update if the article is not published or pending approval
        if (existingArticle.getStatus() == ArticleStatus.PUBLISHED || existingArticle.getStatus() == ArticleStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Cannot update an article that is published or pending approval.");
        }

        existingArticle.setTitle(updatedArticle.getTitle());
        existingArticle.setContent(updatedArticle.getContent());
        existingArticle.setLastModifiedDate(new Date());
        // Other fields like image/audio/video URLs should also be updated

        return articleRepository.save(existingArticle);
    }

    @Transactional
    public void deleteArticle(Long id) {
        Article existingArticle = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        User currentUser = getCurrentUser();
        // An article can only be deleted by its creator.
        if (!existingArticle.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this article.");
        }

        articleRepository.delete(existingArticle);
    }

    @Transactional
    public Article submitForApproval(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        User currentUser = getCurrentUser();
        // Only the author can submit their article
        if (!article.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to submit this article.");
        }

        if (article.getStatus() == ArticleStatus.DRAFT || article.getStatus() == ArticleStatus.REJECTED) {
            article.setStatus(ArticleStatus.PENDING_APPROVAL);
            article.setRejectionReason(null); // Clear rejection reason on resubmission
            return articleRepository.save(article);
        } else {
            throw new IllegalStateException("Article is not in a state to be submitted for approval.");
        }
    }

    @Transactional
    public Article approveArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        // Security check for APPROVE_ARTICLE_PRIVILEGE is done at the controller level

        if (article.getStatus() == ArticleStatus.PENDING_APPROVAL) {
            article.setStatus(ArticleStatus.APPROVED);
            return articleRepository.save(article);
        } else {
            throw new IllegalStateException("Article is not pending approval.");
        }
    }

    @Transactional
    public Article rejectArticle(Long id, String reason) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        // Security check for APPROVE_ARTICLE_PRIVILEGE is done at the controller level

        if (article.getStatus() == ArticleStatus.PENDING_APPROVAL) {
            article.setStatus(ArticleStatus.REJECTED);
            article.setRejectionReason(reason);
            // In a real app, we would inform the Clerk of the reason here (e.g., email)
            return articleRepository.save(article);
        } else {
            throw new IllegalStateException("Article is not pending approval.");
        }
    }

    @Transactional
    public Article publishArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        // Security check for PUBLISH_PRIVILEGE is done at the controller level

        if (article.getStatus() == ArticleStatus.APPROVED) {
            article.setStatus(ArticleStatus.PUBLISHED);
            article.setPublished(true);
            return articleRepository.save(article);
        } else {
            throw new IllegalStateException("Article is not approved and cannot be published.");
        }
    }

    @Transactional
    public Article unpublishArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));

        // Security check for DELETE_ANY_ARTICLE_PRIVILEGE is done at the controller level

        if (article.isPublished()) {
            article.setPublished(false);
            // Optionally change status back to APPROVED or DRAFT
            return articleRepository.save(article);
        } else {
            throw new IllegalStateException("Article is not published.");
        }
    }
}
