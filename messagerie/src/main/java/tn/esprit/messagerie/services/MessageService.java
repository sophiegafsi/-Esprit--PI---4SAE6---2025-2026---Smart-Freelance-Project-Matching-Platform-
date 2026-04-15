package tn.esprit.messagerie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.messagerie.Message;
import tn.esprit.messagerie.Conversation;
import tn.esprit.messagerie.repository.MessageRepository;
import tn.esprit.messagerie.repository.ConversationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private ContentValidatorService contentValidatorService;

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

    public List<Message> getMessagesByConversation(Long conversationId) {
        return messageRepository.findByConversation_IdOrderBySentAtAsc(conversationId);
    }

    public Message sendMessage(Message message, Long conversationId) {
        if (conversationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Conversation ID cannot be null");
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Conversation not found with id " + conversationId));

        message.setConversation(conversation);
        message.setSentAt(LocalDateTime.now());
        message.setIsRead(false);
        message.setIsDeleted(false);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        logger.info("Processing message for conversation {}: {}", conversationId, message.getContent());

        // Content validation
        if (contentValidatorService.containsForbiddenContent(message.getContent())) {
            logger.warn("Forbidden content detected in message to conversation {}", conversationId);
            message.setIsFlagged(true);
            message.setContent(contentValidatorService.maskForbiddenContent(message.getContent()));
        } else {
            message.setIsFlagged(false);
        }

        return messageRepository.save(message);
    }

    private static final int EDIT_WINDOW_MINUTES = 30;

    public Message updateMessage(Long id, Message messageDetails) {
        Message message = messageRepository.findById(id)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found with id " + id));


        LocalDateTime now = LocalDateTime.now();
        if (message.getSentAt().plusMinutes(EDIT_WINDOW_MINUTES).isBefore(now)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Message edit window has expired (30 minutes)");
        }

        if (Boolean.TRUE.equals(message.getIsRead())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot edit a message that has already been read");
        }

        if (messageDetails.getContent() != null && !messageDetails.getContent().equals(message.getContent())) {
            message.setIsEdited(true);
        }

        message.setContent(messageDetails.getContent());
        message.setType(messageDetails.getType());
        message.setIsRead(messageDetails.getIsRead());
        message.setIsDeleted(messageDetails.getIsDeleted());

        if (contentValidatorService.containsForbiddenContent(message.getContent())) {
            message.setIsFlagged(true);
            message.setContent(contentValidatorService.maskForbiddenContent(message.getContent()));
        } else {
            message.setIsFlagged(false);
        }

        return messageRepository.save(message);
    }

    public void deleteMessage(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id " + id));
        messageRepository.delete(message);
    }
}
