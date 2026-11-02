package io.fireflysoftware.experience.finance.terminal;
import org.fireflyframework.web.openapi.EnableOpenApiGen;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.*;
import org.springframework.web.bind.annotation.RestController;
/** Complete actual-controller producer, isolated from technical and financial runtime configurations. */
@EnableOpenApiGen
@Profile("openapi-gen")
@ComponentScan(basePackages="io.fireflysoftware.experience.finance.terminal",useDefaultFilters=false,
    includeFilters=@ComponentScan.Filter(type=FilterType.ANNOTATION,classes=RestController.class),
    excludeFilters=@ComponentScan.Filter(type=FilterType.REGEX,pattern=".*Test\\$.*"))
public class OpenApiGenApplication {public static void main(String[] args){SpringApplication.run(OpenApiGenApplication.class,args);}}
