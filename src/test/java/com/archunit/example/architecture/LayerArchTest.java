package com.archunit.example.architecture;

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
                .layer("Controller").definedBy("com.archunit.example.controller..")
                .layer("Service").definedBy("com.archunit.example.service..")
                .layer("Repository").definedBy("com.archunit.example.model.repository..")
                .layer("Model").definedBy("com.archunit.example.model..", "com.archunit.example.model.domain..")

                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                .whereLayer("Model").mayOnlyBeAccessedByLayers("Repository", "Service", "Controller");
}
