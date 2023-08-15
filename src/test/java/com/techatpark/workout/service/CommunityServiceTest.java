package com.techatpark.workout.service;

import com.techatpark.workout.model.Community;
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
class CommunityServiceTest {

    @Autowired
    private CommunityService communityService;

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
        communityService.deleteAll();
    }


    @Test
    void create() {
        final Community community = communityService.create("hari"
                , null, newCommunity());
        Assertions.assertTrue(communityService.read("hari", community.id(), null).isPresent(), "Created community");
    }


    /**
     * Gets practice.
     *
     * @return the practice
     */
    Community newCommunity() {

        Community community = new Community(UUID.randomUUID().toString(), "Haricommunity", null, null, null, null);
        return community;
    }


    @Test
    void createLocalized() {
        final Community community = communityService.create("hari"
                , Locale.GERMAN, newCommunity());
        Assertions.assertTrue(communityService.read("hari", community.id(), Locale.GERMAN).isPresent(), "Created Localized community");
        Assertions.assertTrue(communityService.read("hari", community.id(), null).isPresent(), "Created community");
    }
//
    @Test
    void read() {
        final Community community = communityService.create("hari",
                null, newCommunity());
        Assertions.assertTrue(communityService.read("hari", community.id(), null).isPresent(),
                "Created community");
    }

    @Test
    void update() {

        final Community community = communityService.create("hari",
                null, newCommunity());
        Community newCommunity = new Community(community.id(), "HansiCommunity", null, null, null, null);
        Community updatedCommunity = communityService
                .update(community.id(), "priya", null, newCommunity);
        Assertions.assertEquals("HansiCommunity", updatedCommunity.title(), "Updated");

        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            communityService
                    .update(UUID.randomUUID().toString(), "priya", null, newCommunity);
        });
    }

    @Test
    void updateLocalized() {

        final Community community = communityService.create("hari",
                null, newCommunity());
        Community newCommunity = new Community(community.id(), "HansiCommunity", null, null, null, null);
        Community updatedCommunity = communityService
                .update(community.id(), "priya", Locale.GERMAN, newCommunity);

        Assertions.assertEquals("HansiCommunity", communityService.read("mani", community.id(), Locale.GERMAN).get().title(), "Updated");
        Assertions.assertNotEquals("HansiCommunity", communityService.read("mani", community.id(), null).get().title(), "Updated");


        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            communityService
                    .update(UUID.randomUUID().toString(), "priya", null, newCommunity);
        });
    }
//
    @Test
    void delete() {

        final Community community = communityService.create("hari", null,
                newCommunity());
        communityService.delete("mani", community.id());
        Assertions.assertFalse(communityService.read("mani", community.id(), null).isPresent(), "Deleted community");
    }

    @Test
    void list() {

        final Community community = communityService.create("hari", null,
                newCommunity());
        Community newCommunity = new Community(UUID.randomUUID().toString(), "HansiCommunity", null, null, null, null);
        communityService.create("hari", null,
            newCommunity);
        List<Community> listOfCommunities = communityService.list("hari", null);
        Assertions.assertEquals(2, listOfCommunities.size());

    }

    @Test
    void listLocalized() {

        final Community community = communityService.create("hari", Locale.GERMAN,
                newCommunity());
        Community newCommunity = new Community(UUID.randomUUID().toString(), "HansiCommunity", null, null, null, null);
        communityService.create("hari", null,
            newCommunity);
        List<Community> listOfCommunities = communityService.list("hari", null);
        Assertions.assertEquals(2, listOfCommunities.size());

        listOfCommunities = communityService.list("hari", Locale.GERMAN);
        Assertions.assertEquals(2, listOfCommunities.size());

    }

}