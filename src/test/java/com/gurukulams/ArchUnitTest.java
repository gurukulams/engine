package com.gurukulams;

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


import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public class ArchUnitTest {

    static JavaClasses allClasses = null;

    private static final GivenClassesConjunction SERVICE_CLASSES = classes()
            .that()
            .areAnnotatedWith(Service.class);

    @BeforeAll
    public static void setUp() {
        allClasses = new ClassFileImporter()
                .importPackages("com.gurukulams");
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
    public void serviceLayerIsolcation() {
//        noClasses()
//                .that().resideInAPackage("..service..")
//                .should().dependOnClassesThat().belongToAnyOf(org.springframework.util.StringUtils.class);
        noClasses().that().resideInAPackage("..service..")
                .and().haveSimpleNameEndingWith("Service").should()
                .dependOnClassesThat().resideInAPackage("..org.springframework.util..").check(allClasses);
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
                .layer("Component").definedBy("..component..")
                .layer("Config").definedBy("..config..")
                .layer("Security").definedBy("..security.config..", "." +
                        ".security.filter..")

                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller"
                        , "Component", "Security", "Util", "Config")
                .check(allClasses);

        fields().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
                .should().haveNameEndingWith("Service");


    }
}
