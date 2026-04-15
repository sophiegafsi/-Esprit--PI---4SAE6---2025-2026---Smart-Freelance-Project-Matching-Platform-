package tn.esprit.messagerie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.messagerie.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversation_IdOrderBySentAtAsc(Long conversationId);

    List<Message> findByConversation_Id(Long conversationId);
}
