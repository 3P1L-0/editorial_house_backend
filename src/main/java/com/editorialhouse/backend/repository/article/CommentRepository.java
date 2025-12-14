package com.editorialhouse.backend.repository.article;

import com.editorialhouse.backend.model.article.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
