package com.lms.ccrp.service;

import com.lms.ccrp.entity.RewardTransaction;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.RewardTransactionRepository;
import com.lms.ccrp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final UserRepository userRepository;
    private final RewardTransactionRepository transactionRepository;

    public void addPoints(Long userId, int points) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setTotalPoints(user.getTotalPoints() + points);
        userRepository.save(user);

        RewardTransaction tx = new RewardTransaction();
        tx.setPoints(points);
        tx.setType("ADD");
        tx.setTimestamp(LocalDateTime.now());
        tx.setUser(user);
        transactionRepository.save(tx);
    }

    public void redeemPoints(Long userId, int points) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getTotalPoints() < points) {
            throw new IllegalArgumentException("Not enough points");
        }

        user.setTotalPoints(user.getTotalPoints() - points);
        userRepository.save(user);

        RewardTransaction tx = new RewardTransaction();
        tx.setPoints(points);
        tx.setType("REDEEM");
        tx.setTimestamp(LocalDateTime.now());
        tx.setUser(user);
        transactionRepository.save(tx);
    }

    public int getUserPoints(Long userId) {
        return userRepository.findById(userId).orElseThrow().getTotalPoints();
    }

    public List<RewardTransaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
}
