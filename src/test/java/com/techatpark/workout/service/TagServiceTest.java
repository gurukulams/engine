package com.techatpark.workout.service;

import com.gurukulams.core.model.Tags;
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
        final Tags tag = tagService.create("hari"
                , null, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.getId(), null).isPresent(), "Created Tags");
    }

    @Test
    void createLocalized() {
        final Tags tag = tagService.create("hari"
                , Locale.GERMAN, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.getId(), Locale.GERMAN).isPresent(), "Created Localized Tags");
        Assertions.assertTrue(tagService.read("hari", tag.getId(), null).isPresent(), "Created Tags");
    }

    @Test
    void read() {
        final Tags tag = tagService.create("hari",
                null, anTag());
        Assertions.assertTrue(tagService.read("hari", tag.getId(), null).isPresent(),
                "Created Tags");
    }

    @Test
    void update() {

        final Tags tag = tagService.create("hari",
                null, anTag());
        Tags newTag = new Tags();
        newTag.setId(tag.getId());
         newTag.setTitle("HansiTag");
        Tags updatedTag = tagService
                .update(tag.getId(), "priya", null, newTag);
        Assertions.assertEquals("HansiTag", updatedTag.getTitle(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tagService
                    .update(UUID.randomUUID().toString(), "priya", null, newTag);
        });
    }

    @Test
    void updateLocalized() {

        final Tags tag = tagService.create("hari",
                null, anTag());
        Tags newTag = new Tags();
        newTag.setId(tag.getId());
        newTag.setTitle("HansiTag");
        Tags updatedTag = tagService
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

        final Tags tag = tagService.create("hari", null,
                anTag());
        tagService.delete("mani", tag.getId());
        Assertions.assertFalse(tagService.read("mani", tag.getId(), null).isPresent(), "Deleted Tags");
    }

    @Test
    void list() {

        final Tags tag = tagService.create("hari", null,
                anTag());
        Tags newTag = new Tags();
        newTag.setId(UUID.randomUUID().toString());
        newTag.setTitle("HansiTag");
        tagService.create("hari", null,
                newTag);
        List<Tags> listofcategories = tagService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

    }

    @Test
    void listLocalized() {

        final Tags tag = tagService.create("hari", Locale.GERMAN,
                anTag());
        Tags newTag = new Tags();
        newTag.setId(UUID.randomUUID().toString());
        newTag.setTitle("HansiTag");
        tagService.create("hari", null,
                newTag);
        List<Tags> listofcategories = tagService.list("hari", null);
        Assertions.assertEquals(2, listofcategories.size());

        listofcategories = tagService.list("hari", Locale.GERMAN);
        Assertions.assertEquals(2, listofcategories.size());

    }


    /**
     * Gets practice.
     *
     * @return the practice
     */
    Tags anTag() {

        Tags tag = new Tags();
        tag.setId(UUID.randomUUID().toString());
        tag.setTitle("HariTag");
        return tag;
    }


}