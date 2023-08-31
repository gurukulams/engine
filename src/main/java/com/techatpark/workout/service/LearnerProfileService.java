package com.techatpark.workout.service;

import com.techatpark.workout.model.LearnerProfile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LearnerProfileService {


    /**
     * this is the connection for the database.
     */
    private final DataSource dataSource;
    /**
     * this helps to execute sql queries.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * this is the constructor.
     *
     * @param anDataSource
     * @param anJdbcTemplate
     */
    public LearnerProfileService(final DataSource anDataSource,
                                 final JdbcTemplate anJdbcTemplate) {
        this.dataSource = anDataSource;
        this.jdbcTemplate = anJdbcTemplate;
    }

    /**
     * Maps the data from and to the database.
     *
     * @param rs
     * @param rowNum
     * @return p
     * @throws SQLException
     */
    private LearnerProfile rowMapper(final ResultSet rs,
                                     final Integer rowNum)
            throws SQLException {

        return new LearnerProfile(rs.getString(
                "user_handle"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getObject("dob", LocalDate.class)
        );
    }


    /**
     * @param learnerProfile
     * @return learnerProfile
     */
    public LearnerProfile create(final LearnerProfile learnerProfile) {

        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("learner_profile")
                .usingColumns("user_handle",
                        "first_name",
                        "last_name", "dob");
        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("user_handle", learnerProfile.userHandle());
        valueMap.put("first_name", learnerProfile.firstName());
        valueMap.put("last_name", learnerProfile.lastName());
        valueMap.put("dob", learnerProfile.dob());

        insert.execute(valueMap);

        final Optional<LearnerProfile> createdLearner =
                read(learnerProfile.userHandle());

        return createdLearner.get();
    }

    /**
     * @param userHandle
     * @return LearnerProfile
     */
    public Optional<LearnerProfile> read(final String userHandle) {
        final String query = "SELECT user_handle,first_name,last_name,dob"
                + " FROM learner_profile WHERE user_handle = ?";

        try {
            final LearnerProfile p = jdbcTemplate.queryForObject(query,
                    this::rowMapper, userHandle);
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
