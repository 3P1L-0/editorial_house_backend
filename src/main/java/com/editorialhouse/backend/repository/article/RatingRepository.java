package com.editorialhouse.backend.repository.article;

import com.editorialhouse.backend.model.article.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByArticleIdAndUserId(Long articleId, Long userId);
}
