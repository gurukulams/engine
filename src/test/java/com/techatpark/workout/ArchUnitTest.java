package com.techatpark.workout;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.syntax.elements.GivenClassesConjunction;
import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

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
    public void servicesAreSecureByDesign() {
        SERVICE_CLASSES.should()
                .accessClassesThat()
                .resideOutsideOfPackage("javax.validation")
                .andShould().onlyDependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework.stereotype"
                        , "org.springframework.security.core.userdetails"
                        , "org.springframework.jdbc.core"
                        , "org.springframework.security.crypto.bcrypt"
                        , "org.springframework.security.authentication"
                        , "com.techatpark.workout.starter.security.util"
                        , "com.gurukulams.core.store"
                        , "java.util.stream"
                        , "javax.sql"
                        , "java.sql.Date"
                        , "java.sql.ResultSet"
                        , "com.fasterxml.jackson.databind"
                        , "org.springframework.jdbc.core.simple"
                        , "org.slf4j"
                        , "com.gurukulams.core"
                        , "java.util"
                        , "java.lang"
                        , "jakarta.validation"
                        , "jakarta.validation.constraints"
                        , "..service.."
                        , "..model.."
                        , "..payload.."
                        )
                .because("Services should only be hexogonal")
                .check(allClasses);

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
