package com.telegram_bot.PanDev.repository;

import com.telegram_bot.PanDev.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
}
