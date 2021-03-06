package org.citeplag;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Predicate;
import org.citeplag.components.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;

/**
 * Main application start.
 *
 * @author Vincent Stange
 */
@ComponentScan
@Configuration
@EnableSwagger2
@EnableScheduling
@EnableAutoConfiguration
public class ApplicationStart {
    @Value("${server.cron_enabled}")
    private boolean cronEnabled;
    public static void main(String[] args) throws Exception {
        // start the full spring environment
        SpringApplication.run(ApplicationStart.class, args);
    }

    @Autowired
    private TypeResolver typeResolver;

    /**
     * Pretty print for every json output.
     *
     * @return override the jackson builder.
     */
    @Bean
    public Jackson2ObjectMapperBuilder jacksonBuilder() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.indentOutput(true);
        return builder;
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        return new MethodValidationPostProcessor();
    }

    @Bean
    public Scheduler scheduler() {
        if (this.cronEnabled) {
            return new Scheduler();
        } else {
            return null;
        }
    }
    /**
     * SpringFox / Swagger configuration.
     * @return Docket Object from SpringFox / Swagger.
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                // general informations
                .apiInfo(getApiInfo())
                .pathMapping("/")
                // exposed endpoints
                .select()
                .paths(getDocumentedApiPaths())
                .build()
                // Convenience rule builder that substitutes a generic type with one type parameter
                // with the type parameter. In this case ResponseEntity<T>
                .genericModelSubstitutes(ResponseEntity.class)
                // default response code should not be used
                .useDefaultResponseMessages(false);
    }

    /**
     * Every REST Service we want to document with Swagger
     *
     * @return Predicate conditions
     */
    private Predicate<String> getDocumentedApiPaths() {
        return or(
                regex("/math.*"),
                regex("/moi.*"),
                regex("/tests.*"),
                regex("/config.*"),
                regex("/basex.*"),
                regex("/v1/media.*")
        );
    }
    @Scheduled(cron = "*/1 * * * * *")
    private void minutely() {
        System.out.println("asd");
    }

    /**
     * General information about our project's API.
     * (Information for the Swagger UI)
     *
     * @return see ApiInfo
     */
    private ApiInfo getApiInfo() {
        return new ApiInfoBuilder()
                .title("Formulasearch - A REST-Endpoint for Searching Formulae based on VMEXT-Demo")
                .description("Spring based REST API")
                .termsOfServiceUrl("http://springfox.io")
                .contact(new Contact("MaRDI@NFDI TA5", "https://github.com/MaRDI4NFDI", "ta5 at mardi4nfdi.de "))
                .license("Apache License Version 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0")
                .version("2.0")
                .build();
    }
}