package com.mathfe.finance.service;

import com.mathfe.finance.dto.CategoryResponseDTO;
import com.mathfe.finance.dto.TransactionResponseDTO;
import com.mathfe.finance.entity.Category;
import com.mathfe.finance.entity.Transaction;
import com.mathfe.finance.entity.User;
import com.mathfe.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }


    public List<TransactionResponseDTO> listByCategory(User user, Category category) {
        List<Transaction> transactions = transactionRepository.findByUserAndCategory(user, category);

        return transactions.stream()
                .map(transaction -> new TransactionResponseDTO(
                        transaction.getId(),
                        transaction.getDescription(),
                        transaction.getAmount(),
                        transaction.getType(),
                        transaction.getTransactionDate(),
                        new CategoryResponseDTO(
                                transaction.getCategory().getId(),
                                transaction.getCategory().getName(),
                                transaction.getCategory().getType(),
                                transaction.getCategory().getColor(),
                                transaction.getCategory().getCreatedAt()
                        )
                ))
                .toList();

    }
}
