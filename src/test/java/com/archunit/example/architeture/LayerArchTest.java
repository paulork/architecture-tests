package com.archunit.example.architeture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

@AnalyzeClasses(packages = "com.archunit.example")
public class LayerArchTest {

    @ArchTest
    public static final ArchRule layers_access_check =
        Architectures.layeredArchitecture()
                .consideringAllDependencies()
                .layer("Config").definedBy("com.archunit.example.config..")
                .layer("Controller").definedBy("com.archunit.example.controller..")
                .layer("Service").definedBy("com.archunit.example.service..")
                .layer("Repository").definedBy("com.archunit.example.model.repository..")
                .layer("Model").definedBy("com.archunit.example.model..", "com.archunit.example.model.domain..")

                // Add back original, more complex access rules
                .whereLayer("Controller").mayOnlyBeAccessedByLayers("Config")
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service", "Config")
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                .whereLayer("Model").mayOnlyBeAccessedByLayers("Repository", "Service", "Controller", "Config", "java..");
}
