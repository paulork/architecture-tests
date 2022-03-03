package com.archunit.example.architeture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public class ServiceArchTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPackages("com.archunit.example");
    }

    //------------------------------------------------------------------------------------------------------------------
    // Services precisam ter a anotação @Service e a classe o sufixo Service.

    @Test
    @DisplayName("Services should have '@Service' anotation and suffix 'Service'")
    public void services_should_have_anotation_and_suffix_service() {
        classes().that().resideInAPackage("..service..")
                .should().beAnnotatedWith(Service.class)
                .andShould().haveSimpleNameEndingWith("Service")
                .check(classes);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Services só são permitidos dentro do seu pacote (com.archunit.example.service).
    @Test
    @DisplayName("Repositories should only exists in your package")
    public void repositories_should_only_exists_in_repository_package() {
        noClasses().that().resideOutsideOfPackage("..service..")
                .should().beAnnotatedWith(Service.class)
                .orShould().haveSimpleNameEndingWith("Service")
                .check(classes);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Services são chamados por Controllers e outros Services, e se utilizam de repositories.

    @Test
    @DisplayName("Services should only be accessed by controllers or other services")
    public void services_should_only_be_accessed_by_controllers_or_other_services() {
        classes().that().resideInAPackage("..service..")
                .should().onlyBeAccessed().byAnyPackage("..controller..", "..service..")
                .check(classes);
    }

    @Test
    @DisplayName("Service should only access other services and repositories")
    public void services_should_only_access_other_services_and_repositories() {
        classes().that().resideInAPackage("..service..")
                .should().accessClassesThat().resideInAnyPackage("..repository..", "..service..")
                .check(classes);
    }

}
