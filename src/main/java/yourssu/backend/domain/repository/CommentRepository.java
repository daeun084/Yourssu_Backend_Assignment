package yourssu.backend.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yourssu.backend.domain.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
