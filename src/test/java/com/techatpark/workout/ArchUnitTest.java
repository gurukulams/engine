package com.techatpark.workout;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import io.swagger.v3.oas.annotations.Operation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchUnitTest {

    static JavaClasses allClasses = null;

    @BeforeAll
    public static void setUp() {
        allClasses = new ClassFileImporter().importPackages("com"
                + ".techatpark.workout");
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
                .layer("Security").definedBy("..security.config..", "." +
                        ".security.filter..")

                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller"
                        , "Security")
                .check(allClasses);

        fields().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                .should().haveNameEndingWith("Service");


    }
}
