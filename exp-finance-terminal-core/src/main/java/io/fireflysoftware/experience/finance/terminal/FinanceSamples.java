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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sample book and the property keys an installer sets in core-common-config-mgmt.
 * Access grants are not listed here. Those live in the security center sandbox policy.
 */
public final class FinanceSamples {

    public static final UUID CUSTOMER_PARTY = UUID.fromString("10000000-0000-4000-8000-000000000001");
    public static final UUID HOLDER_ROLE = UUID.fromString("c1000000-0000-4000-8000-000000000001");
    public static final UUID CARD_SECURITY = UUID.fromString("c1000000-0000-4000-8000-000000000002");
    public static final UUID SCF_ACTIVE = UUID.fromString("c1000000-0000-4000-8000-000000000016");
    public static final UUID BROKER = UUID.fromString("b1000000-0000-4000-8000-000000000001");
    public static final UUID AGENCY = UUID.fromString("b1000000-0000-4000-8000-000000000002");
    public static final UUID COLLABORATOR = UUID.fromString("b1000000-0000-4000-8000-000000000003");

    private FinanceSamples() {
    }

    public static Map<String, String> properties() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(FinanceArea.ACCOUNT.closeStatusKey(), FinanceArea.ACCOUNT.defaultCloseStatus());
        values.put(FinanceArea.CARD.closeStatusKey(), FinanceArea.CARD.defaultCloseStatus());
        values.put(FinanceArea.LENDING_APPLICATION.closeStatusKey(), FinanceArea.LENDING_APPLICATION.defaultCloseStatus());
        values.put(FinanceArea.MORTGAGE_AGREEMENT.closeStatusKey(), FinanceArea.MORTGAGE_AGREEMENT.defaultCloseStatus());
        values.put(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.closeStatusKey(), FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.defaultCloseStatus());
        values.put(FinanceArea.COLLATERAL_CASE.closeStatusKey(), FinanceArea.COLLATERAL_CASE.defaultCloseStatus());
        values.put(FinanceArea.COLLECTION_CASE.closeStatusKey(), FinanceArea.COLLECTION_CASE.defaultCloseStatus());
        values.put(FinanceArea.PROVISIONING_CASE.closeStatusKey(), FinanceArea.PROVISIONING_CASE.defaultCloseStatus());
        values.put(FinanceArea.AML_CASE.closeStatusKey(), FinanceArea.AML_CASE.defaultCloseStatus());
        values.put(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.removeStatusKey(), "DRAFT");
        values.put(FinanceArea.ACCOUNT.holderRoleKey(), HOLDER_ROLE.toString());
        values.put(FinanceArea.CARD.cardSecurityKey(), CARD_SECURITY.toString());
        values.put(FinanceArea.ACCOUNT.channelKey(), "BRANCH");
        values.put(FinanceArea.ACCOUNT.recordIndexKey(), FinanceArea.ACCOUNT.primaryId().toString());
        values.put(FinanceArea.CARD.recordIndexKey(), FinanceArea.CARD.primaryId().toString());
        values.put(FinanceArea.LENDING_APPLICATION.recordIndexKey(), FinanceArea.LENDING_APPLICATION.primaryId().toString());
        values.put(FinanceArea.MORTGAGE_AGREEMENT.recordIndexKey(), FinanceArea.MORTGAGE_AGREEMENT.primaryId().toString());
        values.put(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.recordIndexKey(),
                FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.primaryId() + "," + SCF_ACTIVE);
        values.put(FinanceArea.COLLATERAL_CASE.recordIndexKey(), FinanceArea.COLLATERAL_CASE.primaryId().toString());
        values.put(FinanceArea.COLLECTION_CASE.recordIndexKey(), FinanceArea.COLLECTION_CASE.primaryId().toString());
        values.put(FinanceArea.PROVISIONING_CASE.recordIndexKey(), FinanceArea.PROVISIONING_CASE.primaryId().toString());
        values.put(FinanceArea.AML_CASE.recordIndexKey(), FinanceArea.AML_CASE.primaryId().toString());
        values.put("finance-terminal.customer-party-id", CUSTOMER_PARTY.toString());
        return values;
    }

    public static SandboxRecordBook.ListSeed records() {
        return new SandboxRecordBook.ListSeed(List.of(
                record(FinanceArea.ACCOUNT, FinanceArea.ACCOUNT.primaryId(), "ACTIVE", "Sample checking account"),
                record(FinanceArea.CARD, FinanceArea.CARD.primaryId(), "ACTIVE", "Sample card"),
                record(FinanceArea.LENDING_APPLICATION, FinanceArea.LENDING_APPLICATION.primaryId(), "OPEN", "Sample lending application"),
                record(FinanceArea.MORTGAGE_AGREEMENT, FinanceArea.MORTGAGE_AGREEMENT.primaryId(), "ACTIVE", "Sample mortgage agreement"),
                record(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT, FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.primaryId(), "DRAFT", "Sample supply chain draft"),
                record(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT, SCF_ACTIVE, "ACTIVE", "Sample supply chain agreement"),
                record(FinanceArea.COLLATERAL_CASE, FinanceArea.COLLATERAL_CASE.primaryId(), "ACTIVE", "Sample collateral case"),
                record(FinanceArea.COLLECTION_CASE, FinanceArea.COLLECTION_CASE.primaryId(), "ACTIVE", "Sample collection case"),
                record(FinanceArea.PROVISIONING_CASE, FinanceArea.PROVISIONING_CASE.primaryId(), "ACTIVE", "Sample provisioning case"),
                record(FinanceArea.AML_CASE, FinanceArea.AML_CASE.primaryId(), "ACTIVE", "Sample AML case")
        ));
    }

    public static List<DistributorView> distributors() {
        return List.of(
                new DistributorView(BROKER, "North Broker", "BROKER",
                        UUID.fromString("b2000000-0000-4000-8000-000000000001"), "BRANCH", null,
                        UUID.fromString("b3000000-0000-4000-8000-000000000001")),
                new DistributorView(AGENCY, "Harbor Agency", "AGENCY",
                        UUID.fromString("b2000000-0000-4000-8000-000000000002"), "BRANCH",
                        UUID.fromString("b4000000-0000-4000-8000-000000000002"),
                        UUID.fromString("b3000000-0000-4000-8000-000000000002")),
                new DistributorView(COLLABORATOR, "Field Collaborator", "COLLABORATOR",
                        UUID.fromString("b2000000-0000-4000-8000-000000000003"), "PARTNER", null,
                        UUID.fromString("b3000000-0000-4000-8000-000000000003"))
        );
    }

    public static List<MasterRecord> currencies() {
        return List.of(
                new MasterRecord("CURRENCY", UUID.fromString("d1000000-0000-4000-8000-000000000001"), "USD", "US Dollar"),
                new MasterRecord("CURRENCY", UUID.fromString("d1000000-0000-4000-8000-000000000002"), "EUR", "Euro")
        );
    }

    public static List<MasterRecord> countries() {
        return List.of(new MasterRecord("COUNTRY", UUID.fromString("d2000000-0000-4000-8000-000000000001"), "US", "United States"));
    }

    private static StaffRecord record(FinanceArea area, UUID id, String status, String reference) {
        return new StaffRecord(id, area.name(), CUSTOMER_PARTY, null, status, reference, null, null);
    }
}
