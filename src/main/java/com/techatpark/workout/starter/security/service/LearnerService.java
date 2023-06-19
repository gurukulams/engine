package com.techatpark.workout.starter.security.service;


import com.techatpark.workout.starter.security.payload.AuthProvider;
import com.techatpark.workout.starter.security.payload.Learner;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

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

        Learner learner = new Learner((UUID)
                rs.getObject("id"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("image_url"),
                AuthProvider.valueOf(rs.getString("provider")),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getString("created_by"),
                rs.getObject("modified_at", LocalDateTime.class),
                rs.getString("modified_by")
        );
        return learner;
    }

    /**
     * Sigup an User.
     *
     * @param signUpRequest
     * @param encoderFunction
     */
    public void signUp(final SignupRequest signUpRequest,
                       final Function<String, String> encoderFunction) {
        Set<ConstraintViolation<SignupRequest>> violations =
                validator.validate(signUpRequest);
        if (violations.isEmpty()) {
            create("System",
                    new Learner(null, signUpRequest.getEmail(),
                            encoderFunction.apply(signUpRequest.getPassword()),
                            signUpRequest.getImageUrl(),
                            AuthProvider.local, null, null,
                            null, null));
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
     * @param userName
     * @param learner
     * @return learner
     */
    public Learner create(final String userName,
                          final Learner learner) {
        final SimpleJdbcInsert insert = new SimpleJdbcInsert(dataSource)
                .withTableName("learner")
                .usingColumns("id", "email",
                        "password",
                        "provider", "image_url", "created_by");
        final Map<String, Object> valueMap = new HashMap<>();
        valueMap.put("email", learner.email());
        valueMap.put("password", learner.password());
        valueMap.put("image_url", learner.imageUrl());
        valueMap.put("provider", learner.provider().toString());
        valueMap.put("created_by", userName);
        final UUID learnerId = UUID.randomUUID();
        valueMap.put("id", learnerId);
        insert.execute(valueMap);

        final Optional<Learner> createdLearner = read(userName,
                learnerId);
        logger.info("Created learner {}", learnerId);
        return createdLearner.get();
    }

    /**
     * @param userName
     * @param id
     * @return learner
     */
    public Optional<Learner> read(final String userName,
                                  final UUID id) {
        final String query = "SELECT id,email,password,image_url,provider"
                + ",created_by,created_at, modified_at, modified_by"
                + " FROM learner WHERE id = ?";

        try {
            final Learner p = jdbcTemplate.queryForObject(query,
                     this::rowMapper, id);
            return Optional.of(p);
        } catch (final EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * @param userName
     * @param email
     * @return learner
     */
    public Optional<Learner> readByEmail(final String userName,
                                         final String email) {
        final String query = "SELECT id,email,password,image_url,provider"
                + ",created_by,created_at, modified_at, modified_by"
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
     * @param id
     * @param userName
     * @param learner
     * @return learner
     */
    public Learner update(final UUID id,
                          final String userName,
                          final Learner learner) {
        logger.debug("Entering updating from learner {}", id);
        final String query = "UPDATE learner SET email=?,provider=?,"
                + "password=?,image_url=?,modified_by=? WHERE id=?";
        final Integer updatedRows = jdbcTemplate.update(query,
                learner.email(), learner.provider().toString(),
                learner.password(),
                learner.imageUrl(), userName, id);
        if (updatedRows == 0) {
            logger.error("update not found", id);
            throw new IllegalArgumentException("Learner not found");
        }
        return read(userName, id).get();
    }

    /**
     * @param id
     * @param userName the userName
     * @return learner
     */
    public Boolean delete(final String userName,
                          final UUID id) {
        final String query = "DELETE FROM learner WHERE id = ?";
        final Integer updatedRows = jdbcTemplate.update(query, id);
        return !(updatedRows == 0);
    }

    /**
     * @param userName
     * @return learner
     */
    public List<Learner> list(final String userName) {
        final String query = "SELECT id,email,password,image_url,provider,"
                + "created_by,created_at,modified_by,modified_at FROM learner";
        return jdbcTemplate.query(query, this::rowMapper);
    }

    /**
     * @return learner
     */
    public Integer deleteAll() {
        jdbcTemplate.update("DELETE FROM LEARNER_PROFILE");
        jdbcTemplate.update("DELETE FROM HANDLE WHERE type='Learner'");
        return jdbcTemplate.update("DELETE FROM learner");
    }
}
