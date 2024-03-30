package com.study.ch02_6.repository;

import com.study.ch02_6.MessageText;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepository extends CrudRepository<MessageText, Long> {

}