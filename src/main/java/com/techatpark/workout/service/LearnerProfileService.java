package com.techatpark.workout.service;

import com.techatpark.workout.model.LearnerProfile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * The type Learner profile service.
 */
@Service
public class LearnerProfileService {

    /**
     * Index.
     */
    private static final int INDEX_1 = 1;
    /**
     * Index.
     */
    private static final int INDEX_2 = 2;
    /**
     * Index.
     */
    private static final int INDEX_3 = 3;
    /**
     * Index.
     */
    private static final int INDEX_4 = 4;

    /**
     * learner_profile table.
     */
    private static final String LEARNER_PROFILE_TABLE = "learner_profile";

    /**
     * jdbcClient.
     */
    private final JdbcClient jdbcClient;

    /**
     * Instantiates a new Learner profile service.
     *
     * @param aJdbcClient the jdbc client
     */
    public LearnerProfileService(final JdbcClient aJdbcClient) {
        this.jdbcClient = aJdbcClient;
    }

    private LearnerProfile rowMapper(final ResultSet rs, final Integer rowNum)
            throws SQLException {
        return new LearnerProfile(
                rs.getString(INDEX_1),
                rs.getString(INDEX_2),
                rs.getString(INDEX_3),
                rs.getObject(INDEX_4, LocalDate.class)
        );
    }

    /**
     * Create learner profile.
     *
     * @param learnerProfile the learner profile
     * @return the learner profile
     */
    public LearnerProfile create(final LearnerProfile learnerProfile) {
        final String insertLearnerProfileQuery = """
                INSERT INTO %s(user_handle, first_name, last_name, dob)
                VALUES (?, ?, ?, ?)
                """.formatted(LEARNER_PROFILE_TABLE);

        jdbcClient.sql(insertLearnerProfileQuery)
                .param(INDEX_1, learnerProfile.userHandle())
                .param(INDEX_2, learnerProfile.firstName())
                .param(INDEX_3, learnerProfile.lastName())
                .param(INDEX_4, learnerProfile.dob())
                .update();

        return read(learnerProfile.userHandle()).get();
    }

    /**
     * Read optional.
     *
     * @param userHandle the user handle
     * @return the optional
     */
    public Optional<LearnerProfile> read(final String userHandle) {
        final String selectLearnerProfileQuery = """
                SELECT user_handle, first_name, last_name, dob
                FROM %s
                WHERE user_handle = ?
                """.formatted(LEARNER_PROFILE_TABLE);

        try {
            return jdbcClient.sql(selectLearnerProfileQuery)
                    .param(INDEX_1, userHandle)
                    .query(this::rowMapper)
                    .optional();
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
