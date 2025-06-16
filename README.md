## ArchUnit

[ArchUnit](http://www.archunit.org) is a library that helps you 'check' certain aspects of a Java project's architecture. With it, you can define certain architectural actions and boundaries to keep your project's architecture cohesive.

This example project demonstrates how to use ArchUnit to enforce architectural rules in a Spring Boot application.

## Running the Application

To run the application, use the following Maven command:
```bash
mvn spring-boot:run
```
Make sure you have Java and Maven installed and configured in your environment.

## Running Tests

To run the tests, use the following Maven command:
```bash
mvn test
```
This command will execute all tests in the project, including the ArchUnit architecture tests defined in the `src/test/java` directory.

### Project Structure Examples

#### Controller

```java
package com.archunit.example.controller;

import com.archunit.example.model.domain.Produto;
import com.archunit.example.service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/produto")
public class ProdutoController {

    @Autowired
    private ProdutoService prodService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produto save(@RequestBody Produto produto) {
        return prodService.save(produto);
    }

    @GetMapping("/{produto_id}")
    @ResponseStatus(HttpStatus.OK)
    public Produto findById(@PathVariable String produto_id) {
        return prodService.findById(produto_id);
    }

    @DeleteMapping("/{produto_id}")
    @ResponseStatus(HttpStatus.OK)
    public void removeById(@PathVariable String produto_id) {
        prodService.removeById(produto_id);
    }
}
```

#### Service

```java
package com.archunit.example.service;

import com.archunit.example.model.domain.Produto;
import com.archunit.example.model.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository prodRepository;

    // Example of a dependency that might be restricted by ArchUnit rules
    // @Autowired
    // private ProdutoController prodController;

    public Produto save(Produto produto) {
        return prodRepository.save(produto);
    }

    public Produto findById(String produto_id) {
        return prodRepository.findById(produto_id)
                .orElseThrow(() -> new IllegalArgumentException("Produto nao foi encontrado."));
    }

    public void removeById(String produto_id) {
        prodRepository.deleteById(produto_id);
    }
}
```

#### Repository

```java
package com.archunit.example.model.repository;

import com.archunit.example.model.domain.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {
}
```

### Testing the Architecture

Before creating tests, we need to add the ArchUnit dependencies to our project's `pom.xml`.

The core ArchUnit library:
```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit</artifactId>
    <version>1.4.1</version>
    <scope>test</scope>
</dependency>
```

For JUnit 5 integration, we use `archunit-junit5`:
```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.2.2</version>
    <scope>test</scope>
</dependency>
```
(Note: Generally, including `archunit-junit5` is sufficient as it transitively includes the necessary API and engine components. This project uses `archunit` for the core functionalities and `archunit-junit5` for seamless JUnit 5 integration.)


With this, we can create the tests and start 'checking' the limits we want for our architecture. We'll begin with the controllers.

All test classes should be annotated with `@AnalyzeClasses` to specify the packages to be imported for analysis. For example:
```java
import com.tngtech.archunit.junit.AnalyzeClasses;

@AnalyzeClasses(packages = "com.archunit.example")
public class ControllerArchTest {
    // ... tests ...
}
```

#### Controller Rules Example

```java
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

import static com.archunit.example.architecture.ArchTestConstants.commonPackagesAnd;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.archunit.example")
public class ControllerArchTest {

    // Rule: Controllers must have the @RestController annotation and the class name should end with 'Controller'.
    @ArchTest
    public static final ArchRule controllers_should_have_annotation_and_suffix_controller =
        classes().that().resideInAPackage("..controller..")
                .should().beAnnotatedWith(RestController.class)
                .andShould().haveSimpleNameEndingWith("Controller");

    // Rule: Controllers are only allowed within their package (com.archunit.example.controller)
    @ArchTest
    public static final ArchRule controllers_should_not_exists_outside_of_your_package =
        noClasses().that().resideOutsideOfPackage("..controller..")
                .should().beAnnotatedWith(RestController.class)
                .orShould().haveSimpleNameEndingWith("Controller");

    // Rule: Controllers should not be accessed by services, repositories, or any other class in your project (except Config).
    @ArchTest
    public static final ArchRule controllers_should_not_be_accessed_from_outsite_your_package =
        noClasses().that().resideOutsideOfPackage("..controller..")
                .and().resideOutsideOfPackage("..config..") // Allowing config classes to access controllers
                .should().accessClassesThat().resideInAPackage("..controller..");

    // Rule: Controllers should only access Services, not repositories directly.
    // (commonPackagesAnd includes basic Java, Spring, and controller packages itself)
    @ArchTest
    public static final ArchRule controllers_should_only_access_classes_in_service_package =
        classes().that().resideInAPackage("..controller..")
                .should().onlyAccessClassesThat().resideInAnyPackage(commonPackagesAnd("..service..", "..controller.."));
}
```

#### Service Rules Example
```java
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Service;

import static com.archunit.example.architecture.ArchTestConstants.commonPackagesAnd;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.archunit.example")
public class ServiceArchTest {

    // Rule: Services must have the @Service annotation and the class name should end with 'Service'.
    @ArchTest
    public static final ArchRule services_should_have_anotation_and_suffix_service =
        classes().that().resideInAPackage("..service..")
                .should().beAnnotatedWith(Service.class)
                .andShould().haveSimpleNameEndingWith("Service");

    // Rule: Services are only allowed within their package (com.archunit.example.service).
    @ArchTest
    public static final ArchRule services_should_only_exist_in_service_package =
        noClasses().that().resideOutsideOfPackage("..service..")
                .should().beAnnotatedWith(Service.class)
                .orShould().haveSimpleNameEndingWith("Service");

    // Rule: Services are called by Controllers and other Services, and they use repositories.
    @ArchTest
    public static final ArchRule services_should_only_be_accessed_by_controllers_or_other_services =
        classes().that().resideInAPackage("..service..")
                .should().onlyBeAccessed().byAnyPackage("..controller..", "..service..");

    @ArchTest
    public static final ArchRule services_should_only_access_allowed_packages =
        classes().that().resideInAPackage("..service..")
                .should().onlyAccessClassesThat().resideInAnyPackage(commonPackagesAnd("..repository..", "..service.."));
}

```

#### Repository Rules Example
```java
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.repository.CrudRepository; // Or JpaRepository, etc.
import org.springframework.stereotype.Repository;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.archunit.example")
public class RepositoryArchTest {

    // Rule: Repositories are interfaces, have the suffix 'Repository', extend a Spring Data repository interface, and have the @Repository annotation.
    @ArchTest
    public static final ArchRule repositories_should_be_correctly_defined =
        classes().that().resideInAPackage("..repository..")
                .should().beAnnotatedWith(Repository.class)
                .andShould().haveSimpleNameEndingWith("Repository")
                .andShould().beInterfaces()
                .andShould().beAssignableTo(CrudRepository.class); // Or JpaRepository, PagingAndSortingRepository as appropriate

    // Rule: Repositories can only be accessed by Services (and themselves, for helper methods or default implementations).
    @ArchTest
    public static final ArchRule repositories_should_only_be_accessed_by_services =
        classes().that().resideInAPackage("..repository..")
                .should().onlyBeAccessed().byClassesThat().resideInAnyPackage("..service..", "..repository..");

    // Rule: Repositories are only allowed within their package (e.g., com.archunit.example.model.repository).
    @ArchTest
    public static final ArchRule repositories_should_only_exist_in_repository_package =
        noClasses().that().resideOutsideOfPackage("..repository..")
                .should().beAnnotatedWith(Repository.class)
                .orShould().haveSimpleNameEndingWith("Repository");
}
```

### Layer Access Definitions

Instead of placing access definitions within the previous tests, we can separate them into a specific test for this purpose. This will expose the definition of layers and their accesses, and check if everything is okay. This way, we can leave specific validations and rules for the respective class tests (controllers/services/repositories).

```java
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;

@AnalyzeClasses(packages = "com.archunit.example")
public class LayerArchTest {

    @ArchTest
    public static final ArchRule layers_access_check =
        Architectures.layeredArchitecture()
                .consideringAllDependencies() // Optional: consider all dependencies including JDK, third-party libs
                .layer("Config").definedBy("com.archunit.example.config..")
                .layer("Controller").definedBy("com.archunit.example.controller..")
                .layer("Service").definedBy("com.archunit.example.service..")
                .layer("Repository").definedBy("com.archunit.example.model.repository..")
                .layer("Model").definedBy("com.archunit.example.model..", "com.archunit.example.model.domain..")

                .whereLayer("Controller").mayOnlyBeAccessedByLayers("Config")
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service", "Config")
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                .whereLayer("Model").mayOnlyBeAccessedByLayers("Repository", "Service", "Controller", "Config", "java..");
}
```

### Conclusion

This is a small example of using ArchUnit. That's why a classic MVC/Java project was chosen to make it easier to understand.
The main goal is to show how ArchUnit can be used to define and enforce architectural rules, helping to maintain a clean and well-structured codebase.
