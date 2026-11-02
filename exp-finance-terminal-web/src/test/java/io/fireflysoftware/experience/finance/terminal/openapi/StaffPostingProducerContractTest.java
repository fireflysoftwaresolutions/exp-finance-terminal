/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.openapi;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.fireflysoftware.experience.finance.terminal.OpenApiGenApplication;
import io.fireflysoftware.experience.finance.terminal.posting.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
@ActiveProfiles("openapi-gen")
@SpringBootTest(classes=OpenApiGenApplication.class,webEnvironment=SpringBootTest.WebEnvironment.MOCK,properties={"spring.main.web-application-type=reactive","springdoc.api-docs.version=OPENAPI_3_0"})
class StaffPostingProducerContractTest {
    static final String BASE="/api/v1/staff/tenants/{tenantId}/books/{bookId}/ledger-journals",BANK="/api/v1/staff/tenants/{tenantId}/banks/{bankId}/enrollments/{enrollmentId}/versions/{version}/current";
    @Autowired ApplicationContext context;
    record Operation(String path,String verb,String id){}
    Set<Operation> operations(JsonNode doc){var values=new HashSet<Operation>();doc.path("paths").fields().forEachRemaining(path->path.getValue().fields().forEachRemaining(method->{if(Set.of("get","post","put","patch","delete","head","options","trace").contains(method.getKey()))assertTrue(values.add(new Operation(path.getKey(),method.getKey(),method.getValue().path("operationId").asText())));}));return values;}
    JsonNode actual()throws Exception{var http=WebTestClient.bindToApplicationContext(context).configureClient().responseTimeout(Duration.ofSeconds(3)).build();var json=http.get().uri("/v3/api-docs").exchange().expectStatus().isOk().expectBody().returnResult().getResponseBody();var yaml=http.get().uri("/v3/api-docs.yaml").exchange().expectStatus().isOk().expectBody().returnResult().getResponseBody();assertNotNull(json);assertNotNull(yaml);assertTrue(json.length>0&&json.length<=4194304&&yaml.length>0&&yaml.length<=4194304);var doc=new ObjectMapper().readTree(json);assertEquals(doc,new ObjectMapper(new YAMLFactory()).readTree(yaml));Files.createDirectories(Path.of("target/staff-posting-producer"));Files.write(Path.of("target/staff-posting-producer/openapi.json"),json);Files.write(Path.of("target/staff-posting-producer/openapi.yml"),yaml);return doc;}
    JsonNode resolve(JsonNode doc,JsonNode value){return value.has("$ref")?doc.at(value.path("$ref").asText().substring(1)):value;}
    @Test void everyLegacyOperationAndCurrentOwnerReadSurviveWithOnlyTwoStaffPostingOperationsAdded()throws Exception{var doc=actual();var legacy=new ObjectMapper(new YAMLFactory()).readTree(getClass().getResourceAsStream("/legacy-terminal-openapi.yml"));var expected=new HashSet<>(operations(legacy));assertTrue(expected.add(new Operation(BANK,"get","getCurrentStaffBankOwner")));assertTrue(expected.add(new Operation(BASE,"post","postStaffLedgerJournal")));assertTrue(expected.add(new Operation(BASE+"/recovery","post","recoverStaffLedgerJournal")));assertEquals(expected,operations(doc));}
    @Test void actualProducerRetainsTheSoleOriginalBearerSchemeAndTypedOriginalActionContract()throws Exception{
        var doc=actual();var schemes=new HashSet<String>();doc.path("components").path("securitySchemes").fieldNames().forEachRemaining(schemes::add);assertEquals(Set.of("staffBearer"),schemes);
        for(var path:List.of(BASE,BASE+"/recovery")){var operation=doc.path("paths").path(path).path("post");assertTrue(operation.path("security").toString().contains("staffBearer"));var input=resolve(doc,operation.path("requestBody").path("content").path("application/json").path("schema"));assertTrue(input.path("properties").has("expectedIntentDigest"));assertTrue(input.path("properties").has("posting"));for(var code:List.of("200","400","401","403","404","409","422","503","4XX","default"))assertTrue(operation.path("responses").has(code),code);for(var parameter:operation.path("parameters"))assertFalse(parameter.path("name").asText().equalsIgnoreCase("X-Idempotency-Key"));}
        assertEquals("#/components/schemas/FireflyInstant",doc.path("components").path("schemas").path("StaffPostingReceipt").path("properties").path("recordedAt").path("$ref").asText());assertEquals("date-time",doc.path("components").path("schemas").path("FireflyInstant").path("format").asText());
    }
    @Test void producerNeverCreatesDomainClientsOrFinancialExecutionBeans()throws Exception{actual();assertEquals(0,context.getBeanNamesForType(StaffPostingClientFactory.class,false,false).length);assertEquals(0,context.getBeanNamesForType(StaffPostingExecution.class,false,false).length);}
    @Test void onlyCompleteAssertedOriginalProducerBytesCanFeedTheOfficialSdk()throws Exception{everyLegacyOperationAndCurrentOwnerReadSurviveWithOnlyTwoStaffPostingOperationsAdded();actualProducerRetainsTheSoleOriginalBearerSchemeAndTypedOriginalActionContract();producerNeverCreatesDomainClientsOrFinancialExecutionBeans();io.fireflysoftware.experience.finance.terminal.openapi.impl.OriginalProducerSdkInputFixtureImpl.writeIfRequested(Files.readAllBytes(Path.of("target/staff-posting-producer/openapi.yml")));}
}
