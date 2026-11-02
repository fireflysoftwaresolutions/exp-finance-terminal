/* Copyright 2026 Firefly Software Foundation. Licensed under the Apache License, Version 2.0. */
package io.fireflysoftware.experience.finance.terminal.sdk;
import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.nio.file.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
class StaffPostingOriginalSdkContractTest {
    @Test void fullCanonicalRawAliasesMatchTheActualCurrentControllerProducer()throws Exception{var raw=Files.readAllBytes(Path.of("src/main/resources/openapi.yml"));try(var a=getClass().getClassLoader().getResourceAsStream("openapi.yml");var b=getClass().getClassLoader().getResourceAsStream("api-spec/openapi.yml")){assertNotNull(a);assertNotNull(b);assertArrayEquals(raw,a.readAllBytes());assertArrayEquals(raw,b.readAllBytes());}var doc=new ObjectMapper(new YAMLFactory()).readTree(raw);var actual=new ObjectMapper().readTree(Files.readAllBytes(Path.of("../exp-finance-terminal-web/target/staff-posting-producer/openapi.json")));assertEquals(actual.path("paths"),doc.path("paths"));assertEquals(1,doc.path("components").path("securitySchemes").size());assertTrue(doc.path("components").path("securitySchemes").has("staffBearer"));}
    @Test void actualOriginalGeneratorProducesTypedStaffApiAndInstantWithoutAnyHandwrittenSdk()throws Exception{assertEquals(Instant.class,Class.forName("io.fireflysoftware.experience.finance.terminal.sdk.model.StaffPostingReceipt").getMethod("getRecordedAt").getReturnType());assertNotNull(Class.forName("io.fireflysoftware.experience.finance.terminal.sdk.api.StaffLedgerPostingsApi"));}
}
