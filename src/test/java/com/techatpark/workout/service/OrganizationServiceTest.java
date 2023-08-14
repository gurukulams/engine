package com.techatpark.workout.service;

import com.techatpark.workout.model.Organization;
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
class OrganizationServiceTest {

    @Autowired
    private OrganizationService organizationService;

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
        organizationService.deleteAll();
    }


    @Test
    void create() {
        final Organization tag = organizationService.create("hari"
                , null, anTag());
        Assertions.assertTrue(organizationService.read("hari", tag.id(), null).isPresent(), "Created Organization");
    }


    /**
     * Gets practice.
     *
     * @return the practice
     */
    Organization anTag() {

        Organization tag = new Organization(UUID.randomUUID().toString(), "HariTag", null, null, null, null);
        return tag;
    }


    @Test
    void createLocalized() {
        final Organization tag = organizationService.create("hari"
                , Locale.GERMAN, anTag());
        Assertions.assertTrue(organizationService.read("hari", tag.id(), Locale.GERMAN).isPresent(), "Created Localized Organization");
        Assertions.assertTrue(organizationService.read("hari", tag.id(), null).isPresent(), "Created Organization");
    }
//
    @Test
    void read() {
        final Organization tag = organizationService.create("hari",
                null, anTag());
        Assertions.assertTrue(organizationService.read("hari", tag.id(), null).isPresent(),
                "Created Organization");
    }

    @Test
    void update() {

        final Organization tag = organizationService.create("hari",
                null, anTag());
        Organization newTag = new Organization(tag.id(), "HansiTag", null, null, null, null);
        Organization updatedTag = organizationService
                .update(tag.id(), "priya", null, newTag);
        Assertions.assertEquals("HansiTag", updatedTag.title(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            organizationService
                    .update(UUID.randomUUID().toString(), "priya", null, newTag);
        });
    }

    @Test
    void updateLocalized() {

        final Organization tag = organizationService.create("hari",
                null, anTag());
        Organization newTag = new Organization(tag.id(), "HansiTag", null, null, null, null);
        Organization updatedTag = organizationService
                .update(tag.id(), "priya", Locale.GERMAN, newTag);

        Assertions.assertEquals("HansiTag", organizationService.read("mani", tag.id(), Locale.GERMAN).get().title(), "Updated");
        Assertions.assertNotEquals("HansiTag", organizationService.read("mani", tag.id(), null).get().title(), "Updated");


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            organizationService
                    .update(UUID.randomUUID().toString(), "priya", null, newTag);
        });
    }
//
    @Test
    void delete() {

        final Organization tag = organizationService.create("hari", null,
                anTag());
        organizationService.delete("mani", tag.id());
        Assertions.assertFalse(organizationService.read("mani", tag.id(), null).isPresent(), "Deleted Organization");
    }

    @Test
    void list() {

        final Organization tag = organizationService.create("hari", null,
                anTag());
        Organization newTag = new Organization(UUID.randomUUID().toString(), "HansiTag", null, null, null, null);
        organizationService.create("hari", null,
                newTag);
        List<Organization> listOfOrganizations = organizationService.list("hari", null);
        Assertions.assertEquals(2, listOfOrganizations.size());

    }

    @Test
    void listLocalized() {

        final Organization tag = organizationService.create("hari", Locale.GERMAN,
                anTag());
        Organization newTag = new Organization(UUID.randomUUID().toString(), "HansiTag", null, null, null, null);
        organizationService.create("hari", null,
                newTag);
        List<Organization> listOfOrganizations = organizationService.list("hari", null);
        Assertions.assertEquals(2, listOfOrganizations.size());

        listOfOrganizations = organizationService.list("hari", Locale.GERMAN);
        Assertions.assertEquals(2, listOfOrganizations.size());

    }

}