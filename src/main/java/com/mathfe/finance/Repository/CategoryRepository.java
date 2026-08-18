package com.mathfe.finance.Repository;

import com.mathfe.finance.Entity.Category;
import com.mathfe.finance.Entity.CategoryType;
import com.mathfe.finance.Entity.User;
import org.aspectj.weaver.ast.And;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUser(User user);

    List<Category> findByUserAndType( User user, CategoryType categoryType);
}
