## ArchUnit

[ArchUnit](http://www.archunit.org) é uma biblioteca que lhe ajuda a 'checar' certos aspectos da arquitetura de um projeto Java. Com ela você consegue delimitar certas ações e fronteiras arquiteturais, afim de manter coesa a arquitetura do seu projeto.

#### Controller

```java
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
@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository prodRepository;

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
@Repository
public interface ProdutoRepository extends JpaRepository<Produto, String> {
}
```

### Testando a arquitetura

Antes de criarmos os testes, precisamos adicionar a dependência do ArchUnit ao 'pom.xml' do nosso projeto.

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit</artifactId>
    <version>0.13.0</version>
    <scope>test</scope>
</dependency>
```

Com isso já podemos criar os testes e começar a 'checar' os limites que queremos para a nossa arquitetura, e faremos isso iniciando pelos controllers.

#### Controller

```java
// -----------------------------------------------------------------------------------------------------------------
// Controllers precisam ter a anotação @RestController e a classe o sufixo Controller.
@Test
@DisplayName("Controlers should have 'RestController' annotation and should have suffix 'Controller'")
public void controllers_should_have_annotation_and_suffix_controller() {
classes().that().resideInAPackage("..controller..")
        .should().beAnnotatedWith(RestController.class)
        .andShould().haveSimpleNameEndingWith("Controller")
        .check(classes);
}

// -----------------------------------------------------------------------------------------------------------------
// Controllers só são permitidos dentro do seu pacote (com.archunit.example.controller)
@Test
@DisplayName("Should not have classes with '@RestController' and/or suffix 'Controller' outside Controllers package")
public void controllers_should_not_exists_outside_of_your_package() {
noClasses().that().resideOutsideOfPackage("..controller..")
        .should().beAnnotatedWith(RestController.class)
        .orShould().haveSimpleNameEndingWith("Controller")
        .check(classes);
}

//------------------------------------------------------------------------------------------------------------------
// Controllers não podem ser acessados por services, repositories ou qualquer outra classe do seu projeto.
@Test
@DisplayName("Controllers should not be accessed from outside your package")
public void controllers_should_not_be_accessed_from_outsite_your_package() {
noClasses().that().resideOutsideOfPackage("..controller..")
        .should().accessClassesThat().resideInAPackage("..controller..")
        .check(classes);
}

//------------------------------------------------------------------------------------------------------------------
// Controllers acessam apenas Services, não repositories diretamente.
@Test
@DisplayName("Controllers should only access classes in Services package")
public void controllers_should_only_access_classes_in_service_package() {
classes().that().resideInAPackage("..controller..")
        .should().onlyAccessClassesThat().resideInAnyPackage(commonPackagesAnd("..service.."))
        .check(classes);
}
```

#### Service

```java
//------------------------------------------------------------------------------------------------------------------
// Services precisam ter a anotação @Service e a classe o sufixo Service.
@Test
@DisplayName("Services should have '@Service' anotation and suffix 'Service'")
public void services_should_have_anotation_and_suffix_service() {
classes().that().resideInAPackage("..service..")
        .should().beAnnotatedWith(Service.class)
        .andShould().haveSimpleNameEndingWith("Service")
        .check(classes);
}

//------------------------------------------------------------------------------------------------------------------
// Services só são permitidos dentro do seu pacote (com.archunit.example.service).
@Test
@DisplayName("Repositories should only exists in your package")
public void repositories_should_only_exists_in_repository_package() {
noClasses().that().resideOutsideOfPackage("..service..")
        .should().beAnnotatedWith(Service.class)
        .orShould().haveSimpleNameEndingWith("Service")
        .check(classes);
}

//------------------------------------------------------------------------------------------------------------------
// Services são chamados por Controllers e outros Services, e se utilizam de repositories.
@Test
@DisplayName("Services should only be accessed by controllers or other services")
public void services_should_only_be_accessed_by_controllers_or_other_services() {
classes().that().resideInAPackage("..service..")
        .should().onlyBeAccessed().byAnyPackage("..controller..", "..service..")
        .check(classes);
}

@Test
@DisplayName("Service should only access other services and repositories")
public void services_should_only_access_other_services_and_repositories() {
classes().that().resideInAPackage("..service..")
        .should().accessClassesThat().resideInAnyPackage("..repository..", "..service..")
        .check(classes);
}
```

#### Repository

```java
//------------------------------------------------------------------------------------------------------------------
// Repositories são interfaces, tem o sufixo Repository, extendem CrudRepository e tem a anotação @Repository.
@Test
@DisplayName("Repositories should have '@Repository' anotation, suffix 'Repository', be interfaces")
public void repositories_should_have_anotation_and_suffix_repository_be_interfaces() {
classes().that().resideInAPackage("..repository..")
        .should().beAnnotatedWith(Repository.class)
        .andShould().haveSimpleNameEndingWith("Repository")
        .andShould().beInterfaces()
        .andShould().beAssignableTo(CrudRepository.class)
        .check(classes);
}

//------------------------------------------------------------------------------------------------------------------
// Repositories só podem ser acessados por Services.
@Test
@DisplayName("Repositories should only be accessed by Services")
public void repositories_should_only_be_accessed_by_services() {
classes().that().resideInAPackage("..repository..")
        .should().onlyBeAccessed().byClassesThat().resideInAPackage("..service..")
        .check(classes);
}

//------------------------------------------------------------------------------------------------------------------
// Repositories só são permitidos dentro do seu pacote (com.archunit.example.model.repository).
@Test
@DisplayName("Repositories should only exists in your package")
public void repositories_should_only_exists_in_repository_package() {
noClasses().that().resideOutsideOfPackage("..repository..")
        .should().beAnnotatedWith(Repository.class)
        .orShould().haveSimpleNameEndingWith("Repository")
        .check(classes);
}
```

### Layer Access Definitions

Em vez de colocarmos as definições de acesso dentro dos testes anteriores, podemos separá-los em um testes específico para isso. Que vai expor a definição das camadas e seus acessos, e ver se tudo está ok. Dessa forma podemos deixar para os testes específicos (controllers/services/repositorys) as validações e regras da própria classe.

```java
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
```

### Conclusão

Esse é um pequeno exemplo do uso do ArchUnit. Por isso inclusive a escolha de uma projeto classico MVC/Java para ficar mais facil o entendimento.
