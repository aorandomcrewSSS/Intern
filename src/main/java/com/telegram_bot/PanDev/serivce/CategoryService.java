package com.telegram_bot.PanDev.serivce;

import com.telegram_bot.PanDev.model.Category;
import com.telegram_bot.PanDev.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    public void initRootCategory() {
        if (categoryRepository.findByName("Root") == null) {
            Category root = new Category("Root");
            categoryRepository.save(root);
        }
    }

    public String formatTree() {
        Category root = categoryRepository.findByName("Root");
        if (root == null) return "Дерево категорий пусто.";

        StringBuilder sb = new StringBuilder();
        formatTreeRecursive(root, sb, 0);
        return sb.toString();
    }

    private void formatTreeRecursive(Category category, StringBuilder sb, int level) {
        for (int i = 0; i < level; i++) {
            sb.append("--");
        }
        sb.append(category.getName()).append("\n");
        for (Category child : category.getChildrens()) {
            formatTreeRecursive(child, sb, level + 1);
        }
    }

    private void removeRecursive(Category category) {
        for (Category child : category.getChildrens()) {
            removeRecursive(child);
        }
        categoryRepository.delete(category);
    }

    @Transactional
    public String addElement(String parentName, String elementName) {
        Category parent = categoryRepository.findByName(parentName);
        if (parent == null) {
            return "Родительская категория не найдена.";
        }

        if (categoryRepository.findByName(elementName) != null) {
            return "Категория с таким именем уже существует.";
        }

        Category newCategory = new Category(elementName);
        parent.addChild(newCategory);
        categoryRepository.save(newCategory);
        return "Категория добавлена.";
    }


    @Transactional public String removeElement(String elementName) {
        Category category = categoryRepository.findByName(elementName);
        if (category == null) {
            return "Категория не найдена.";
        }

        removeRecursive(category);
        return "Категория удалена.";
    }

}
