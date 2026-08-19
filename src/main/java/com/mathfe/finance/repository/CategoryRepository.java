package com.mathfe.finance.repository;

import com.mathfe.finance.entity.Category;
import com.mathfe.finance.entity.CategoryType;
import com.mathfe.finance.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUser(User user);

    List<Category> findByUserAndType( User user, CategoryType categoryType);
}
