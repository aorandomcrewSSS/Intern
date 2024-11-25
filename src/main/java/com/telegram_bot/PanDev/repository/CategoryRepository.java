package com.telegram_bot.PanDev.repository;

import com.telegram_bot.PanDev.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с сущностью Category в базе данных.
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByName(String name);
}
