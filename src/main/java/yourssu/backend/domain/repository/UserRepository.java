package yourssu.backend.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yourssu.backend.domain.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsUserByUsername(String username);

    Boolean existsUserByEmail(String email);

    Optional<User> findUserByEmail(String email);
}
