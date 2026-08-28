package com.mathfe.finance.service;

import com.mathfe.finance.dto.CategoryRequestDTO;
import com.mathfe.finance.dto.CategoryResponseDTO;
import com.mathfe.finance.entity.Category;
import com.mathfe.finance.entity.User;
import com.mathfe.finance.repository.CategoryRepository;
import com.mathfe.finance.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponseDTO  create(CategoryRequestDTO dto, User user) {

        Category category = Category.builder()
                .user(user)
                .name(dto.name())
                .type(dto.type())
                .color(dto.color())
                .build();

        Category savedCategory = categoryRepository.save(category);

        return new CategoryResponseDTO(
                savedCategory.getId(),
                savedCategory.getName(),
                savedCategory.getType(),
                savedCategory.getColor(),
                savedCategory.getCreatedAt()
        );
    }

    public List<CategoryResponseDTO> listByUser(User user) {
        List<Category> categories = categoryRepository.findByUser(user);

        return categories.stream()
                .map(category -> new CategoryResponseDTO(
                        category.getId(),
                        category.getName(),
                        category.getType(),
                        category.getColor(),
                        category.getCreatedAt()
                ))
                .toList();


    }




}
