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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountBackofficeApi;
import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountHoldersApi;
import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountRecordsControllerApi;
import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountsApi;
import io.fireflysoftware.domain.banking.cards.sdk.api.CardBackofficeApi;
import io.fireflysoftware.domain.banking.cards.sdk.api.CardsApi;
import io.fireflysoftware.domain.configuration.sdk.api.EnvironmentConfigApi;
import io.fireflysoftware.domain.configuration.sdk.invoker.ApiClient;
import io.fireflysoftware.domain.distributor.branding.sdk.api.AgencyApi;
import io.fireflysoftware.domain.distributor.branding.sdk.api.AgentApi;
import io.fireflysoftware.domain.distributor.branding.sdk.api.DistributorQueryApi;
import io.fireflysoftware.domain.lending.collateral.sdk.api.CollateralCasesApi;
import io.fireflysoftware.domain.lending.collections.sdk.api.CollectionCasesApi;
import io.fireflysoftware.domain.lending.compliance.sdk.api.AmlCasesApi;
import io.fireflysoftware.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import io.fireflysoftware.domain.lending.mortgage.sdk.api.MortgageAgreementsApi;
import io.fireflysoftware.domain.lending.provisioning.sdk.api.ProvisioningCasesApi;
import io.fireflysoftware.domain.lending.supplychainfinance.sdk.api.SupplyChainFinanceAgreementsApi;
import io.fireflysoftware.domain.payments.sdk.api.PaymentRecordsApi;
import io.fireflysoftware.domain.referencedata.sdk.api.MastersApi;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Wires staff identity and access to domain-core-security-center.
 * {@code firefly.security-center.identity.adapter} stays on that service and defaults to sandbox.
 */
@Configuration
@EnableConfigurationProperties(TerminalWiring.TerminalLocations.class)
public class TerminalWiring {

    @Bean
    public IdentityPort identityPort(
            @Value("${api-configuration.domain-platform.security-center.base-path:http://localhost:8085}") String basePath) {
        return HttpIdentityAdapter.at(basePath);
    }

    @Bean
    public AccessPort accessPort(
            @Value("${api-configuration.domain-platform.security-center.base-path:http://localhost:8085}") String basePath) {
        return HttpAccessAdapter.at(basePath);
    }

    @Bean
    public AuditPort auditPort() {
        return new InMemoryAuditLog();
    }

    @Bean
    public ImpersonationPort impersonationPort(AccessPort accessPort, AuditPort auditPort) {
        return new ImpersonationCoordinator(accessPort, auditPort);
    }

    @Bean
    @ConditionalOnProperty(prefix = "firefly.terminal", name = "adapter", havingValue = "sandbox", matchIfMissing = true)
    public ConfigurationPort sandboxConfigurationPort() {
        return new SandboxConfigurationStore(FinanceSamples.properties(), FinanceSamples.currencies(), FinanceSamples.countries());
    }

    @Bean
    @ConditionalOnProperty(prefix = "firefly.terminal", name = "adapter", havingValue = "sandbox", matchIfMissing = true)
    public StaffRecordPort sandboxRecordPort(ConfigurationPort configurationPort) {
        return new SandboxRecordBook(configurationPort, FinanceSamples.records());
    }

    @Bean
    @ConditionalOnProperty(prefix = "firefly.terminal", name = "adapter", havingValue = "sandbox", matchIfMissing = true)
    public DistributorPort sandboxDistributorPort(ConfigurationPort configurationPort) {
        return new SandboxDistributorDirectory(configurationPort, FinanceArea.ACCOUNT.channelKey(), FinanceSamples.distributors());
    }

    @Bean
    @ConditionalOnProperty(prefix = "firefly.terminal", name = "adapter", havingValue = "domain")
    public ConfigurationPort domainConfigurationPort(TerminalLocations locations) {
        ApiClient configuration = new ApiClient();
        configuration.setBasePath(locations.getConfigurationBaseUrl());
        io.fireflysoftware.domain.referencedata.sdk.invoker.ApiClient masters =
                new io.fireflysoftware.domain.referencedata.sdk.invoker.ApiClient();
        masters.setBasePath(locations.getMasterDataBaseUrl());
        return new DomainConfigurationClient(new EnvironmentConfigApi(configuration), new MastersApi(masters));
    }

    @Bean
    @ConditionalOnProperty(prefix = "firefly.terminal", name = "adapter", havingValue = "domain")
    public StaffRecordPort domainRecordPort(ConfigurationPort configurationPort, TerminalLocations locations) {
        Map<String, String> domains = locations.getDomains();
        Set<String> configured = new LinkedHashSet<>();
        domains.forEach((key, value) -> {
            if (value != null && !value.isBlank()) {
                configured.add(key);
            }
        });
        io.fireflysoftware.domain.banking.accounts.sdk.invoker.ApiClient accounts = accountsClient(domains, "accounts");
        io.fireflysoftware.domain.banking.cards.sdk.invoker.ApiClient cards = cardsClient(domains, "cards");
        io.fireflysoftware.domain.lending.loan.origination.sdk.invoker.ApiClient loans =
                loanClient(domains, "lending-applications");
        return new DomainRecordClient(configurationPort, configured,
                new AccountBackofficeApi(accounts),
                new AccountHoldersApi(accounts),
                new AccountsApi(accounts),
                new CardBackofficeApi(cards),
                new CardsApi(cards),
                new LoanOriginationApi(loans),
                new MortgageAgreementsApi(mortgageClient(domains, "mortgage-agreements")),
                new SupplyChainFinanceAgreementsApi(supplyChainClient(domains, "supply-chain-finance-agreements")),
                new CollateralCasesApi(collateralClient(domains, "collateral-cases")),
                new CollectionCasesApi(collectionsClient(domains, "collection-cases")),
                new ProvisioningCasesApi(provisioningClient(domains, "provisioning-cases")),
                new AmlCasesApi(amlClient(domains, "aml-cases")));
    }

    @Bean
    @ConditionalOnProperty(prefix = "firefly.terminal", name = "adapter", havingValue = "domain")
    public DistributorPort domainDistributorPort(ConfigurationPort configurationPort, TerminalLocations locations) {
        io.fireflysoftware.domain.distributor.branding.sdk.invoker.ApiClient distributors =
                new io.fireflysoftware.domain.distributor.branding.sdk.invoker.ApiClient();
        distributors.setBasePath(trim(locations.getDistributorBaseUrl()));
        return new DomainDistributorClient(configurationPort,
                new DistributorQueryApi(distributors),
                new AgencyApi(distributors),
                new AgentApi(distributors),
                FinanceArea.ACCOUNT.channelKey());
    }

    @Bean
    public InstructionPort instructionPort(TerminalLocations locations) {
        io.fireflysoftware.domain.banking.accounts.sdk.invoker.ApiClient accounts =
                new io.fireflysoftware.domain.banking.accounts.sdk.invoker.ApiClient();
        accounts.setBasePath(locations.getAccountsBaseUrl());
        io.fireflysoftware.domain.payments.sdk.invoker.ApiClient payments =
                new io.fireflysoftware.domain.payments.sdk.invoker.ApiClient();
        payments.setBasePath(locations.getPaymentsBaseUrl());
        return new DomainInstructionClient(new AccountRecordsControllerApi(accounts), new PaymentRecordsApi(payments));
    }

    @Bean
    public StaffTerminal staffTerminal(IdentityPort identityPort, AccessPort accessPort, ImpersonationPort impersonationPort,
                                       ConfigurationPort configurationPort, DistributorPort distributorPort,
                                       StaffRecordPort recordPort, AuditPort auditPort) {
        return new StaffTerminal(identityPort, accessPort, impersonationPort, configurationPort, distributorPort, recordPort,
                auditPort, "ACCOUNT", FinanceArea.ACCOUNT.primaryId().toString(), FinanceArea.PREFIX);
    }

    @Bean
    public InstructionDesk instructionDesk(IdentityPort identityPort, AccessPort accessPort, InstructionPort instructionPort) {
        return new InstructionDesk(identityPort, accessPort, instructionPort, "ACCOUNT",
                FinanceArea.ACCOUNT.primaryId().toString());
    }

    @ConfigurationProperties(prefix = "firefly.terminal")
    public static class TerminalLocations {
        private String adapter = "sandbox";
        private String configurationBaseUrl = "http://localhost:8088";
        private String masterDataBaseUrl = "http://localhost:8089";
        private String distributorBaseUrl = "http://localhost:8070";
        private String accountsBaseUrl = "http://localhost:8090";
        private String paymentsBaseUrl = "http://localhost:8092";
        private Map<String, String> domains = new LinkedHashMap<>();

        public String getAdapter() {
            return adapter;
        }

        public void setAdapter(String adapter) {
            this.adapter = adapter;
        }

        public String getConfigurationBaseUrl() {
            return configurationBaseUrl;
        }

        public void setConfigurationBaseUrl(String configurationBaseUrl) {
            this.configurationBaseUrl = configurationBaseUrl;
        }

        public String getMasterDataBaseUrl() {
            return masterDataBaseUrl;
        }

        public void setMasterDataBaseUrl(String masterDataBaseUrl) {
            this.masterDataBaseUrl = masterDataBaseUrl;
        }

        public String getDistributorBaseUrl() {
            return distributorBaseUrl;
        }

        public void setDistributorBaseUrl(String distributorBaseUrl) {
            this.distributorBaseUrl = distributorBaseUrl;
        }

        public String getAccountsBaseUrl() {
            return accountsBaseUrl;
        }

        public void setAccountsBaseUrl(String accountsBaseUrl) {
            this.accountsBaseUrl = accountsBaseUrl;
        }

        public String getPaymentsBaseUrl() {
            return paymentsBaseUrl;
        }

        public void setPaymentsBaseUrl(String paymentsBaseUrl) {
            this.paymentsBaseUrl = paymentsBaseUrl;
        }

        public Map<String, String> getDomains() {
            return domains;
        }

        public void setDomains(Map<String, String> domains) {
            this.domains = domains;
        }
    }

    private static String trim(String url) {
        if (url == null || url.isBlank()) {
            throw new TerminalDeniedException("PROPERTY_NOT_SET");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String domainUrl(Map<String, String> domains, String key) {
        String url = domains.get(key);
        if (url == null || url.isBlank()) {
            return "http://127.0.0.1";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static io.fireflysoftware.domain.banking.accounts.sdk.invoker.ApiClient accountsClient(Map<String, String> domains, String key) {
        io.fireflysoftware.domain.banking.accounts.sdk.invoker.ApiClient client =
                new io.fireflysoftware.domain.banking.accounts.sdk.invoker.ApiClient();
        client.setBasePath(domainUrl(domains, key));
        return client;
    }

    private static io.fireflysoftware.domain.banking.cards.sdk.invoker.ApiClient cardsClient(Map<String, String> domains, String key) {
        io.fireflysoftware.domain.banking.cards.sdk.invoker.ApiClient client =
                new io.fireflysoftware.domain.banking.cards.sdk.invoker.ApiClient();
        client.setBasePath(domainUrl(domains, key));
        return client;
    }

    private static io.fireflysoftware.domain.lending.loan.origination.sdk.invoker.ApiClient loanClient(Map<String, String> domains, String key) {
        io.fireflysoftware.domain.lending.loan.origination.sdk.invoker.ApiClient client =
                new io.fireflysoftware.domain.lending.loan.origination.sdk.invoker.ApiClient();
        client.setBasePath(domainUrl(domains, key));
        return client;
    }

    private static io.fireflysoftware.domain.lending.mortgage.sdk.invoker.ApiClient mortgageClient(Map<String, String> domains, String key) {
        io.fireflysoftware.domain.lending.mortgage.sdk.invoker.ApiClient client =
                new io.fireflysoftware.domain.lending.mortgage.sdk.invoker.ApiClient();
        client.setBasePath(domainUrl(domains, key));
        return client;
    }

    private static io.fireflysoftware.domain.lending.supplychainfinance.sdk.invoker.ApiClient supplyChainClient(Map<String, String> domains, String key) {
        io.fireflysoftware.domain.lending.supplychainfinance.sdk.invoker.ApiClient client =
                new io.fireflysoftware.domain.lending.supplychainfinance.sdk.invoker.ApiClient();
        client.setBasePath(domainUrl(domains, key));
        return client;
    }

    private static io.fireflysoftware.domain.lending.collateral.sdk.invoker.ApiClient collateralClient(Map<String, String> domains, String key) {
        io.fireflysoftware.domain.lending.collateral.sdk.invoker.ApiClient client =
                new io.fireflysoftware.domain.lending.collateral.sdk.invoker.ApiClient();
        client.setBasePath(domainUrl(domains, key));
        return client;
    }

    private static io.fireflysoftware.domain.lending.collections.sdk.invoker.ApiClient collectionsClient(Map<String, String> domains, String key) {
        io.fireflysoftware.domain.lending.collections.sdk.invoker.ApiClient client =
                new io.fireflysoftware.domain.lending.collections.sdk.invoker.ApiClient();
        client.setBasePath(domainUrl(domains, key));
        return client;
    }

    private static io.fireflysoftware.domain.lending.provisioning.sdk.invoker.ApiClient provisioningClient(Map<String, String> domains, String key) {
        io.fireflysoftware.domain.lending.provisioning.sdk.invoker.ApiClient client =
                new io.fireflysoftware.domain.lending.provisioning.sdk.invoker.ApiClient();
        client.setBasePath(domainUrl(domains, key));
        return client;
    }

    private static io.fireflysoftware.domain.lending.compliance.sdk.invoker.ApiClient amlClient(Map<String, String> domains, String key) {
        io.fireflysoftware.domain.lending.compliance.sdk.invoker.ApiClient client =
                new io.fireflysoftware.domain.lending.compliance.sdk.invoker.ApiClient();
        client.setBasePath(domainUrl(domains, key));
        return client;
    }
}
