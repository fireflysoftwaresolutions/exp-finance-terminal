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

import java.util.UUID;

/**
 * Financial administration areas. Lending cases call the domain sagas already on main.
 * Cards call domain-banking-cards. There is no credit-card domain in this terminal.
 */
public enum FinanceArea implements StaffArea {
    ACCOUNT("ACCOUNT", "10000000-0000-4000-8000-000000000002", "accounts",
            "/api/v1/backoffice/accounts/{id}", UpdateMode.ADD_HOLDER, CloseMode.ACCOUNT_QUERY, "",
            DecideMode.NONE, "CLOSED", false),
    CARD("CARD", "10000000-0000-4000-8000-000000000003", "cards",
            "/api/v1/backoffice/cards/{id}", UpdateMode.CARD_SECURITY, CloseMode.CARD_CANCEL, "",
            DecideMode.NONE, "CANCELLED", false),
    LENDING_APPLICATION("APPLICATION", "10000000-0000-4000-8000-000000000004", "lending-applications",
            "/api/v1/loan-applications/{id}", UpdateMode.EMPLOYMENT, CloseMode.APPLICATION_WITHDRAW, "withdraw",
            DecideMode.APPROVE_OR_REJECT, "WITHDRAWN", false),
    MORTGAGE_AGREEMENT("CONTRACT", "10000000-0000-4000-8000-000000000007", "mortgage-agreements",
            "/api/v1/mortgage-agreements/{id}", UpdateMode.PUT_DOCUMENT, CloseMode.JSON_SUFFIX, "close",
            DecideMode.NONE, "CLOSED", false),
    SUPPLY_CHAIN_FINANCE_AGREEMENT("CONTRACT", "c1000000-0000-4000-8000-000000000011", "supply-chain-finance-agreements",
            "/api/v1/supply-chain-finance-agreements/{id}", UpdateMode.PUT_DOCUMENT, CloseMode.JSON_SUFFIX, "close",
            DecideMode.NONE, "CLOSED", true),
    COLLATERAL_CASE("CONTRACT", "c1000000-0000-4000-8000-000000000012", "collateral-cases",
            "/api/v1/collateral-cases/{id}", UpdateMode.PUT_DOCUMENT, CloseMode.JSON_SUFFIX, "close",
            DecideMode.NONE, "RELEASED", false),
    COLLECTION_CASE("CONTRACT", "c1000000-0000-4000-8000-000000000013", "collection-cases",
            "/api/v1/collection-cases/{id}", UpdateMode.PUT_DOCUMENT, CloseMode.JSON_SUFFIX, "close",
            DecideMode.NONE, "CLOSED", false),
    PROVISIONING_CASE("CONTRACT", "c1000000-0000-4000-8000-000000000014", "provisioning-cases",
            "/api/v1/provisioning-cases/{id}", UpdateMode.PUT_DOCUMENT, CloseMode.JSON_SUFFIX, "close",
            DecideMode.NONE, "RELEASED", false),
    AML_CASE("CONTRACT", "c1000000-0000-4000-8000-000000000015", "aml-cases",
            "/api/v1/aml-cases/{id}", UpdateMode.PUT_DOCUMENT, CloseMode.JSON_SUFFIX, "close",
            DecideMode.NONE, "CLOSED", false);

    public static final String PREFIX = "finance-terminal";

    private final String accessType;
    private final UUID primaryId;
    private final String domainKey;
    private final String itemTemplate;
    private final UpdateMode updateMode;
    private final CloseMode closeMode;
    private final String closeSuffix;
    private final DecideMode decideMode;
    private final String defaultCloseStatus;
    private final boolean draftRemoval;

    FinanceArea(String accessType, String primaryId, String domainKey, String itemTemplate,
                UpdateMode updateMode, CloseMode closeMode, String closeSuffix, DecideMode decideMode,
                String defaultCloseStatus, boolean draftRemoval) {
        this.accessType = accessType;
        this.primaryId = UUID.fromString(primaryId);
        this.domainKey = domainKey;
        this.itemTemplate = itemTemplate;
        this.updateMode = updateMode;
        this.closeMode = closeMode;
        this.closeSuffix = closeSuffix;
        this.decideMode = decideMode;
        this.defaultCloseStatus = defaultCloseStatus;
        this.draftRemoval = draftRemoval;
    }

    @Override
    public String propertyPrefix() {
        return PREFIX;
    }

    @Override
    public String accessType() {
        return accessType;
    }

    @Override
    public UUID primaryId() {
        return primaryId;
    }

    @Override
    public String domainKey() {
        return domainKey;
    }

    @Override
    public String itemPath(UUID id) {
        return itemTemplate.replace("{id}", id.toString());
    }

    @Override
    public UpdateMode updateMode() {
        return updateMode;
    }

    @Override
    public CloseMode closeMode() {
        return closeMode;
    }

    @Override
    public String closeSuffix() {
        return closeSuffix;
    }

    @Override
    public DecideMode decideMode() {
        return decideMode;
    }

    @Override
    public String closeStatusKey() {
        return PREFIX + ".close-status." + name();
    }

    @Override
    public String defaultCloseStatus() {
        return defaultCloseStatus;
    }

    @Override
    public boolean draftRemoval() {
        return draftRemoval;
    }

    public static FinanceArea fromName(String name) {
        return FinanceArea.valueOf(name);
    }
}
