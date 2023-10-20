package com.techatpark.workout.service;

import com.gurukulams.core.model.Tag;
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
        Assertions.assertTrue(tagService.read("hari", tag.getId(), null).isPresent(), "Created Tag");
    }

    @Test
    void createLocalized() {
        final Tag tag = tagService.create("hari"
                , Locale.GERMAN, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.getId(), Locale.GERMAN).isPresent(), "Created Localized Tag");
        Assertions.assertTrue(tagService.read("hari", tag.getId(), null).isPresent(), "Created Tag");
    }

    @Test
    void read() {
        final Tag tag = tagService.create("hari",
                null, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.getId(), null).isPresent(),
                "Created Tag");
    }

    @Test
    void update() {

        final Tag tag = tagService.create("hari",
                null, anTag());
        Tag newTag = new Tag();
        newTag.setId(tag.getId());
         newTag.setTitle("HansiTag");
        Tag updatedTag = tagService
                .update(tag.getId(), "priya", null, newTag);
        Assertions.assertEquals("HansiTag", updatedTag.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tagService
                    .update(UUID.randomUUID().toString(), "priya", null, newTag);
        });
    }

    @Test
    void updateLocalized() {

        final Tag tag = tagService.create("hari",
                null, anTag());
        Tag newTag = new Tag();
        newTag.setId(tag.getId());
        newTag.setTitle("HansiTag");
        Tag updatedTag = tagService
                .update(tag.getId(), "priya", Locale.GERMAN, newTag);

        Assertions.assertEquals("HansiTag", tagService.read("mani", tag.getId(), Locale.GERMAN).get().getTitle(), "Updated");
        Assertions.assertNotEquals("HansiTag", tagService.read("mani", tag.getId(), null).get().getTitle(), "Updated");


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tagService
                    .update(UUID.randomUUID().toString(), "priya", null, newTag);
        });
    }

    @Test
    void delete() {

        final Tag tag = tagService.create("hari", null,
                anTag());
        tagService.delete("mani", tag.getId());
        Assertions.assertFalse(tagService.read("mani", tag.getId(), null).isPresent(), "Deleted Tag");
    }

    @Test
    void list() {

        final Tag tag = tagService.create("hari", null,
                anTag());
        Tag newTag = new Tag();
        newTag.setId(UUID.randomUUID().toString());
        newTag.setTitle("HansiTag");
        tagService.create("hari", null,
                newTag);
        List<Tag> listofcategories = tagService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

    }

    @Test
    void listLocalized() {

        final Tag tag = tagService.create("hari", Locale.GERMAN,
                anTag());
        Tag newTag = new Tag();
        newTag.setId(UUID.randomUUID().toString());
        newTag.setTitle("HansiTag");
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

        Tag tag = new Tag();
        tag.setId(UUID.randomUUID().toString());
        tag.setTitle("HariTag");
        return tag;
    }


}