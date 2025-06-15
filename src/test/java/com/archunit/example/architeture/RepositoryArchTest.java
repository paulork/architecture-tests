package com.archunit.example.architeture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.archunit.example")
public class RepositoryArchTest {

    //------------------------------------------------------------------------------------------------------------------
    // Repositories são interfaces, tem o sufixo Repository, extendem CrudRepository e tem a anotação @Repository.

    @ArchTest
    public static final ArchRule repositories_should_be_correctly_defined =
        classes().that().resideInAPackage("..repository..")
                .should().beAnnotatedWith(Repository.class)
                .andShould().haveSimpleNameEndingWith("Repository")
                .andShould().beInterfaces()
                .andShould().beAssignableTo(CrudRepository.class);

    //------------------------------------------------------------------------------------------------------------------
    // Repositories só podem ser acessados por Services.

    @ArchTest
    public static final ArchRule repositories_should_only_be_accessed_by_services =
        classes().that().resideInAPackage("..repository..")
                .should().onlyBeAccessed().byClassesThat().resideInAnyPackage("..service..", "..repository.."); // Allowing access from repository package itself for potential helper classes or default methods

    //------------------------------------------------------------------------------------------------------------------
    // Repositories só são permitidos dentro do seu pacote (com.archunit.example.model.repository).

    @ArchTest
    public static final ArchRule repositories_should_only_exist_in_repository_package =
        noClasses().that().resideOutsideOfPackage("..repository..")
                .should().beAnnotatedWith(Repository.class)
                .orShould().haveSimpleNameEndingWith("Repository");

}
