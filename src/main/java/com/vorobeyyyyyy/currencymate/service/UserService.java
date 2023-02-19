package com.vorobeyyyyyy.currencymate.service;

import java.math.BigDecimal;

import com.vorobeyyyyyy.currencymate.model.User;
import com.vorobeyyyyyy.currencymate.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateUserSalary(long chatId, BigDecimal salary) {
        User user = userRepository.findByChatId(chatId)
                .orElseGet(User::new);
        user.setChatId(chatId);
        user.setSalary(salary);
        userRepository.save(user);
    }
}
