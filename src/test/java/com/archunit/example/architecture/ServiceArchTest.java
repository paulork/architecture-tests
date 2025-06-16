package com.archunit.example.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

import static com.archunit.example.architecture.ArchTestConstants.commonPackagesAnd;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.archunit.example")
public class ServiceArchTest {

    //------------------------------------------------------------------------------------------------------------------
    // Services precisam ter a anotação @Service e a classe o sufixo Service.

    @ArchTest
    public static final ArchRule services_should_have_anotation_and_suffix_service =
        classes().that().resideInAPackage("..service..")
                .should().beAnnotatedWith(Service.class)
                .andShould().haveSimpleNameEndingWith("Service");

    //------------------------------------------------------------------------------------------------------------------
    // Services só são permitidos dentro do seu pacote (com.archunit.example.service).
    @ArchTest
    public static final ArchRule services_should_only_exist_in_service_package = // Corrected method name
        noClasses().that().resideOutsideOfPackage("..service..")
                .should().beAnnotatedWith(Service.class)
                .orShould().haveSimpleNameEndingWith("Service");

    //------------------------------------------------------------------------------------------------------------------
    // Services são chamados por Controllers e outros Services, e se utilizam de repositories.

    @ArchTest
    public static final ArchRule services_should_only_be_accessed_by_controllers_or_other_services =
        classes().that().resideInAPackage("..service..")
                .should().onlyBeAccessed().byAnyPackage("..controller..", "..service..");

    @ArchTest
    public static final ArchRule services_should_only_access_allowed_packages = // Corrected method name for clarity
        classes().that().resideInAPackage("..service..")
                .should().onlyAccessClassesThat().resideInAnyPackage(commonPackagesAnd("..repository..", "..service.."));
}
