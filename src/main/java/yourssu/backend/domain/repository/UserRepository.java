package yourssu.backend.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yourssu.backend.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
