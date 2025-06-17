package kakaotech.bootcamp.respec.specranking.chatconsumer.domain.chatroom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import kakaotech.bootcamp.respec.specranking.chatconsumer.domain.common.BaseTimeEntity;
import lombok.Getter;

@Entity
@Getter
public class Chatroom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "BIGINT UNSIGNED")
    private Long id;
}
