package com.study.ch02_6;

import com.study.ch02_6.configuration.SpringDataConfiguration;
import com.study.ch02_6.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpringDataConfiguration.class})
public class HelloWorldJPATest {

    @Autowired
    private MessageRepository messageRepository;

    @Test
    public void storeLoadMessage() {
        MessageText messageText = new MessageText();
        messageText.setText("Hello World from Spring Data JPA!");

        messageRepository.save(messageText);

        List<MessageText> messageTexts = (List<MessageText>) messageRepository.findAll();

        assertAll(
                () -> assertEquals(1, messageTexts.size()),
                () -> assertEquals("Hello World from Spring Data JPA!", messageTexts.get(0).getText())
        );

    }

}