# 💬 Respec Chat Consumer Service

Kafka를 활용한 실시간 채팅 메시지 처리 시스템으로, Spring Boot 기반으로 구현한 마이크로서비스입니다.

## 관련 블로그 글

1. [테스트 코드 작성을 통한 리팩토링과 코드 품질 개선기](https://aole.tistory.com/174)

2. [Kafka Consumer를 활용한 채팅 릴레이 시스템 구현기](https://aole.tistory.com/172)

## 📋 개요

스펙랭킹 플랫폼의 채팅 시스템에서 Kafka를 통해 전송되는 메시지를 소비하고 처리하는 서비스입니다. 채팅 메시지를 데이터베이스에 저장하고, 사용자에게 알림을 전송하며, 중복 처리 방지 및 에러 처리를 담당합니다.

## ✨ 주요 기능

- **Kafka 메시지 소비**: `@KafkaListener`를 통한 채팅 메시지 실시간 처리
- **채팅 메시지 저장**: MySQL을 활용한 채팅 이력 영구 보관
- **메시지 전달**: WebClient를 통한 메인 서버로의 메시지 릴레이
- **알림 시스템**: 사용자에게 알림 전송
- **중복 처리 방지**: Redis 기반 Idempotency 키를 통한 중복 메시지 처리 방지
- **사용자 서버 위치 추적**: Redis를 활용한 사용자별 서버 위치 정보 관리
- **채팅방 자동 생성**: 존재하지 않는 채팅방 자동 생성 기능

## 🎯 핵심 구현 사항

### Kafka 메시지 처리

```java

@KafkaListener(topics = "chat", containerFactory = "chatMessageContainerFactory")
public void handleChatMessage(ChatConsumeEvent chatDto) {
    // 중복 처리 방지를 위한 Idempotency 키 검증
    // 채팅 메시지 저장 및 채팅방 자동 생성
    // 메시지 릴레이 또는 알림 전송
}
```

### 도메인 중심 설계

- **Chat Domain**: 채팅 메시지 관련 비즈니스 로직
- **Notification Domain**: 알림 시스템 관리
- **User Domain**: 사용자 및 서버 위치 관리
- **Chatroom Domain**: 채팅방 생성 및 관리
- **ChatParticipation Domain**: 채팅 참여자 관리

### 서비스 분리 구조

- **ChatConsumeService**: Kafka 메시지 수신 및 조합
- **ChatService**: 채팅 메시지 저장 로직
- **ChatDeliveryService**: 메시지 전달 및 알림 관리
- **ChatRelayService**: 외부 서버로의 메시지 릴레이
- **NotificationService**: 푸시 알림 전송

## 🧩 시스템 구성도

```
Kafka Topic "chat" 
       ↓
ChatConsumeService (@KafkaListener)
       ↓
┌─ ChatService (메시지 저장)
│  └─ ChatRepository → MySQL
└─ ChatDeliveryService (메시지 전달)
   ├─ ChatRelayService → WebClient (다른 서버)
   └─ NotificationService → WebClient (알림 서버)
```

## 📄 프로젝트 구조

```
src/main/java/kakaotech/bootcamp/respec/specranking/chatconsumer/
├── ChatconsumerApplication.java    # 메인 애플리케이션
├── domain/                         # 도메인 계층
│   ├── chat/                      # 채팅 도메인
│   │   ├── adapter/in/           # Kafka Consumer
│   │   │   ├── ChatConsumeService.java
│   │   │   ├── Event/            # 이벤트 객체
│   │   │   ├── mapping/          # 매핑 유틸
│   │   │   └── exception/        # 어댑터 예외
│   │   ├── service/              # 비즈니스 로직
│   │   │   ├── ChatService.java
│   │   │   ├── ChatDeliveryService.java
│   │   │   └── ChatRelayService.java
│   │   ├── entity/               # JPA 엔티티
│   │   ├── repository/           # 데이터 접근
│   │   ├── dto/                  # 데이터 전송 객체
│   │   └── constant/             # 상수 관리
│   ├── notification/             # 알림 도메인
│   ├── user/                     # 사용자 도메인
│   ├── chatroom/                 # 채팅방 도메인
│   └── chatparticipation/        # 채팅 참여 도메인
└── global/                        # 글로벌 설정
    ├── common/                   # 공통 설정
    └── infrastructure/           # 인프라 계층
        ├── kafka/               # Kafka 설정
        ├── redis/               # Redis 설정 및 서비스
        └── myserver/            # 헬스체크
```

## 🔍 주요 특징

### 중복 처리 방지

Redis 기반 Idempotency 서비스를 통해 3분간 동일한 메시지의 중복 처리를 방지합니다.

### 에러 처리

메시지 처리 중 오류 발생 시 Idempotency 키를 삭제하여 재처리가 가능하도록 구현했습니다.

### 채팅방 자동 관리

존재하지 않는 채팅방에 메시지가 전송될 경우 자동으로 채팅방을 생성합니다.

### 리액티브 HTTP 통신

WebClient를 활용하여 논블로킹 방식의 외부 서버 통신을 구현했습니다.

## 📚 추가 자료

- **메인 서버**: [19-Respec-BE](https://github.com/100-hours-a-week/19-Respec-BE/tree/main) - BackEnd 메인 서버 레포지토리

---

*"실제 운영 환경을 고려한 안정적인 메시지 처리 시스템을 구축했습니다."*

