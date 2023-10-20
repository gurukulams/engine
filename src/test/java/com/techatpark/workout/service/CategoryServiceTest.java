package com.techatpark.workout.service;

import com.gurukulams.core.model.Category;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@SpringBootTest
class CategoryServiceTest {


    @Autowired
    private CategoryService categoryService;

    /**
     * Before.
     *
     * @throws IOException the io exception
     */
    @BeforeEach
    void before() throws IOException {
        cleanUp();
    }

    /**
     * After.
     */
    @AfterEach
    void after() {
        cleanUp();
    }

    private void cleanUp() {
        categoryService.deleteAll();
    }


    @Test
    void create() {
        final Category category = categoryService.create("hari"
                , null, anCategory());
        Assertions.assertTrue(categoryService.read("hari", category.getId(), null).isPresent(), "Created Category");
    }

    @Test
    void createLocalized() {
        final Category category = categoryService.create("hari"
                , Locale.GERMAN, anCategory());
        Assertions.assertTrue(categoryService.read("hari", category.getId(), Locale.GERMAN).isPresent(), "Created Localized Category");
        Assertions.assertTrue(categoryService.read("hari", category.getId(), null).isPresent(), "Created Category");
    }

    @Test
    void read() {
        final Category category = categoryService.create("hari",
                null, anCategory());
        Assertions.assertTrue(categoryService.read("hari", category.getId(), null).isPresent(),
                "Created Category");
    }

    @Test
    void update() {

        final Category category = categoryService.create("hari",
                null, anCategory());
        Category newCategory = new Category();
        newCategory.setId(UUID.randomUUID().toString());
        newCategory.setTitle("HansiCategory");
        Category updatedCategory = categoryService
                .update(category.getId(), "priya", null, newCategory);
        Assertions.assertEquals("HansiCategory", updatedCategory.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            categoryService
                    .update(UUID.randomUUID().toString(), "priya", null, newCategory);
        });
    }

    @Test
    void updateLocalized() {

        final Category category = categoryService.create("hari",
                null, anCategory());
        Category newCategory = new Category();
        newCategory.setId(category.getId());
        newCategory.setTitle("HansiCategory");
        Category updatedCategory = categoryService
                .update(category.getId(), "priya", Locale.GERMAN, newCategory);

        Assertions.assertEquals("HansiCategory", categoryService.read("mani", category.getId(), Locale.GERMAN).get().getTitle(), "Updated");
        Assertions.assertNotEquals("HansiCategory", categoryService.read("mani", category.getId(), null).get().getTitle(), "Updated");


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            categoryService
                    .update(UUID.randomUUID().toString(), "priya", null, newCategory);
        });
    }

    @Test
    void delete() {

        final Category category = categoryService.create("hari", null,
                anCategory());
        categoryService.delete("mani", category.getId());
        Assertions.assertFalse(categoryService.read("mani", category.getId(), null).isPresent(), "Deleted Category");
    }

    @Test
    void list() {

        final Category category = categoryService.create("hari", null,
                anCategory());
        Category newCategory = new Category();
        newCategory.setId(UUID.randomUUID().toString());
        newCategory.setTitle("HansiCategory");
        categoryService.create("hari", null,
                newCategory);
        List<Category> listofcategories = categoryService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

    }

    @Test
    void listLocalized() {

        final Category category = categoryService.create("hari", Locale.GERMAN,
                anCategory());
        Category newCategory = new Category();
        newCategory.setId(UUID.randomUUID().toString());
        newCategory.setTitle("HansiCategory");
        categoryService.create("hari", null,
                newCategory);
        List<Category> listofcategories = categoryService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

        listofcategories = categoryService.list("hari", Locale.GERMAN);
        Assertions.assertEquals(2, listofcategories.size());

    }


    /**
     * Gets practice.
     *
     * @return the practice
     */
    Category anCategory() {
        Category categories = new Category();
        categories.setId(UUID.randomUUID().toString());
        categories.setTitle("HariCategory");
        return categories;
    }
}