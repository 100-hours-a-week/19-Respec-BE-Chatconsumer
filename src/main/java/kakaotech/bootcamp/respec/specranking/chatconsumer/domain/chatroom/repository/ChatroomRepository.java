package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.repository;

import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.entity.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
}
