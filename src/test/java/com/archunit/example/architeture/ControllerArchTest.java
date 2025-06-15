package com.archunit.example.architeture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.springframework.web.bind.annotation.RestController;

import static com.archunit.example.architeture.ArchTestConstants.commonPackagesAnd;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.archunit.example")
public class ControllerArchTest {

    // -----------------------------------------------------------------------------------------------------------------
    // Controllers precisam ter a anotação @RestController e a classe o sufixo Controller.

    @ArchTest
    public static final ArchRule controllers_should_have_annotation_and_suffix_controller =
        classes().that().resideInAPackage("..controller..")
                .should().beAnnotatedWith(RestController.class)
                .andShould().haveSimpleNameEndingWith("Controller");

    // -----------------------------------------------------------------------------------------------------------------
    // Controllers só são permitidos dentro do seu pacote (com.archunit.example.controller)

    @ArchTest
    public static final ArchRule controllers_should_not_exists_outside_of_your_package =
        noClasses().that().resideOutsideOfPackage("..controller..")
                .should().beAnnotatedWith(RestController.class)
                .orShould().haveSimpleNameEndingWith("Controller");

    //------------------------------------------------------------------------------------------------------------------
    // Controllers não podem ser acessados por services, repositories ou qualquer outra classe do seu projeto.

    @ArchTest
    public static final ArchRule controllers_should_not_be_accessed_from_outsite_your_package =
        noClasses().that().resideOutsideOfPackage("..controller..")
                .should().accessClassesThat().resideInAPackage("..controller..");

    //------------------------------------------------------------------------------------------------------------------
    // Controllers acessam apenas Services, não repositories diretamente.

    @ArchTest
    public static final ArchRule controllers_should_only_access_classes_in_service_package = // Renamed test method
        classes().that().resideInAPackage("..controller..")
                .should().onlyAccessClassesThat().resideInAnyPackage(commonPackagesAnd("..service..", "..controller..")); // Added ..controller.. to allowed packages

}
