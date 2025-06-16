package com.archunit.example.architecture;

import java.util.Arrays;
import java.util.stream.Stream;

public class ArchTestConstants {

    public static final String[] COMMON_PACKAGES = {
            "java..",
            "javax..",
            "com.google..",
            "org.springframework..",
            "org.slf4j.." // Added for logging, common in Spring Boot
    };

    public static String[] commonPackagesAnd(String... packages) {
        return Stream.concat(Arrays.stream(COMMON_PACKAGES), Arrays.stream(packages))
                .toArray(String[]::new);
    }
}
