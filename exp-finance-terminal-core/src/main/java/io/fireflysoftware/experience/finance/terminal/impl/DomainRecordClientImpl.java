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


package io.fireflysoftware.experience.finance.terminal.impl;

import io.fireflysoftware.experience.finance.terminal.ConfigurationPort;
import io.fireflysoftware.experience.finance.terminal.DecideMode;
import io.fireflysoftware.experience.finance.terminal.FinanceArea;
import io.fireflysoftware.experience.finance.terminal.RecordCommand;
import io.fireflysoftware.experience.finance.terminal.RecordOutcomeUnknownException;
import io.fireflysoftware.experience.finance.terminal.StaffArea;
import io.fireflysoftware.experience.finance.terminal.StaffRecord;
import io.fireflysoftware.experience.finance.terminal.StaffRecordPort;
import io.fireflysoftware.experience.finance.terminal.TerminalDeniedException;

import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountBackofficeApi;
import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountHoldersApi;
import io.fireflysoftware.domain.banking.accounts.sdk.api.AccountsApi;
import io.fireflysoftware.domain.banking.accounts.sdk.model.AccountDTO;
import io.fireflysoftware.domain.banking.accounts.sdk.model.AddAccountHolderCommand;
import io.fireflysoftware.domain.banking.cards.sdk.api.CardBackofficeApi;
import io.fireflysoftware.domain.banking.cards.sdk.api.CardsApi;
import io.fireflysoftware.domain.banking.cards.sdk.model.CardDTO;
import io.fireflysoftware.domain.banking.cards.sdk.model.UpdateSecuritySettingsRequest;
import io.fireflysoftware.domain.lending.collateral.sdk.api.CollateralCasesApi;
import io.fireflysoftware.domain.lending.collateral.sdk.model.CollateralCaseDTO;
import io.fireflysoftware.domain.lending.collections.sdk.api.CollectionCasesApi;
import io.fireflysoftware.domain.lending.collections.sdk.model.CollectionCaseDTO;
import io.fireflysoftware.domain.lending.compliance.sdk.api.AmlCasesApi;
import io.fireflysoftware.domain.lending.compliance.sdk.model.AmlCaseDTO;
import io.fireflysoftware.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import io.fireflysoftware.domain.lending.loan.origination.sdk.model.LoanApplicationDTO;
import io.fireflysoftware.domain.lending.loan.origination.sdk.model.UpdateApplicationEmploymentDataCommand;
import io.fireflysoftware.domain.lending.mortgage.sdk.api.MortgageAgreementsApi;
import io.fireflysoftware.domain.lending.mortgage.sdk.model.MortgageAgreementDTO;
import io.fireflysoftware.domain.lending.provisioning.sdk.api.ProvisioningCasesApi;
import io.fireflysoftware.domain.lending.provisioning.sdk.model.ProvisioningCaseDTO;
import io.fireflysoftware.domain.lending.supplychainfinance.sdk.api.SupplyChainFinanceAgreementsApi;
import io.fireflysoftware.domain.lending.supplychainfinance.sdk.model.SupplyChainFinanceAgreementDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Staff records go through the generated domain SDKs. Each domain reads and writes its core.
 */
public final class DomainRecordClientImpl implements StaffRecordPort {

    private final ConfigurationPort configuration;
    private final Set<String> configuredDomains;
    private final AccountBackofficeApi accountBackoffice;
    private final AccountHoldersApi accountHolders;
    private final AccountsApi accounts;
    private final CardBackofficeApi cardBackoffice;
    private final CardsApi cards;
    private final LoanOriginationApi loans;
    private final MortgageAgreementsApi mortgages;
    private final SupplyChainFinanceAgreementsApi supplyChain;
    private final CollateralCasesApi collateral;
    private final CollectionCasesApi collections;
    private final ProvisioningCasesApi provisioning;
    private final AmlCasesApi aml;

    public DomainRecordClientImpl(ConfigurationPort configuration,
                              Set<String> configuredDomains,
                              AccountBackofficeApi accountBackoffice,
                              AccountHoldersApi accountHolders,
                              AccountsApi accounts,
                              CardBackofficeApi cardBackoffice,
                              CardsApi cards,
                              LoanOriginationApi loans,
                              MortgageAgreementsApi mortgages,
                              SupplyChainFinanceAgreementsApi supplyChain,
                              CollateralCasesApi collateral,
                              CollectionCasesApi collections,
                              ProvisioningCasesApi provisioning,
                              AmlCasesApi aml) {
        this.configuration = configuration;
        this.configuredDomains = Set.copyOf(configuredDomains);
        this.accountBackoffice = accountBackoffice;
        this.accountHolders = accountHolders;
        this.accounts = accounts;
        this.cardBackoffice = cardBackoffice;
        this.cards = cards;
        this.loans = loans;
        this.mortgages = mortgages;
        this.supplyChain = supplyChain;
        this.collateral = collateral;
        this.collections = collections;
        this.provisioning = provisioning;
        this.aml = aml;
    }

    @Override
    public Flux<StaffRecord> list(StaffArea area) {
        require(area);
        return configuration.getRequired(area.recordIndexKey())
                .flatMapMany(csv -> Flux.fromArray(csv.split(",")))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .concatMap(token -> get(area, UUID.fromString(token)));
    }

    @Override
    public Mono<StaffRecord> get(StaffArea area, UUID recordId) {
        require(area);
        return switch ((FinanceArea) area) {
            case ACCOUNT -> accountBackoffice.getAccount(recordId, key())
                    .map(account -> fromAccount(area, recordId, account));
            case CARD -> cardBackoffice.getCard(recordId, key())
                    .map(card -> fromCard(area, recordId, card));
            case LENDING_APPLICATION -> loans.getApplication(recordId, key())
                    .map(application -> fromLoan(area, recordId, application));
            case MORTGAGE_AGREEMENT -> mortgages.get(recordId, key())
                    .map(agreement -> fromMortgage(area, recordId, agreement));
            case SUPPLY_CHAIN_FINANCE_AGREEMENT -> supplyChain.get(recordId, key())
                    .map(agreement -> fromSupplyChain(area, recordId, agreement));
            case COLLATERAL_CASE -> collateral.get(recordId, key())
                    .map(row -> fromCollateral(area, recordId, row));
            case COLLECTION_CASE -> collections.get(recordId, key())
                    .map(row -> fromCollection(area, recordId, row));
            case PROVISIONING_CASE -> provisioning.get(recordId, key())
                    .map(row -> fromProvisioning(area, recordId, row));
            case AML_CASE -> aml.get(recordId, key())
                    .map(row -> fromAml(area, recordId, row));
        };
    }

    @Override
    public Mono<StaffRecord> update(RecordCommand command) {
        require(command.area());
        FinanceArea area = (FinanceArea) command.area();
        String requestKey = key();
        return switch (area.updateMode()) {
            case PUT_DOCUMENT -> putDocument(area, command, requestKey);
            case ADD_HOLDER -> configuration.getRequired(area.holderRoleKey()).flatMap(roleId -> {
                AddAccountHolderCommand body = new AddAccountHolderCommand()
                        .accountId(command.recordId()).partyId(command.customerPartyId())
                        .roleInContractId(UUID.fromString(roleId));
                return mutationWithRead(command, requestKey, accountHolders.addHolder(command.recordId(), body, requestKey),
                        () -> accountBackoffice.getAccount(command.recordId(), key()),
                        account -> fromAccount(area, command.recordId(), account));
            });
            case CARD_SECURITY -> configuration.getRequired(area.cardSecurityKey()).flatMap(settingId -> {
                UpdateSecuritySettingsRequest body = new UpdateSecuritySettingsRequest()
                        .securitySettingId(UUID.fromString(settingId)).enabled(true);
                return mutationWithRead(command, requestKey, cardBackoffice.updateSecurity(command.recordId(), body, requestKey),
                        () -> cardBackoffice.getCard(command.recordId(), key()),
                        card -> fromCard(area, command.recordId(), card));
            });
            case EMPLOYMENT -> {
                UpdateApplicationEmploymentDataCommand body = new UpdateApplicationEmploymentDataCommand()
                        .applicationId(command.recordId()).employer(command.note() == null ? "" : command.note());
                yield observeLoanAfterMutation(command, requestKey,
                        loans.updateApplicationEmploymentData(command.recordId(), body, requestKey));
            }
        };
    }

    @Override
    public Mono<StaffRecord> close(RecordCommand command) {
        require(command.area());
        FinanceArea area = (FinanceArea) command.area();
        String reason = command.reason() == null ? "" : command.reason();
        String requestKey = key();
        return switch (area.closeMode()) {
            case JSON_SUFFIX -> closeDocument(area, command, reason, requestKey);
            case ACCOUNT_QUERY -> mutationWithRead(command, requestKey,
                    accounts.closeAccount(command.recordId(), reason, requestKey),
                    () -> accountBackoffice.getAccount(command.recordId(), key()),
                    account -> fromAccount(area, command.recordId(), account));
            case CARD_CANCEL -> mutationWithRead(command, requestKey,
                    cards.cancelCard(command.recordId(), reason, command.actorId().toString(), requestKey),
                    () -> cardBackoffice.getCard(command.recordId(), key()),
                    card -> fromCard(area, command.recordId(), card));
            case APPLICATION_WITHDRAW -> observeLoanAfterMutation(command, requestKey,
                    loans.withdrawApplication(command.recordId(), requestKey));
        };
    }

    @Override
    public Mono<Void> removeDraft(RecordCommand command, String requiredStatus) {
        if (!command.area().draftRemoval()) {
            return Mono.error(new TerminalDeniedException("REMOVAL_NOT_APPLICABLE"));
        }
        require(command.area());
        if (!"DRAFT".equals(requiredStatus)) {
            return Mono.error(new TerminalDeniedException("DRAFT_REQUIRED"));
        }
        return statusOf(command.area(), command.recordId()).flatMap(status -> {
            if (!requiredStatus.equals(status)) {
                return Mono.error(new TerminalDeniedException("DRAFT_REQUIRED"));
            }
            String requestKey = key();
            return supplyChain.remove(command.recordId(), requestKey)
                    .onErrorMap(error -> refusalOrUnknown(command, requestKey, error));
        });
    }

    @Override
    public Mono<StaffRecord> decide(RecordCommand command) {
        require(command.area());
        if (command.area().decideMode() != DecideMode.APPROVE_OR_REJECT) {
            return Mono.error(new TerminalDeniedException("DECISION_NOT_APPLICABLE"));
        }
        String requestKey = key();
        if ("APPROVE".equalsIgnoreCase(command.decision())) {
            return observeLoanAfterMutation(command, requestKey, loans.approveApplication(command.recordId(), requestKey));
        }
        if ("REJECT".equalsIgnoreCase(command.decision())) {
            return observeLoanAfterMutation(command, requestKey, loans.rejectApplication(command.recordId(), requestKey));
        }
        return Mono.error(new TerminalDeniedException("DECISION_NOT_APPLICABLE"));
    }

    private Mono<StaffRecord> putDocument(FinanceArea area, RecordCommand command, String requestKey) {
        UUID id = command.recordId();
        return switch (area) {
            case MORTGAGE_AGREEMENT -> mortgages.get(id, key()).flatMap(current -> {
                fromMortgage(area, id, current);
                if (command.actorId() != null) current.setUpdatedBy(command.actorId().toString());
                return mutation(command, requestKey, mortgages.update(id, current, requestKey),
                        saved -> fromMortgage(area, id, saved));
            });
            case SUPPLY_CHAIN_FINANCE_AGREEMENT -> supplyChain.get(id, key()).flatMap(current -> {
                fromSupplyChain(area, id, current);
                if (command.actorId() != null) current.setLastModifiedBy(command.actorId());
                if (command.note() != null) current.setNotes(command.note());
                return mutation(command, requestKey, supplyChain.update(id, current, requestKey),
                        saved -> fromSupplyChain(area, id, saved));
            });
            case COLLATERAL_CASE -> collateral.get(id, key()).flatMap(current -> {
                fromCollateral(area, id, current);
                if (command.note() != null) current.setRemarks(command.note());
                return mutation(command, requestKey, collateral.update(id, current, requestKey),
                        saved -> fromCollateral(area, id, saved));
            });
            case COLLECTION_CASE -> collections.get(id, key()).flatMap(current -> {
                fromCollection(area, id, current);
                if (command.note() != null) current.setRemarks(command.note());
                return mutation(command, requestKey, collections.update(id, current, requestKey),
                        saved -> fromCollection(area, id, saved));
            });
            case PROVISIONING_CASE -> provisioning.get(id, key()).flatMap(current -> {
                fromProvisioning(area, id, current);
                if (command.note() != null) current.setRemarks(command.note());
                return mutation(command, requestKey, provisioning.update(id, current, requestKey),
                        saved -> fromProvisioning(area, id, saved));
            });
            case AML_CASE -> aml.get(id, key()).flatMap(current -> {
                fromAml(area, id, current);
                if (command.customerPartyId() != null) current.setCustomerId(command.customerPartyId());
                if (command.note() != null) current.setNotes(command.note());
                return mutation(command, requestKey, aml.update(id, current, requestKey),
                        saved -> fromAml(area, id, saved));
            });
            default -> Mono.error(new TerminalDeniedException("UPDATE_NOT_APPLICABLE"));
        };
    }

    private Mono<StaffRecord> closeDocument(FinanceArea area, RecordCommand command, String reason, String requestKey) {
        UUID id = command.recordId();
        return switch (area) {
            case MORTGAGE_AGREEMENT -> mutation(command, requestKey, mortgages.close(id,
                    new io.fireflysoftware.domain.lending.mortgage.sdk.model.ReasonRequest().statusReason(reason), requestKey),
                    saved -> fromMortgage(area, id, saved));
            case SUPPLY_CHAIN_FINANCE_AGREEMENT -> mutation(command, requestKey, supplyChain.close(id,
                    new io.fireflysoftware.domain.lending.supplychainfinance.sdk.model.ReasonRequest().statusReason(reason), requestKey),
                    saved -> fromSupplyChain(area, id, saved));
            case COLLATERAL_CASE -> mutation(command, requestKey, collateral.close(id,
                    new io.fireflysoftware.domain.lending.collateral.sdk.model.ReasonRequest().statusReason(reason), requestKey),
                    saved -> fromCollateral(area, id, saved));
            case COLLECTION_CASE -> mutation(command, requestKey, collections.close(id,
                    new io.fireflysoftware.domain.lending.collections.sdk.model.ReasonRequest().statusReason(reason), requestKey),
                    saved -> fromCollection(area, id, saved));
            case PROVISIONING_CASE -> mutation(command, requestKey, provisioning.close(id,
                    new io.fireflysoftware.domain.lending.provisioning.sdk.model.ReasonRequest().statusReason(reason), requestKey),
                    saved -> fromProvisioning(area, id, saved));
            case AML_CASE -> mutation(command, requestKey, aml.close(id,
                    new io.fireflysoftware.domain.lending.compliance.sdk.model.ReasonRequest().statusReason(reason), requestKey),
                    saved -> fromAml(area, id, saved));
            default -> Mono.error(new TerminalDeniedException("CLOSE_NOT_APPLICABLE"));
        };
    }

    private Mono<String> statusOf(StaffArea area, UUID recordId) {
        return switch ((FinanceArea) area) {
            case SUPPLY_CHAIN_FINANCE_AGREEMENT -> supplyChain.get(recordId, key())
                    .map(row -> fromSupplyChain(area, recordId, row).status())
                    .switchIfEmpty(Mono.error(new TerminalDeniedException("CURRENT_OWNER_RECORD_REQUIRED")));
            default -> Mono.error(new TerminalDeniedException("REMOVAL_NOT_APPLICABLE"));
        };
    }

    private static StaffRecord fromAccount(StaffArea area, UUID id, AccountDTO account) {
        return ownerRecord(area, id, account.getAccountId(), null, null, code(account.getAccountStatus()),
                account.getAccountNumber(), null, null);
    }

    private static StaffRecord fromCard(StaffArea area, UUID id, CardDTO card) {
        return ownerRecord(area, id, card.getCardId(), card.getPartyId(), null, code(card.getCardStatus()),
                card.getMaskedCardNumber(), null, card.getNotes());
    }

    private static StaffRecord fromLoan(StaffArea area, UUID id, LoanApplicationDTO application) {
        UUID statusId = application.getApplicationStatusId();
        if (statusId == null || statusId.equals(new UUID(0, 0))) {
            throw new IllegalStateException("Current owner loan status identity required");
        }
        String reference = application.getApplicationNumber() == null ? null : application.getApplicationNumber().toString();
        // This is the real owner status identity until the Domain catalogue projection is available.
        return ownerRecord(area, id, application.getLoanApplicationId(), null, null, statusId.toString(),
                reference, application.getDistributorId(), application.getNote());
    }

    private static StaffRecord fromMortgage(StaffArea area, UUID id, MortgageAgreementDTO agreement) {
        return ownerRecord(area, id, agreement.getMortgageAgreementId(), null, uuidOrNull(agreement.getUpdatedBy()),
                code(agreement.getAgreementStatus()), agreement.getDeedNumber(), null, null);
    }

    private static StaffRecord fromSupplyChain(StaffArea area, UUID id, SupplyChainFinanceAgreementDTO agreement) {
        return ownerRecord(area, id, agreement.getId(), null, agreement.getLastModifiedBy(),
                code(agreement.getStatus()), agreement.getAgreementNumber(), null, agreement.getNotes());
    }

    private static StaffRecord fromCollateral(StaffArea area, UUID id, CollateralCaseDTO row) {
        return ownerRecord(area, id, row.getCollateralCaseId(), null, null, code(row.getCollateralStatus()),
                row.getReferenceNumber(), null, row.getRemarks());
    }

    private static StaffRecord fromCollection(StaffArea area, UUID id, CollectionCaseDTO row) {
        return ownerRecord(area, id, row.getCollectionCaseId(), null, null, code(row.getCollectionStatus()), null, null, row.getRemarks());
    }

    private static StaffRecord fromProvisioning(StaffArea area, UUID id, ProvisioningCaseDTO row) {
        return ownerRecord(area, id, row.getProvisioningCaseId(), null, null, code(row.getProvisioningStatus()), null, null, row.getRemarks());
    }

    private static StaffRecord fromAml(StaffArea area, UUID id, AmlCaseDTO row) {
        return ownerRecord(area, id, row.getAmlCaseId(), row.getCustomerId(), null, code(row.getAmlCaseStatus()), null, null, row.getNotes());
    }

    private static StaffRecord ownerRecord(StaffArea area, UUID expected, UUID actual, UUID customer, UUID actor,
                                          String status, String reference, UUID distributor, String note) {
        requireOwnerId(expected, actual);
        if (status == null || status.isBlank()) {
            throw new IllegalStateException("Current complete owner record required");
        }
        return new StaffRecord(actual, area.name(), customer, actor, status, reference, distributor, note);
    }

    private static void requireOwnerId(UUID expected, UUID actual) {
        if (expected == null || actual == null || !expected.equals(actual) || actual.equals(new UUID(0, 0))) {
            throw new IllegalStateException("Matching current owner record required");
        }
    }

    private <T> Mono<StaffRecord> observeLoanAfterMutation(RecordCommand original, String requestKey, Mono<T> invocation) {
        var checked = invocation.doOnNext(reply -> {
            if (reply instanceof java.util.Map<?, ?> value && value.isEmpty()) {
                throw new RecordOutcomeUnknownException(original, requestKey);
            }
        }).switchIfEmpty(Mono.error(new RecordOutcomeUnknownException(original, requestKey)));
        return mutationWithRead(original, requestKey, checked,
                () -> loans.getApplication(original.recordId(), key()),
                application -> fromLoan(original.area(), original.recordId(), application));
    }

    private static <T> Mono<StaffRecord> mutation(RecordCommand original, String requestKey, Mono<T> invocation,
                                                 Function<T, StaffRecord> mapping) {
        return invocation.map(mapping).switchIfEmpty(Mono.error(new RecordOutcomeUnknownException(original, requestKey)))
                .onErrorMap(error -> refusalOrUnknown(original, requestKey, error));
    }

    private static <T> Mono<StaffRecord> mutationWithRead(RecordCommand original, String requestKey, Mono<?> invocation,
                                                         Supplier<Mono<T>> currentRead, Function<T, StaffRecord> mapping) {
        return invocation.onErrorMap(error -> refusalOrUnknown(original, requestKey, error))
                .then(Mono.defer(() -> currentRead.get().map(mapping)
                        .switchIfEmpty(Mono.error(new RecordOutcomeUnknownException(original, requestKey))))
                        .onErrorMap(error -> error instanceof RecordOutcomeUnknownException
                                ? error : new RecordOutcomeUnknownException(original, requestKey)));
    }

    private static Throwable refusalOrUnknown(RecordCommand original, String requestKey, Throwable error) {
        if (error instanceof RecordOutcomeUnknownException) return error;
        if (error instanceof WebClientResponseException response
                && Set.of(400, 401, 403, 404, 409, 422).contains(response.getStatusCode().value())) {
            return new ResponseStatusException(response.getStatusCode(), "Current owner invocation refused");
        }
        return new RecordOutcomeUnknownException(original, requestKey);
    }

    private void require(StaffArea area) {
        if (!configuredDomains.contains(area.domainKey())) {
            throw new TerminalDeniedException("PROPERTY_NOT_SET");
        }
    }

    private static String code(Enum<?> value) {
        if (value == null) {
            return "";
        }
        try {
            Object coded = value.getClass().getMethod("getValue").invoke(value);
            return coded == null ? value.name() : coded.toString();
        } catch (ReflectiveOperationException exception) {
            return value.name();
        }
    }

    private static UUID uuidOrNull(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String key() {
        return UUID.randomUUID().toString();
    }
}
