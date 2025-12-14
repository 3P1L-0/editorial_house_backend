package com.editorialhouse.backend.repository.article;

import com.editorialhouse.backend.model.article.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findByPublishedTrue();
    List<Article> findByAuthorId(Long authorId);
}
