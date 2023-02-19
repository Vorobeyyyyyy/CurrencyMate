package com.vorobeyyyyyy.currencymate.repository;

import java.util.Optional;

import com.vorobeyyyyyy.currencymate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByChatId(long chatId);
}
