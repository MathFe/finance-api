package com.mathfe.finance.service;

import com.mathfe.finance.dto.CategoryResponseDTO;
import com.mathfe.finance.dto.TransactionRequestDTO;
import com.mathfe.finance.dto.TransactionResponseDTO;
import com.mathfe.finance.entity.Category;
import com.mathfe.finance.entity.Transaction;
import com.mathfe.finance.entity.User;
import com.mathfe.finance.repository.CategoryRepository;
import com.mathfe.finance.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRepository transactionRepository, CategoryRepository categoryRepository) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    public TransactionResponseDTO create (TransactionRequestDTO dto,User user) {
        Category category = categoryRepository.findById(dto.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .description(dto.description())
                .amount(dto.amount())
                .type(dto.type())
                .transactionDate(dto.transactionDate())
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransactionResponseDTO(
                savedTransaction.getId(),
                savedTransaction.getDescription(),
                savedTransaction.getAmount(),
                savedTransaction.getType(),
                savedTransaction.getTransactionDate(),
                new CategoryResponseDTO(
                        savedTransaction.getCategory().getId(),
                        savedTransaction.getCategory().getName(),
                        savedTransaction.getCategory().getType(),
                        savedTransaction.getCategory().getColor(),
                        savedTransaction.getCategory().getCreatedAt()
                )
        );
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
