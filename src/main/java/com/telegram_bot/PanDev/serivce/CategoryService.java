package com.telegram_bot.PanDev.serivce;

import com.telegram_bot.PanDev.model.Category;
import com.telegram_bot.PanDev.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
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

    public byte[] exportCategoriesToExcel() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Categories");

            // Создаем заголовок
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("ID");
            headerRow.createCell(1).setCellValue("Name");
            headerRow.createCell(2).setCellValue("Parent ID");

            // Заполняем данные категорий
            List<Category> categories = categoryRepository.findAll();
            int rowNum = 1;
            for (Category category : categories) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(category.getId());
                row.createCell(1).setCellValue(category.getName());

                // Проверяем, что parent не равен null
                Long parentId = category.getParent() != null ? category.getParent().getId() : null;
                if (parentId != null) {
                    row.createCell(2).setCellValue(parentId);
                } else {
                    row.createCell(2).setBlank();
                }
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Transactional
    public String importCategoriesFromExcel(InputStream excelData) {
        try (Workbook workbook = WorkbookFactory.create(excelData)) {
            Sheet sheet = workbook.getSheetAt(0);

            Map<Long, Category> categoriesById = new HashMap<>();
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                Long id = (long) row.getCell(0).getNumericCellValue();
                String name = row.getCell(1).getStringCellValue();
                Long parentId = row.getCell(2) != null ? (long) row.getCell(2).getNumericCellValue() : null;

                Category category = new Category(name);
                categoriesById.put(id, category);
                if (parentId != null) {
                    Category parent = categoriesById.get(parentId);
                    parent.addChild(category);
                }
                categoryRepository.save(category);
            }
            return "Импорт завершен.";
        } catch (Exception e) {
            log.error("Error during import: " + e.getMessage());
            return "Ошибка импорта.";
        }
    }

}
