package com.archunit.example.architeture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.thirdparty.com.google.common.collect.ObjectArrays;
import org.junit.jupiter.api.*;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ControllerArchTest {

    private static final String[] COMMON_PACKAGES = {
            "java..",
            "javax..",
            "com.google..",
            "org.springframework..",
            "..controller.."};

    private static JavaClasses classes;

    private static String[] commonPackagesAnd(String... packages) {
        return ObjectArrays.concat(COMMON_PACKAGES, packages, String.class);
    }

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPackages("com.archunit.example");
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Controllers precisam ter a anotação @RestController e a classe o sufixo Controller.

    @Test
    @DisplayName("Controllers should have 'RestController' annotation and should have suffix 'Controller'")
    public void controllers_should_have_annotation_and_suffix_controller() {
        classes().that().resideInAPackage("..controller..")
                .should().beAnnotatedWith(RestController.class)
                .andShould().haveSimpleNameEndingWith("Controller")
                .check(classes);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Controllers só são permitidos dentro do seu pacote (com.archunit.example.controller)

    @Test
    @DisplayName("Should not have classes with '@RestController' and/or suffix 'Controller' outside Controllers package")
    public void controllers_should_not_exists_outside_of_your_package() {
        noClasses().that().resideOutsideOfPackage("..controller..")
                .should().beAnnotatedWith(RestController.class)
                .orShould().haveSimpleNameEndingWith("Controller")
                .check(classes);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Controllers não podem ser acessados por services, repositories ou qualquer outra classe do seu projeto.

    @Test
    @DisplayName("Controllers should not be accessed from outside your package")
    public void controllers_should_not_be_accessed_from_outsite_your_package() {
        noClasses().that().resideOutsideOfPackage("..controller..")
                .should().accessClassesThat().resideInAPackage("..controller..")
                .check(classes);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Controllers acessam apenas Services, não repositories diretamente.

    @Test
    @DisplayName("Controllers should only access classes in Services package")
    public void teste() {
        classes().that().resideInAPackage("..controller..")
                .should().onlyAccessClassesThat().resideInAnyPackage(commonPackagesAnd("..service.."))
                .check(classes);
    }

}
