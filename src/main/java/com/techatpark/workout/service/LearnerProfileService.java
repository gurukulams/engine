package com.techatpark.workout.service;

import com.gurukulams.core.GurukulamsManager;
import com.gurukulams.core.model.LearnerProfile;
import com.gurukulams.core.store.LearnerProfileStore;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.Optional;

/**
 * The type Learner profile service.
 */
@Service
public class LearnerProfileService {

    /**
     * jdbcClient.
     */
    private final LearnerProfileStore learnerProfileStore;

    /**
     * Instantiates a new Learner profile service.
     *
     * @param gurukulamsManager the jdbc client
     */
    public LearnerProfileService(
            final GurukulamsManager gurukulamsManager) {
        this.learnerProfileStore =
                gurukulamsManager.getLearnerProfileStore();
    }

    /**
     * Create learner profile.
     *
     * @param learnerProfile the learner profile
     * @return the learner profile
     */
    public LearnerProfile create(final LearnerProfile learnerProfile)
            throws SQLException {
        return this.learnerProfileStore.insert()
                .values(learnerProfile).returning();
    }

    /**
     * Read optional.
     *
     * @param userHandle the user handle
     * @return the optional
     */
    public Optional<LearnerProfile> read(final String userHandle)
            throws SQLException {
            return this.learnerProfileStore.select(userHandle);

    }
}
