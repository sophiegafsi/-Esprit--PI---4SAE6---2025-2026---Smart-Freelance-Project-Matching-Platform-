package tn.esprit.messagerie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.esprit.messagerie.Conversation;
import tn.esprit.messagerie.Message;
import tn.esprit.messagerie.repository.ConversationRepository;
import tn.esprit.messagerie.repository.MessageRepository;

import java.time.LocalDateTime;

@Component
public class TestDataInitializer implements CommandLineRunner {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public void run(String... args) throws Exception {
        if (conversationRepository.count() == 0) {
            System.out.println(">>> Initializing Test Data for Messaging Service...");

            // Conversation 1: Design Project
            Conversation conv1 = new Conversation();
            conv1.setTitle("Logo Design for Startup");
            conv1.setProjectId(101L);
            Long user1 = 1L;
            Long user2 = 2L;
            conv1.setClientId(user1);
            conv1.setFreelancerId(user2);
            conv1.setStatus("ACTIVE");
            conv1.setCreatedAt(LocalDateTime.now().minusDays(2));
            conv1.setLastMessageAt(LocalDateTime.now().minusHours(1));
            conv1.setIsArchived(false);
            conv1.setIsDeleted(false);
            conv1 = conversationRepository.save(conv1);

            // Messages for Conversation 1
            createMessage(conv1, user1, "Hello! I saw your portfolio and I love it.", LocalDateTime.now().minusDays(1));
            createMessage(conv1, user2, "Thank you! I'd love to help with your logo.",
                    LocalDateTime.now().minusHours(2));
            createMessage(conv1, user1, "Great! My phone is 99123456.", LocalDateTime.now().minusHours(1));

            // Conversation 2: Backend Development
            Conversation conv2 = new Conversation();
            conv2.setTitle("Spring Boot API Development");
            conv2.setProjectId(102L);
            Long user3 = 3L;
            Long user4 = 4L;
            conv2.setClientId(user3);
            conv2.setFreelancerId(user4);
            conv2.setStatus("ACTIVE");
            conv2.setCreatedAt(LocalDateTime.now().minusDays(5));
            conv2.setLastMessageAt(LocalDateTime.now());
            conv2.setIsArchived(false);
            conv2.setIsDeleted(false);
            conv2 = conversationRepository.save(conv2);

            // Messages for Conversation 2
            createMessage(conv2, user3, "Can you move to WhatsApp?", LocalDateTime.now().minusMinutes(30));
            createMessage(conv2, user4, "I prefer to stay on platform for payment safety.",
                    LocalDateTime.now().minusMinutes(10));

            System.out.println(">>> Test Data Initialization Complete.");
        }
    }

    private void createMessage(Conversation conversation, Long senderId, String content, LocalDateTime sentAt) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setSentAt(sentAt);
        message.setIsRead(true);
        message.setIsDeleted(false);
        message.setIsEdited(false);
        message.setType("text");
        message.setIsFlagged(false);

        messageRepository.save(message);
    }
}
