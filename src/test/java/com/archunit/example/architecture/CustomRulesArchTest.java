package com.archunit.example.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.DependencyRules;

@AnalyzeClasses(packages = "com.archunit.example")
public class CustomRulesArchTest {

    @ArchTest
    public static final ArchRule dto_classes_should_have_dto_suffix =
            ArchRuleDefinition.classes().that().resideInAPackage("..dto..")
            .should().haveSimpleNameEndingWith("DTO");

    @ArchTest
    public static final ArchRule no_dependencies_on_upper_packages =
            DependencyRules.NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;
}
