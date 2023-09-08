package com.techatpark.workout.service;

import com.techatpark.workout.model.AuthProvider;
import com.techatpark.workout.model.Handle;
import com.techatpark.workout.model.Learner;
import com.techatpark.workout.starter.security.payload.SignupRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Learner service.
 */
@Service
public class LearnerService {

    /**
     * Logger Facade.
     */
    private final Logger logger =
            LoggerFactory.getLogger(LearnerService.class);

    /**
     * this is the connection for the database.
     */
    private final DataSource dataSource;
    /**
     * this helps to execute sql queries.
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * Bean Validator.
     */
    private final Validator validator;

    /**
     * this is the constructor.
     *
     * @param anDataSource
     * @param anJdbcTemplate
     * @param pValidator
     */
    public LearnerService(final DataSource anDataSource,
                          final JdbcTemplate anJdbcTemplate,
                          final Validator
                                  pValidator) {
        this.dataSource = anDataSource;
        this.jdbcTemplate = anJdbcTemplate;
        this.validator = pValidator;
    }

    /**
     * Maps the data from and to the database.
     *
     * @param rs
     * @param rowNum
     * @return p
     * @throws SQLException
     */
    private Learner rowMapper(final ResultSet rs,
                              final Integer rowNum)
            throws SQLException {
        return new Learner(
                rs.getString("user_handle"),
                rs.getString("email"),
                rs.getString("pword"),
                rs.getString("image_url"),
                AuthProvider.valueOf(rs.getString("provider")),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getObject("modified_at", LocalDateTime.class)
        );
    }

    /**
     * Sigup an User.
     *
     * @param signUpRequest
     * @param encoderFunction
     */
    @Transactional
    public void signUp(final SignupRequest signUpRequest,
                       final Function<String, String> encoderFunction) {
        Set<ConstraintViolation<SignupRequest>> violations =
                validator.validate(signUpRequest);
        if (violations.isEmpty()) {
            String userHandle = signUpRequest.getEmail().split("@")[0];
            Optional<Handle> handle = createHandle(userHandle);
            if (handle.isPresent()) {
                create(
                    new Learner(userHandle, signUpRequest.getEmail(),
                        encoderFunction.apply(signUpRequest.getPassword()),
                        signUpRequest.getImageUrl(),
                        signUpRequest.getAuthProvider(), null, null));
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<SignupRequest>
                    constraintViolation : violations) {
                sb.append(constraintViolation.getMessage());
            }
            throw new ConstraintViolationException("Error occurred: "
                    + sb.toString(), violations);
        }

    }

    /**
     * @param learner
     * @return learner
     */
    private Learner create(final Learner learner) {
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("learner")
                .usingColumns("user_handle", "email",
                        "pword",
                        "provider", "image_url");
        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("email", learner.email());
        valueMap.put("pword", learner.password());
        valueMap.put("image_url", learner.imageUrl());
        valueMap.put("provider", learner.provider().toString());
        String userHandle = learner.userHandle();
        valueMap.put("user_handle", userHandle);
        insert.execute(valueMap);

        final Optional<Learner> createdLearner = read(userHandle);
        logger.info("Created learner {}", userHandle);
        return createdLearner.get();
    }

    /**
     * @param userHandle
     * @return learner
     */
    public Optional<Learner> read(final String userHandle) {
        final String query =  "SELECT user_handle,email,pword,image_url,"
                + "provider"
                + ",created_at, modified_at"
                + " FROM learner WHERE user_handle = ?";

        try {
            final Learner p = jdbcTemplate.queryForObject(query,
                     this::rowMapper, userHandle);
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * @param email
     * @return learner
     */
    public Optional<Learner> readByEmail(final String email) {
        final String query =  "SELECT user_handle,email,pword,image_url,"
                + "provider"
                + ",created_at, modified_at"
                + " FROM learner WHERE email = ?";

        try {
            final Learner p = jdbcTemplate.queryForObject(query,
                    this::rowMapper, email);
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * @param userHandle
     * @param learner
     * @return learner
     */
    public Learner update(final String userHandle,
                          final Learner learner) {
        logger.debug("Entering updating from learner {}", userHandle);
        final String query = "UPDATE learner SET email=?,provider=?,"
                + "pword=?,image_url=? WHERE user_handle=?";
        final int updatedRows = jdbcTemplate.update(query,
                learner.email(), learner.provider().toString(),
                learner.password(),
                learner.imageUrl(), userHandle);
        if (updatedRows == 0) {
            logger.error("Learner not found to update {}", userHandle);
            throw new IllegalArgumentException("Learner not found");
        }
        return read(userHandle).get();
    }


    private Optional<Handle> createHandle(final String userHandle) {
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
            .withTableName("handle")
            .usingColumns("user_handle", "type");
        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("user_handle", userHandle);
        valueMap.put("type", "Learner");

        insert.execute(valueMap);

        return readHandle(userHandle);
    }

    private Optional<Handle> readHandle(final String userHandle) {
        final String query = "SELECT user_handle,type,created_at"
            + " FROM handle WHERE user_handle = ?";

            final Handle p = jdbcTemplate.queryForObject(query,
                this::rowMapperHandle, userHandle);
            return Optional.of(p);

    }

    /**
     * Deletes Learners.
     */
    public void delete() {
        jdbcTemplate.update("DELETE FROM LEARNER_PROFILE");
        jdbcTemplate.update("DELETE FROM learner");
        jdbcTemplate.update("DELETE FROM HANDLE WHERE type='Learner'");

    }

    private Handle rowMapperHandle(final ResultSet resultSet,
        final int i)
        throws SQLException {
        return new Handle(resultSet.getString("user_handle"),
            resultSet.getString("type"),
            resultSet.getObject("created_at", LocalDateTime.class)
        );
    }
}
