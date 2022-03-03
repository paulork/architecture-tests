package com.archunit.example.architeture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.*;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryArchTest {

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
    // Repositories são interfaces, tem o sufixo Repository, extendem CrudRepository e tem a anotação @Repository.

    @Test
    @DisplayName("Repositories should have '@Repository' anotation, suffix 'Repository', be interfaces")
    public void repositories_should_have_anotation_and_suffix_repository_be_interfaces() {
        classes().that().resideInAPackage("..repository..")
                .should().beAnnotatedWith(Repository.class)
                .andShould().haveSimpleNameEndingWith("Repository")
                .andShould().beInterfaces()
                .andShould().beAssignableTo(CrudRepository.class)
                .check(classes);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Repositories só podem ser acessados por Services.

    @Test
    @DisplayName("Repositories should only be accessed by Services")
    public void repositories_should_only_be_accessed_by_services() {
        classes().that().resideInAPackage("..repository..")
                .should().onlyBeAccessed().byClassesThat().resideInAPackage("..service..")
                .check(classes);
    }

    //------------------------------------------------------------------------------------------------------------------
    // Repositories só são permitidos dentro do seu pacote (com.archunit.example.model.repository).

    @Test
    @DisplayName("Repositories should only exists in your package")
    public void repositories_should_only_exists_in_repository_package() {
        noClasses().that().resideOutsideOfPackage("..repository..")
                .should().beAnnotatedWith(Repository.class)
                .orShould().haveSimpleNameEndingWith("Repository")
                .check(classes);
    }

}
