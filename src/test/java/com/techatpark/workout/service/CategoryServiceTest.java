package com.techatpark.workout.service;

import com.gurukulams.core.model.Categories;
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
        final Categories category = categoryService.create("hari"
                , null, anCategories());
        Assertions.assertTrue(categoryService.read("hari", category.getId(), null).isPresent(), "Created Categories");
    }

    @Test
    void createLocalized() {
        final Categories category = categoryService.create("hari"
                , Locale.GERMAN, anCategories());
        Assertions.assertTrue(categoryService.read("hari", category.getId(), Locale.GERMAN).isPresent(), "Created Localized Categories");
        Assertions.assertTrue(categoryService.read("hari", category.getId(), null).isPresent(), "Created Categories");
    }

    @Test
    void read() {
        final Categories category = categoryService.create("hari",
                null, anCategories());
        Assertions.assertTrue(categoryService.read("hari", category.getId(), null).isPresent(),
                "Created Categories");
    }

    @Test
    void update() {

        final Categories category = categoryService.create("hari",
                null, anCategories());
        Categories newCategories = new Categories();
        newCategories.setId(UUID.randomUUID().toString());
        newCategories.setTitle("HansiCategories");
        Categories updatedCategories = categoryService
                .update(category.getId(), "priya", null, newCategories);
        Assertions.assertEquals("HansiCategories", updatedCategories.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            categoryService
                    .update(UUID.randomUUID().toString(), "priya", null, newCategories);
        });
    }

    @Test
    void updateLocalized() {

        final Categories category = categoryService.create("hari",
                null, anCategories());
        Categories newCategories = new Categories();
        newCategories.setId(category.getId());
        newCategories.setTitle("HansiCategories");
        Categories updatedCategories = categoryService
                .update(category.getId(), "priya", Locale.GERMAN, newCategories);

        Assertions.assertEquals("HansiCategories", categoryService.read("mani", category.getId(), Locale.GERMAN).get().getTitle(), "Updated");
        Assertions.assertNotEquals("HansiCategories", categoryService.read("mani", category.getId(), null).get().getTitle(), "Updated");


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            categoryService
                    .update(UUID.randomUUID().toString(), "priya", null, newCategories);
        });
    }

    @Test
    void delete() {

        final Categories category = categoryService.create("hari", null,
                anCategories());
        categoryService.delete("mani", category.getId());
        Assertions.assertFalse(categoryService.read("mani", category.getId(), null).isPresent(), "Deleted Categories");
    }

    @Test
    void list() {

        final Categories category = categoryService.create("hari", null,
                anCategories());
        Categories newCategories = new Categories();
        newCategories.setId(UUID.randomUUID().toString());
        newCategories.setTitle("HansiCategories");
        categoryService.create("hari", null,
                newCategories);
        List<Categories> listofcategories = categoryService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

    }

    @Test
    void listLocalized() {

        final Categories category = categoryService.create("hari", Locale.GERMAN,
                anCategories());
        Categories newCategories = new Categories();
        newCategories.setId(UUID.randomUUID().toString());
        newCategories.setTitle("HansiCategories");
        categoryService.create("hari", null,
                newCategories);
        List<Categories> listofcategories = categoryService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

        listofcategories = categoryService.list("hari", Locale.GERMAN);
        Assertions.assertEquals(2, listofcategories.size());

    }


    /**
     * Gets practice.
     *
     * @return the practice
     */
    Categories anCategories() {
        Categories categories = new Categories();
        categories.setId(UUID.randomUUID().toString());
        categories.setTitle("HariCategories");
        return categories;
    }
}