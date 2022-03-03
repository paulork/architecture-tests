package com.archunit.example.architeture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LayerArchTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setup() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .importPackages("com.archunit.example");
    }

    @Test
    @DisplayName("Layer access definitions")
    public void layers_access_check() {
        Architectures.layeredArchitecture()
                .layer("Controller").definedBy("com.archunit.example.controller..")
                .layer("Service").definedBy("com.archunit.example.service..")
                .layer("Model").definedBy("com.archunit.example.model..")
                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
                .whereLayer("Model").mayOnlyBeAccessedByLayers("Service")
                .check(classes);
    }

}
