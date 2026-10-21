/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.finance.terminal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainRecordClientTest {

    private HttpServer server;
    private final List<String> calls = new CopyOnWriteArrayList<>();
    private DomainRecordClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", this::handle);
        server.start();
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        ConfigurationPort configuration = new SandboxConfigurationStore(
                FinanceSamples.properties(), FinanceSamples.currencies(), FinanceSamples.countries());
        Map<String, String> domains = FinanceArea.ACCOUNT.domainKey() == null ? Map.of() : Map.of(
                "accounts", base,
                "cards", base,
                "lending-applications", base,
                "mortgage-agreements", base,
                "supply-chain-finance-agreements", base,
                "collateral-cases", base,
                "collection-cases", base,
                "provisioning-cases", base,
                "aml-cases", base);
        client = new DomainRecordClient(WebClient.builder().build(), configuration, domains);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void mortgageUpdatePutsTheEmployeeAndTheCustomerParty() {
        RecordCommand command = new RecordCommand(FinanceArea.MORTGAGE_AGREEMENT, FinanceArea.MORTGAGE_AGREEMENT.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Correct the note", "Staff note", null, null, null,
                UUID.fromString("33333333-3333-4333-8333-333333333333"), null, null);
        StaffRecord updated = client.update(command).block();
        assertEquals(command.actorId(), updated.actorId());
        assertEquals(FinanceSamples.CUSTOMER_PARTY, updated.customerPartyId());
        assertTrue(calls.stream().anyMatch(call -> call.startsWith("PUT /api/v1/mortgage-agreements/")
                && call.contains(command.actorId().toString())
                && call.contains(FinanceSamples.CUSTOMER_PARTY.toString())));
    }

    @Test
    void accountUpdatePostsTheCustomerAsHolder() {
        RecordCommand command = new RecordCommand(FinanceArea.ACCOUNT, FinanceArea.ACCOUNT.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Add the customer", null, null, null, null,
                UUID.fromString("33333333-3333-4333-8333-333333333333"), null, null);
        client.update(command).block();
        assertTrue(calls.stream().anyMatch(call -> call.startsWith("POST /api/v1/accounts/")
                && call.contains("/holders")
                && call.contains(FinanceSamples.CUSTOMER_PARTY.toString())
                && call.contains(FinanceSamples.HOLDER_ROLE.toString())));
    }

    @Test
    void collateralClosePostsTheClosePath() {
        RecordCommand command = new RecordCommand(FinanceArea.COLLATERAL_CASE, FinanceArea.COLLATERAL_CASE.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Release", null, null, null, null,
                UUID.fromString("33333333-3333-4333-8333-333333333333"), "RELEASED", null);
        StaffRecord closed = client.close(command).block();
        assertEquals("RELEASED", closed.status());
        assertTrue(calls.stream().anyMatch(call -> call.startsWith("POST /api/v1/collateral-cases/")
                && call.contains("/close")));
    }

    @Test
    void supplyChainRemovalDeletesOnlyADraft() {
        RecordCommand active = RecordCommand.of(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT, FinanceSamples.SCF_ACTIVE,
                FinanceSamples.CUSTOMER_PARTY, "no");
        TerminalDeniedException denied = assertThrows(TerminalDeniedException.class,
                () -> client.removeDraft(active, "DRAFT").block());
        assertEquals("DRAFT_REQUIRED", denied.reasonCode());
        assertTrue(calls.stream().noneMatch(call -> call.startsWith("DELETE ")));

        RecordCommand draft = RecordCommand.of(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT,
                FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.primaryId(), FinanceSamples.CUSTOMER_PARTY, "yes");
        client.removeDraft(draft, "DRAFT").block();
        assertTrue(calls.stream().anyMatch(call -> call.startsWith("DELETE /api/v1/supply-chain-finance-agreements/"
                + FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.primaryId())));
    }

    private void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String raw = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        calls.add(exchange.getRequestMethod() + " " + path + " " + raw);
        byte[] response;
        if ("GET".equals(exchange.getRequestMethod()) && path.contains("supply-chain-finance-agreements")) {
            ObjectNode body = mapper.createObjectNode();
            body.put("id", path.substring(path.lastIndexOf('/') + 1));
            body.put("status", path.endsWith(FinanceSamples.SCF_ACTIVE.toString()) ? "ACTIVE" : "DRAFT");
            body.put("notes", "agreement");
            response = mapper.writeValueAsBytes(body);
        } else if ("GET".equals(exchange.getRequestMethod()) && path.contains("mortgage-agreements")) {
            ObjectNode body = mapper.createObjectNode();
            body.put("mortgageAgreementId", path.substring(path.lastIndexOf('/') + 1));
            body.put("agreementStatus", "ACTIVE");
            body.put("notes", "mortgage");
            response = mapper.writeValueAsBytes(body);
        } else if ("GET".equals(exchange.getRequestMethod())) {
            ObjectNode body = mapper.createObjectNode();
            body.put("status", "ACTIVE");
            response = mapper.writeValueAsBytes(body);
        } else if ("PUT".equals(exchange.getRequestMethod())) {
            response = raw.getBytes(StandardCharsets.UTF_8);
        } else {
            response = "{}".getBytes(StandardCharsets.UTF_8);
        }
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
