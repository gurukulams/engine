package com.techatpark.workout;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.lang.syntax.elements.GivenClassesConjunction;
import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchUnitTest {

    static JavaClasses allClasses = null;

    private static final GivenClassesConjunction SERVICE_CLASSES = classes()
            .that()
            .areAnnotatedWith(Service.class);

    @BeforeAll
    public static void setUp() {
        allClasses = new ClassFileImporter().importPackages("com"
                + ".techatpark.workout");
    }

    @Test
    void servicesAreSecureByDesign() {
        ArchRule no_access_to_jdbc = ArchRuleDefinition.noClasses().that()
                .resideInAPackage("com.techatpark.workout.service")
                .should().accessClassesThat()
                .belongToAnyOf(JdbcTemplate.class)
                .because("we should not use JDBC Template");
        no_access_to_jdbc.check(allClasses);
    }

    @Test
    public void controllerMethodsReturnOnlyResponseEntities() {
        methods().that().arePublic().and().areDeclaredInClassesThat()
                .areAnnotatedWith(RestController.class)
                .should().haveRawReturnType(ResponseEntity.class)
                .check(allClasses);
    }


    @Test
    public void controllerMethodsAreDocumented() {
        methods().that().arePublic().and().areDeclaredInClassesThat()
                .areAnnotatedWith(RestController.class)
                .should().beAnnotatedWith(Operation.class)
                .check(allClasses);
    }


    @Test
    public void controllersAreSecureByDesign() {
        fields().that().areDeclaredInClassesThat()
                .areAnnotatedWith(RestController.class)
                .should().bePrivate().andShould().beFinal()
                .check(allClasses);

        methods().that().arePublic().and().areDeclaredInClassesThat()
                .areAnnotatedWith(RestController.class)
                .should().beFinal()
                .check(allClasses);

        classes().that().areAnnotatedWith(RestController.class)
                .should().notBePublic()
                .check(allClasses);
    }


    @Test
    public void layeredArchitectureTest() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controller").definedBy("..controller..")
                .layer("Service").definedBy("..service..")
                .layer("Util").definedBy("..util..")
                .layer("Security").definedBy("..security.config..", "." +
                        ".security.filter..")

                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller"
                        , "Security", "Util")
                .check(allClasses);

        fields().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                .should().haveNameEndingWith("Service");


    }
}
