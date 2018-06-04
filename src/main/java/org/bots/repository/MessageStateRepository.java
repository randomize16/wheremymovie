package org.bots.repository;

import org.bots.model.datebase.MessageState;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageStateRepository extends MongoRepository<MessageState, Long> {

    MessageState getByChatIdAndMessageId(Long chatId, Integer messageId);

}
