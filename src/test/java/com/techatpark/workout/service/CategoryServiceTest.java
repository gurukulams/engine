package com.techatpark.workout.service;

import com.techatpark.workout.model.Category;
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
        Assertions.assertTrue(categoryService.read("hari", category.id(), null).isPresent(), "Created Category");
    }

    @Test
    void createLocalized() {
        final Category category = categoryService.create("hari"
                , Locale.GERMAN, anCategory());
        Assertions.assertTrue(categoryService.read("hari", category.id(), Locale.GERMAN).isPresent(), "Created Localized Category");
        Assertions.assertTrue(categoryService.read("hari", category.id(), null).isPresent(), "Created Category");
    }

    @Test
    void read() {
        final Category category = categoryService.create("hari",
                null, anCategory());
        Assertions.assertTrue(categoryService.read("hari", category.id(), null).isPresent(),
                "Created Category");
    }

    @Test
    void update() {

        final Category category = categoryService.create("hari",
                null, anCategory());
        Category newCategory = new Category(category.id(), "HansiCategory", null, null, null, null);
        Category updatedCategory = categoryService
                .update(category.id(), "priya", null, newCategory);
        Assertions.assertEquals("HansiCategory", updatedCategory.title(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            categoryService
                    .update(UUID.randomUUID().toString(), "priya", null, newCategory);
        });
    }

    @Test
    void updateLocalized() {

        final Category category = categoryService.create("hari",
                null, anCategory());
        Category newCategory = new Category(category.id(), "HansiCategory", null, null, null, null);
        Category updatedCategory = categoryService
                .update(category.id(), "priya", Locale.GERMAN, newCategory);

        Assertions.assertEquals("HansiCategory", categoryService.read("mani", category.id(), Locale.GERMAN).get().title(), "Updated");
        Assertions.assertNotEquals("HansiCategory", categoryService.read("mani", category.id(), null).get().title(), "Updated");


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            categoryService
                    .update(UUID.randomUUID().toString(), "priya", null, newCategory);
        });
    }

    @Test
    void delete() {

        final Category category = categoryService.create("hari", null,
                anCategory());
        categoryService.delete("mani", category.id());
        Assertions.assertFalse(categoryService.read("mani", category.id(), null).isPresent(), "Deleted Category");
    }

    @Test
    void list() {

        final Category category = categoryService.create("hari", null,
                anCategory());
        Category newCategory = new Category(UUID.randomUUID().toString(), "HansiCategory", null, null, null, null);
        categoryService.create("hari", null,
                newCategory);
        List<Category> listofcategories = categoryService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

    }

    @Test
    void listLocalized() {

        final Category category = categoryService.create("hari", Locale.GERMAN,
                anCategory());
        Category newCategory = new Category(UUID.randomUUID().toString(), "HansiCategory", null, null, null, null);
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

        Category category = new Category(UUID.randomUUID().toString(), "HariCategory", null, null, null, null);
        return category;
    }
}