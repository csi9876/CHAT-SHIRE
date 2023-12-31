package com.ssafy.backend.domain.chat.service;

import com.ssafy.backend.domain.attachedFile.AttachedFile;
import com.ssafy.backend.domain.attachedFile.Category;
import com.ssafy.backend.domain.attachedFile.dto.AttachedFileInfo;
import com.ssafy.backend.domain.attachedFile.repository.AttachedFileRepository;
import com.ssafy.backend.domain.chat.dto.ChatInfo;
import com.ssafy.backend.domain.chat.dto.ChatInfoResponse;
import com.ssafy.backend.domain.chat.dto.ChatPost;
import com.ssafy.backend.domain.chat.repository.ChatRepository;
import com.ssafy.backend.domain.task.dto.ReferenceChatInfo;
import com.ssafy.backend.domain.task.repository.ReferenceRepository;
import com.ssafy.backend.domain.user.service.ChallengeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.ssafy.backend.domain.common.GlobalMethod.getUserId;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, ChatInfo> chatRedisTemplate;
    private final AttachedFileRepository attachedFileRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChallengeService challengeService;
    private final ReferenceRepository referenceRepository;
    private final String chatKey = "chat";

    public void postChat(ChatPost chatPost) {
        Long chatRoomId = chatPost.getChatRoomId();

        // Redis에서 값을 가져옴
        String chatNumberKey = "chatNumber";
        Object chatNumberValue = redisTemplate.opsForValue().get(chatNumberKey + chatRoomId);

        // chatNumberValue가 null인 경우 "0"으로 설정
        String strChatNumber = (chatNumberValue == null) ? "0" : chatNumberValue.toString();

        // 문자열을 Long으로 변환
        Long chatNumber = Long.valueOf(strChatNumber);
        boolean attachFlag = false;
        List<AttachedFileInfo> attachedFileInfos = new ArrayList<>();
        // 첨부파일 검증
        if (chatPost.getAttachedFileInfos() != null) {
            for (int idx = 0; idx < chatPost.getAttachedFileInfos().size(); idx++) {
                // 카테고리 븐류
                String str = chatPost.getAttachedFileInfos().get(idx).getUrl();
                Category category;
                if (str.endsWith(".jpg") || str.endsWith(".jpeg") || str.endsWith(".png")) {
                    category = Category.IMAGE;
                } else if (str.endsWith(".mp4")) {
                    category = Category.VIDEO;
                } else if (str.endsWith(".pdf") || str.endsWith(".docx") || str.endsWith(".doc")
                        || str.endsWith(".xlsx") || str.endsWith(".xls") || str.endsWith(".txt")) {
                    category = Category.FILE;
                } else continue;
                attachFlag = true;
                // 첨부파일 DB에 저장
                AttachedFile attachedFile = AttachedFile.builder()
                        .url(chatPost.getAttachedFileInfos().get(idx).getUrl())
                        .thumbnail(chatPost.getAttachedFileInfos().get(idx).getThumbnail())
                        .chatRoomId(chatRoomId)
                        .chatNumber(chatNumber)
                        .category(category).build();
                attachedFileRepository.save(attachedFile);
                attachedFileInfos.add(new AttachedFileInfo(attachedFile.getUrl(), attachedFile.getThumbnail()));
            }

            if (attachFlag) {
                challengeService.addLink(getUserId());
            }
        }

        ChatInfo chatInfo = ChatInfo.builder()
                .userId(getUserId())
                .content(chatPost.getContent())
                .chatTime(LocalDateTime.now())
                .chatNumber(chatNumber)
                .isAttached(attachFlag).build();

        redisTemplate.opsForValue().set(chatNumberKey + chatRoomId, chatNumber + 1L);
        chatRedisTemplate.opsForList().rightPush(chatKey + chatRoomId, chatInfo);

        simpMessagingTemplate.convertAndSend("/topic/greetings/" + chatRoomId, ChatInfoResponse.fromChatInfo(chatInfo, attachedFileInfos));

        // SSE
    }

    public List<ChatInfoResponse> getChats(Long chatRoomId, int page, int size) {
        // sql 먼저 조회
        List<ChatInfo> chatSQL = chatRepository.findInfoByChatRoomId(chatRoomId);
        chatSQL = chatSQL == null ? new ArrayList<>() : chatSQL;
        // redis 조회
        List<ChatInfo> chatRedis = chatRedisTemplate.opsForList().range(chatKey + chatRoomId, 0, -1);
        chatRedis = chatRedis == null ? new ArrayList<>() : chatRedis;
        // 둘이 합쳐서 주기 sql + redis
        // TODO - 페이지네이션
        chatSQL.addAll(chatRedis);
        // 첨부파일 있는애들 첨부파일 붙여주기
        return chatSQL.stream().map(chatInfo -> chatAttached(chatInfo, chatRoomId)).collect(Collectors.toList());
    }

    public List<ChatInfoResponse> getReferenceChats(Long referenceId) {
        // 시간 없으니깐 채팅 전부 조회 -> 번호 찾아서 그 이후로 짤라주기
        ReferenceChatInfo referenceChatInfo = referenceRepository.findReferenceChatById(referenceId);
        // sql 먼저 조회
        List<ChatInfo> chatSQL = chatRepository.findInfoByChatRoomId(referenceChatInfo.getChatRoomID());
        chatSQL = chatSQL == null ? new ArrayList<>() : chatSQL;
        // redis 조회
        List<ChatInfo> chatRedis = chatRedisTemplate.opsForList().range(chatKey + referenceChatInfo.getChatRoomID(), 0, -1);
        chatRedis = chatRedis == null ? new ArrayList<>() : chatRedis;
        // 둘이 합쳐서 주기 sql + redis
        chatSQL.addAll(chatRedis);
        List<ChatInfo> findChats = new ArrayList<>();
        // 여기서 chatNumber 맞는거 찾아서 10개 담기
        for (int idx = 0; idx < chatSQL.size(); idx++) {
            if (Objects.equals(chatSQL.get(idx).getChatNumber(), referenceChatInfo.getChatNumber())) {
                findChats = chatSQL.subList(idx, Math.min(idx + 10, chatSQL.size()));
                break;
            }
        }

        // 첨부파일 있는애들 첨부파일 붙여주기
        return findChats.stream().map(chatInfo -> chatAttached(chatInfo, referenceChatInfo.getChatRoomID())).collect(Collectors.toList());
    }


    public ChatInfoResponse chatAttached(ChatInfo chatInfo, Long chatRoomId) {
        if (chatInfo.getIsAttached()) {
            List<AttachedFileInfo> attachedFileInfos = attachedFileRepository.findByChatRoomIdAndChatNumber(chatRoomId, chatInfo.getChatNumber());
            return ChatInfoResponse.fromChatInfo(chatInfo, attachedFileInfos);
        } else {
            return ChatInfoResponse.fromChatInfo(chatInfo, new ArrayList<>());
        }
    }
}
