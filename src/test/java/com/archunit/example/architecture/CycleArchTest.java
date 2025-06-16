package com.archunit.example.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

@AnalyzeClasses(packages = "com.archunit.example")
public class CycleArchTest {

    @ArchTest
    public static final ArchRule check_for_cyclic_dependencies =
            SlicesRuleDefinition.slices().matching("com.archunit.example.(*)..").should().beFreeOfCycles();
}
