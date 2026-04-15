package tn.esprit.messagerie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.messagerie.Message;
import tn.esprit.messagerie.Conversation;
import tn.esprit.messagerie.repository.MessageRepository;
import tn.esprit.messagerie.repository.ConversationRepository;

import java.time.LocalDateTime;

@Service
public class PerformanceSeederService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    public void seedRealisticConversation(Long clientId, Long freelancerId) {
        Conversation conv = new Conversation();
        conv.setClientId(clientId);
        conv.setFreelancerId(freelancerId);
        conv.setTitle("Performance Test Project");
        conv.setStatus("ACTIVE");
        conv.setCreatedAt(LocalDateTime.now().minusDays(2));
        conv.setLastMessageAt(LocalDateTime.now());
        conv = conversationRepository.save(conv);

        LocalDateTime baseTime = conv.getCreatedAt().plusHours(1);

        // 1. Client message
        createMsg(conv, clientId, "Hello, can you help me with a project?", baseTime);

        // 2. Freelancer reply (Fast: 15 mins)
        createMsg(conv, freelancerId, "Sure, I'd love to help! What's the scope?", baseTime.plusMinutes(15));

        // 3. Client message
        createMsg(conv, clientId, "I need a dashboard with real-time metrics.", baseTime.plusHours(2));

        // 4. Freelancer reply (Moderate: 45 mins) - Marked as Edited
        Message m4 = createMsg(conv, freelancerId, "I can do that for $500. Wait, I mean $450.",
                baseTime.plusHours(2).plusMinutes(45));
        m4.setIsEdited(true);
        messageRepository.save(m4);

        // 5. Client message
        createMsg(conv, clientId, "Great, let's start!", baseTime.plusHours(5));

        // 6. Freelancer reply (Slow: 300 mins / 5 hours) - Marked as Read
        Message m6 = createMsg(conv, freelancerId, "Awesome. I'll get started immediately.", baseTime.plusHours(10));
        m6.setIsRead(true);
        messageRepository.save(m6);
    }

    private Message createMsg(Conversation conv, Long senderId, String content, LocalDateTime sentAt) {
        Message msg = new Message();
        msg.setConversation(conv);
        msg.setSenderId(senderId);
        msg.setContent(content);
        msg.setSentAt(sentAt);
        msg.setIsRead(false);
        msg.setIsDeleted(false);
        msg.setIsEdited(false);
        msg.setIsFlagged(false);
        return messageRepository.save(msg);
    }
}
