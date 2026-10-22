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

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountBackofficeApi;
import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountHoldersApi;
import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountsApi;
import io.fireflysoftware.domain.banking.accounts.sdk.model.AccountDTO;
import io.fireflysoftware.domain.banking.accounts.sdk.model.AddAccountHolderCommand;
import io.fireflysoftware.domain.banking.accounts.sdk.model.AddHolderResponse;
import io.fireflysoftware.domain.banking.cards.sdk.api.CardBackofficeApi;
import io.fireflysoftware.domain.banking.cards.sdk.api.CardsApi;
import io.fireflysoftware.domain.lending.collateral.sdk.api.CollateralCasesApi;
import io.fireflysoftware.domain.lending.collateral.sdk.model.CollateralCaseDTO;
import io.fireflysoftware.domain.lending.collateral.sdk.model.ReasonRequest;
import io.fireflysoftware.domain.lending.collections.sdk.api.CollectionCasesApi;
import io.fireflysoftware.domain.lending.compliance.sdk.api.AmlCasesApi;
import io.fireflysoftware.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import io.fireflysoftware.domain.lending.mortgage.sdk.api.MortgageAgreementsApi;
import io.fireflysoftware.domain.lending.mortgage.sdk.model.MortgageAgreementDTO;
import io.fireflysoftware.domain.lending.provisioning.sdk.api.ProvisioningCasesApi;
import io.fireflysoftware.domain.lending.supplychainfinance.sdk.api.SupplyChainFinanceAgreementsApi;
import io.fireflysoftware.domain.lending.supplychainfinance.sdk.model.SupplyChainFinanceAgreementDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DomainRecordClientTest {

    private AccountBackofficeApi accountBackoffice;
    private AccountHoldersApi accountHolders;
    private MortgageAgreementsApi mortgages;
    private SupplyChainFinanceAgreementsApi supplyChain;
    private CollateralCasesApi collateral;
    private DomainRecordClient client;

    @BeforeEach
    void setUp() {
        accountBackoffice = mock(AccountBackofficeApi.class);
        accountHolders = mock(AccountHoldersApi.class);
        mortgages = mock(MortgageAgreementsApi.class);
        supplyChain = mock(SupplyChainFinanceAgreementsApi.class);
        collateral = mock(CollateralCasesApi.class);
        ConfigurationPort configuration = new SandboxConfigurationStore(
                FinanceSamples.properties(), FinanceSamples.currencies(), FinanceSamples.countries());
        client = new DomainRecordClient(configuration, Set.of(
                "accounts", "cards", "lending-applications", "mortgage-agreements",
                "supply-chain-finance-agreements", "collateral-cases", "collection-cases",
                "provisioning-cases", "aml-cases"),
                accountBackoffice, accountHolders, mock(AccountsApi.class),
                mock(CardBackofficeApi.class), mock(CardsApi.class), mock(LoanOriginationApi.class),
                mortgages, supplyChain, collateral, mock(CollectionCasesApi.class),
                mock(ProvisioningCasesApi.class), mock(AmlCasesApi.class));
    }

    @Test
    void mortgageUpdatePutsTheEmployeeOnTheAgreement() {
        UUID actor = UUID.fromString("33333333-3333-4333-8333-333333333333");
        RecordCommand command = new RecordCommand(FinanceArea.MORTGAGE_AGREEMENT, FinanceArea.MORTGAGE_AGREEMENT.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Correct the note", "Staff note", null, null, null, actor, null, null);
        MortgageAgreementDTO current = mortgage(MortgageAgreementDTO.AgreementStatusEnum.ACTIVE);
        when(mortgages.get(eq(command.recordId()), any())).thenReturn(Mono.just(current));
        when(mortgages.update(eq(command.recordId()), any(), any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(1)));

        StaffRecord updated = client.update(command).block();

        ArgumentCaptor<MortgageAgreementDTO> body = ArgumentCaptor.forClass(MortgageAgreementDTO.class);
        verify(mortgages).update(eq(command.recordId()), body.capture(), any());
        assertEquals(actor.toString(), body.getValue().getUpdatedBy());
        assertEquals(actor, updated.actorId());
        assertEquals(FinanceSamples.CUSTOMER_PARTY, updated.customerPartyId());
    }

    @Test
    void accountUpdatePostsTheCustomerAsHolder() {
        UUID actor = UUID.fromString("33333333-3333-4333-8333-333333333333");
        RecordCommand command = new RecordCommand(FinanceArea.ACCOUNT, FinanceArea.ACCOUNT.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Add the customer", null, null, null, null, actor, null, null);
        when(accountHolders.addHolder(eq(command.recordId()), any(), any())).thenReturn(Mono.just(new AddHolderResponse()));
        AccountDTO account = new AccountDTO();
        account.setAccountStatus(AccountDTO.AccountStatusEnum.OPEN);
        account.setAccountNumber("ACC-1");
        when(accountBackoffice.getAccount(eq(command.recordId()), any())).thenReturn(Mono.just(account));

        client.update(command).block();

        ArgumentCaptor<AddAccountHolderCommand> body = ArgumentCaptor.forClass(AddAccountHolderCommand.class);
        verify(accountHolders).addHolder(eq(command.recordId()), body.capture(), any());
        assertEquals(FinanceSamples.CUSTOMER_PARTY, body.getValue().getPartyId());
        assertEquals(FinanceSamples.HOLDER_ROLE, body.getValue().getRoleInContractId());
    }

    @Test
    void collateralClosePostsTheClosePath() {
        UUID actor = UUID.fromString("33333333-3333-4333-8333-333333333333");
        RecordCommand command = new RecordCommand(FinanceArea.COLLATERAL_CASE, FinanceArea.COLLATERAL_CASE.primaryId(),
                FinanceSamples.CUSTOMER_PARTY, "Release", null, null, null, null, actor, "RELEASED", null);
        CollateralCaseDTO closed = new CollateralCaseDTO();
        closed.setCollateralStatus(CollateralCaseDTO.CollateralStatusEnum.RELEASED);
        when(collateral.close(eq(command.recordId()), any(), any())).thenReturn(Mono.just(closed));

        StaffRecord result = client.close(command).block();

        ArgumentCaptor<ReasonRequest> body = ArgumentCaptor.forClass(ReasonRequest.class);
        verify(collateral).close(eq(command.recordId()), body.capture(), any());
        assertEquals("Release", body.getValue().getStatusReason());
        assertEquals("RELEASED", result.status());
    }

    @Test
    void supplyChainRemovalDeletesOnlyADraft() {
        SupplyChainFinanceAgreementDTO active = supplyChain(SupplyChainFinanceAgreementDTO.StatusEnum.ACTIVE);
        when(supplyChain.get(eq(FinanceSamples.SCF_ACTIVE), any())).thenReturn(Mono.just(active));
        RecordCommand activeCommand = RecordCommand.of(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT, FinanceSamples.SCF_ACTIVE,
                FinanceSamples.CUSTOMER_PARTY, "no");
        TerminalDeniedException denied = assertThrows(TerminalDeniedException.class,
                () -> client.removeDraft(activeCommand, "DRAFT").block());
        assertEquals("DRAFT_REQUIRED", denied.reasonCode());

        SupplyChainFinanceAgreementDTO draft = supplyChain(SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT);
        UUID draftId = FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT.primaryId();
        when(supplyChain.get(eq(draftId), any())).thenReturn(Mono.just(draft));
        when(supplyChain.remove(eq(draftId), any())).thenReturn(Mono.empty());
        RecordCommand draftCommand = RecordCommand.of(FinanceArea.SUPPLY_CHAIN_FINANCE_AGREEMENT, draftId,
                FinanceSamples.CUSTOMER_PARTY, "yes");
        client.removeDraft(draftCommand, "DRAFT").block();
        verify(supplyChain).remove(eq(draftId), any());
        assertTrue(draft.getStatus() == SupplyChainFinanceAgreementDTO.StatusEnum.DRAFT);
    }

    private static MortgageAgreementDTO mortgage(MortgageAgreementDTO.AgreementStatusEnum status) {
        MortgageAgreementDTO agreement = new MortgageAgreementDTO();
        agreement.setAgreementStatus(status);
        agreement.setApplicationId(UUID.fromString("10000000-0000-4000-8000-000000000099"));
        agreement.setMortgageType(MortgageAgreementDTO.MortgageTypeEnum.CONVENTIONAL);
        agreement.setRateType(MortgageAgreementDTO.RateTypeEnum.FIXED);
        return agreement;
    }

    private static SupplyChainFinanceAgreementDTO supplyChain(SupplyChainFinanceAgreementDTO.StatusEnum status) {
        SupplyChainFinanceAgreementDTO agreement = new SupplyChainFinanceAgreementDTO();
        agreement.setStatus(status);
        agreement.setAgreementNumber("SCF-1");
        agreement.setCurrency("EUR");
        agreement.setFinanceType(SupplyChainFinanceAgreementDTO.FinanceTypeEnum.FACTORING);
        agreement.setLoanServicingCaseId(UUID.fromString("10000000-0000-4000-8000-000000000098"));
        agreement.setStartDate(java.time.LocalDate.of(2026, 1, 1));
        return agreement;
    }
}
