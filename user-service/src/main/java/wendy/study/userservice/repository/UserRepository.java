package wendy.study.userservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wendy.study.userservice.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
