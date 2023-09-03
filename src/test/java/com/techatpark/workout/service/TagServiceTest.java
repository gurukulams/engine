package com.techatpark.workout.service;

import com.techatpark.workout.model.Tag;
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
class TagServiceTest {

    @Autowired
    private TagService tagService;

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
        tagService.deleteAll();
    }


    @Test
    void create() {
        final Tag tag = tagService.create("hari"
                , null, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.id(), null).isPresent(), "Created Tag");
    }

    @Test
    void createLocalized() {
        final Tag tag = tagService.create("hari"
                , Locale.GERMAN, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.id(), Locale.GERMAN).isPresent(), "Created Localized Tag");
        Assertions.assertTrue(tagService.read("hari", tag.id(), null).isPresent(), "Created Tag");
    }

    @Test
    void read() {
        final Tag tag = tagService.create("hari",
                null, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.id(), null).isPresent(),
                "Created Tag");
    }

    @Test
    void update() {

        final Tag tag = tagService.create("hari",
                null, anTag());
        Tag newTag = new Tag(tag.id(), "HansiTag", null, null, null, null);
        Tag updatedTag = tagService
                .update(tag.id(), "priya", null, newTag);
        Assertions.assertEquals("HansiTag", updatedTag.title(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tagService
                    .update(UUID.randomUUID().toString(), "priya", null, newTag);
        });
    }

    @Test
    void updateLocalized() {

        final Tag tag = tagService.create("hari",
                null, anTag());
        Tag newTag = new Tag(tag.id(), "HansiTag", null, null, null, null);
        Tag updatedTag = tagService
                .update(tag.id(), "priya", Locale.GERMAN, newTag);

        Assertions.assertEquals("HansiTag", tagService.read("mani", tag.id(), Locale.GERMAN).get().title(), "Updated");
        Assertions.assertNotEquals("HansiTag", tagService.read("mani", tag.id(), null).get().title(), "Updated");


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tagService
                    .update(UUID.randomUUID().toString(), "priya", null, newTag);
        });
    }

    @Test
    void delete() {

        final Tag tag = tagService.create("hari", null,
                anTag());
        tagService.delete("mani", tag.id());
        Assertions.assertFalse(tagService.read("mani", tag.id(), null).isPresent(), "Deleted Tag");
    }

    @Test
    void list() {

        final Tag tag = tagService.create("hari", null,
                anTag());
        Tag newTag = new Tag(UUID.randomUUID().toString(), "HansiTag", null, null, null, null);
        tagService.create("hari", null,
                newTag);
        List<Tag> listofcategories = tagService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

    }

    @Test
    void listLocalized() {

        final Tag tag = tagService.create("hari", Locale.GERMAN,
                anTag());
        Tag newTag = new Tag(UUID.randomUUID().toString(), "HansiTag", null, null, null, null);
        tagService.create("hari", null,
                newTag);
        List<Tag> listofcategories = tagService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

        listofcategories = tagService.list("hari", Locale.GERMAN);
        Assertions.assertEquals(2, listofcategories.size());

    }


    /**
     * Gets practice.
     *
     * @return the practice
     */
    Tag anTag() {

        Tag tag = new Tag(UUID.randomUUID().toString(), "HariTag", null, null, null, null);
        return tag;
    }


}