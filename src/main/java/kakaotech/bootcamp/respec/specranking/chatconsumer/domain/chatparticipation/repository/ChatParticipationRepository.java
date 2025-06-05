package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatparticipation.repository;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatparticipation.entity.ChatParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipationRepository extends JpaRepository<ChatParticipation, Long> {
}
