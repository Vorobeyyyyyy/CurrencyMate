package com.vorobeyyyyyy.currencymate.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.vorobeyyyyyy.currencymate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByChatId(long chatId);

    List<User> findAllByLastDailyMessageDateBefore(LocalDate localDate);
}
