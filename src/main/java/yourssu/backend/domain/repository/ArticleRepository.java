package yourssu.backend.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yourssu.backend.domain.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
