package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.repository;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
