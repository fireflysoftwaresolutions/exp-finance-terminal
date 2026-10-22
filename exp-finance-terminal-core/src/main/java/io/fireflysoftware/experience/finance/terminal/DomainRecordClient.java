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
import io.fireflysoftware.domain.lending.loan.origination.sdk.model.ApplicationPartyDTO;
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

/**
 * Staff records go through the generated domain SDKs. Each domain reads and writes its core.
 */
public final class DomainRecordClient implements StaffRecordPort {

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

    public DomainRecordClient(ConfigurationPort configuration,
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
                    .map(account -> fromAccount(area, recordId, account, null));
            case CARD -> cardBackoffice.getCard(recordId, key())
                    .map(card -> fromCard(area, recordId, card, null));
            case LENDING_APPLICATION -> loans.getApplication(recordId, key())
                    .map(application -> fromLoan(area, recordId, application, null));
            case MORTGAGE_AGREEMENT -> mortgages.get(recordId, key())
                    .map(agreement -> fromMortgage(area, recordId, agreement, null));
            case SUPPLY_CHAIN_FINANCE_AGREEMENT -> supplyChain.get(recordId, key())
                    .map(agreement -> fromSupplyChain(area, recordId, agreement, null));
            case COLLATERAL_CASE -> collateral.get(recordId, key())
                    .map(row -> fromCollateral(area, recordId, row, null));
            case COLLECTION_CASE -> collections.get(recordId, key())
                    .map(row -> fromCollection(area, recordId, row, null));
            case PROVISIONING_CASE -> provisioning.get(recordId, key())
                    .map(row -> fromProvisioning(area, recordId, row, null));
            case AML_CASE -> aml.get(recordId, key())
                    .map(row -> fromAml(area, recordId, row, null));
        };
    }

    @Override
    public Mono<StaffRecord> update(RecordCommand command) {
        require(command.area());
        FinanceArea area = (FinanceArea) command.area();
        return switch (area.updateMode()) {
            case PUT_DOCUMENT -> putDocument(area, command);
            case ADD_HOLDER -> configuration.getRequired(area.holderRoleKey()).flatMap(roleId -> {
                AddAccountHolderCommand body = new AddAccountHolderCommand()
                        .accountId(command.recordId())
                        .partyId(command.customerPartyId())
                        .roleInContractId(UUID.fromString(roleId));
                return accountHolders.addHolder(command.recordId(), body, key())
                        .then(accountBackoffice.getAccount(command.recordId(), key()))
                        .map(account -> fromAccount(area, command.recordId(), account, command));
            });
            case CARD_SECURITY -> configuration.getRequired(area.cardSecurityKey()).flatMap(settingId -> {
                UpdateSecuritySettingsRequest body = new UpdateSecuritySettingsRequest()
                        .securitySettingId(UUID.fromString(settingId))
                        .enabled(true);
                return cardBackoffice.updateSecurity(command.recordId(), body, key())
                        .then(cardBackoffice.getCard(command.recordId(), key()))
                        .map(card -> fromCard(area, command.recordId(), card, command));
            });
            case EMPLOYMENT -> {
                UpdateApplicationEmploymentDataCommand body = new UpdateApplicationEmploymentDataCommand()
                        .applicationId(command.recordId())
                        .employer(command.note() == null ? "" : command.note());
                yield loans.updateApplicationEmploymentData(command.recordId(), body, key())
                        .map(party -> fromEmployment(area, command, party));
            }
        };
    }

    @Override
    public Mono<StaffRecord> close(RecordCommand command) {
        require(command.area());
        FinanceArea area = (FinanceArea) command.area();
        String reason = command.reason() == null ? "" : command.reason();
        return switch (area.closeMode()) {
            case JSON_SUFFIX -> closeDocument(area, command, reason);
            case ACCOUNT_QUERY -> accounts.closeAccount(command.recordId(), reason, key())
                    .thenReturn(closed(command));
            case CARD_CANCEL -> cards.cancelCard(command.recordId(), reason, command.actorId().toString(), key())
                    .thenReturn(closed(command));
            case APPLICATION_WITHDRAW -> loans.withdrawApplication(command.recordId(), key())
                    .thenReturn(closed(command));
        };
    }

    @Override
    public Mono<Void> removeDraft(RecordCommand command, String requiredStatus) {
        if (!command.area().draftRemoval()) {
            return Mono.error(new TerminalDeniedException("REMOVAL_NOT_APPLICABLE"));
        }
        require(command.area());
        return statusOf(command.area(), command.recordId()).flatMap(status -> {
            String current = status.isBlank() ? "DRAFT" : status;
            if (!requiredStatus.equals(current)) {
                return Mono.error(new TerminalDeniedException("DRAFT_REQUIRED"));
            }
            return supplyChain.remove(command.recordId(), key());
        });
    }

    @Override
    public Mono<StaffRecord> decide(RecordCommand command) {
        if (command.area().decideMode() == DecideMode.NONE) {
            return Mono.error(new TerminalDeniedException("DECISION_NOT_APPLICABLE"));
        }
        require(command.area());
        if (command.area().decideMode() != DecideMode.APPROVE_OR_REJECT) {
            return Mono.error(new TerminalDeniedException("DECISION_NOT_APPLICABLE"));
        }
        Mono<?> call = "APPROVE".equalsIgnoreCase(command.decision())
                ? loans.approveApplication(command.recordId(), key())
                : loans.rejectApplication(command.recordId(), key());
        String status = SandboxRecordBook.decisionStatus(command);
        return call.thenReturn(new StaffRecord(command.recordId(), command.area().name(),
                command.customerPartyId(), command.actorId(), status, command.reason(),
                command.distributorId(), command.note()));
    }

    private Mono<StaffRecord> putDocument(FinanceArea area, RecordCommand command) {
        UUID id = command.recordId();
        return switch (area) {
            case MORTGAGE_AGREEMENT -> mortgages.get(id, key()).flatMap(current -> {
                if (command.actorId() != null) {
                    current.setUpdatedBy(command.actorId().toString());
                }
                return mortgages.update(id, current, key())
                        .map(saved -> fromMortgage(area, id, saved, command));
            });
            case SUPPLY_CHAIN_FINANCE_AGREEMENT -> supplyChain.get(id, key()).flatMap(current -> {
                if (command.actorId() != null) {
                    current.setLastModifiedBy(command.actorId());
                }
                if (command.note() != null) {
                    current.setNotes(command.note());
                }
                return supplyChain.update(id, current, key())
                        .map(saved -> fromSupplyChain(area, id, saved, command));
            });
            case COLLATERAL_CASE -> collateral.get(id, key()).flatMap(current -> {
                if (command.note() != null) {
                    current.setRemarks(command.note());
                }
                return collateral.update(id, current, key())
                        .map(saved -> fromCollateral(area, id, saved, command));
            });
            case COLLECTION_CASE -> collections.get(id, key()).flatMap(current -> {
                if (command.note() != null) {
                    current.setRemarks(command.note());
                }
                return collections.update(id, current, key())
                        .map(saved -> fromCollection(area, id, saved, command));
            });
            case PROVISIONING_CASE -> provisioning.get(id, key()).flatMap(current -> {
                if (command.note() != null) {
                    current.setRemarks(command.note());
                }
                return provisioning.update(id, current, key())
                        .map(saved -> fromProvisioning(area, id, saved, command));
            });
            case AML_CASE -> aml.get(id, key()).flatMap(current -> {
                if (command.customerPartyId() != null) {
                    current.setCustomerId(command.customerPartyId());
                }
                if (command.note() != null) {
                    current.setNotes(command.note());
                }
                return aml.update(id, current, key())
                        .map(saved -> fromAml(area, id, saved, command));
            });
            default -> Mono.error(new TerminalDeniedException("UPDATE_NOT_APPLICABLE"));
        };
    }

    private Mono<StaffRecord> closeDocument(FinanceArea area, RecordCommand command, String reason) {
        UUID id = command.recordId();
        return switch (area) {
            case MORTGAGE_AGREEMENT -> {
                var body = new io.fireflysoftware.domain.lending.mortgage.sdk.model.ReasonRequest().statusReason(reason);
                yield mortgages.close(id, body, key()).map(saved -> fromMortgage(area, id, saved, command));
            }
            case SUPPLY_CHAIN_FINANCE_AGREEMENT -> {
                var body = new io.fireflysoftware.domain.lending.supplychainfinance.sdk.model.ReasonRequest().statusReason(reason);
                yield supplyChain.close(id, body, key()).map(saved -> fromSupplyChain(area, id, saved, command));
            }
            case COLLATERAL_CASE -> {
                var body = new io.fireflysoftware.domain.lending.collateral.sdk.model.ReasonRequest().statusReason(reason);
                yield collateral.close(id, body, key()).map(saved -> fromCollateral(area, id, saved, command));
            }
            case COLLECTION_CASE -> {
                var body = new io.fireflysoftware.domain.lending.collections.sdk.model.ReasonRequest().statusReason(reason);
                yield collections.close(id, body, key()).map(saved -> fromCollection(area, id, saved, command));
            }
            case PROVISIONING_CASE -> {
                var body = new io.fireflysoftware.domain.lending.provisioning.sdk.model.ReasonRequest().statusReason(reason);
                yield provisioning.close(id, body, key()).map(saved -> fromProvisioning(area, id, saved, command));
            }
            case AML_CASE -> {
                var body = new io.fireflysoftware.domain.lending.compliance.sdk.model.ReasonRequest().statusReason(reason);
                yield aml.close(id, body, key()).map(saved -> fromAml(area, id, saved, command));
            }
            default -> Mono.error(new TerminalDeniedException("CLOSE_NOT_APPLICABLE"));
        };
    }

    private Mono<String> statusOf(StaffArea area, UUID recordId) {
        return switch ((FinanceArea) area) {
            case SUPPLY_CHAIN_FINANCE_AGREEMENT -> supplyChain.get(recordId, key())
                    .map(row -> code(row.getStatus()));
            default -> Mono.error(new TerminalDeniedException("REMOVAL_NOT_APPLICABLE"));
        };
    }

    private static StaffRecord fromAccount(StaffArea area, UUID id, AccountDTO account, RecordCommand command) {
        return overlay(area, id, null, null, code(account.getAccountStatus()), account.getAccountNumber(), null, null, command);
    }

    private static StaffRecord fromCard(StaffArea area, UUID id, CardDTO card, RecordCommand command) {
        String reference = card.getMaskedCardNumber() == null || card.getMaskedCardNumber().isBlank()
                ? card.getCardNumber() : card.getMaskedCardNumber();
        return overlay(area, id, card.getPartyId(), null, code(card.getCardStatus()), reference, null, card.getNotes(), command);
    }

    private static StaffRecord fromLoan(StaffArea area, UUID id, LoanApplicationDTO application, RecordCommand command) {
        String reference = application.getApplicationNumber() == null ? "" : application.getApplicationNumber().toString();
        return overlay(area, id, null, null, "", reference,
                application.getDistributorId(), application.getNote(), command);
    }

    private static StaffRecord fromEmployment(StaffArea area, RecordCommand command, ApplicationPartyDTO party) {
        UUID customer = party.getPartyId() == null ? command.customerPartyId() : party.getPartyId();
        return new StaffRecord(command.recordId(), area.name(), customer, command.actorId(), "",
                party.getEmployer(), command.distributorId(), command.note() == null ? party.getEmployer() : command.note());
    }

    private static StaffRecord fromMortgage(StaffArea area, UUID id, MortgageAgreementDTO agreement, RecordCommand command) {
        UUID actor = uuidOrNull(agreement.getUpdatedBy());
        return overlay(area, id, null, actor, code(agreement.getAgreementStatus()), agreement.getDeedNumber(), null, null, command);
    }

    private static StaffRecord fromSupplyChain(StaffArea area, UUID id, SupplyChainFinanceAgreementDTO agreement, RecordCommand command) {
        return overlay(area, id, null, agreement.getLastModifiedBy(), code(agreement.getStatus()),
                agreement.getAgreementNumber(), null, agreement.getNotes(), command);
    }

    private static StaffRecord fromCollateral(StaffArea area, UUID id, CollateralCaseDTO row, RecordCommand command) {
        return overlay(area, id, null, null, code(row.getCollateralStatus()), row.getReferenceNumber(), null, row.getRemarks(), command);
    }

    private static StaffRecord fromCollection(StaffArea area, UUID id, CollectionCaseDTO row, RecordCommand command) {
        return overlay(area, id, null, null, code(row.getCollectionStatus()), null, null, row.getRemarks(), command);
    }

    private static StaffRecord fromProvisioning(StaffArea area, UUID id, ProvisioningCaseDTO row, RecordCommand command) {
        return overlay(area, id, null, null, code(row.getProvisioningStatus()), null, null, row.getRemarks(), command);
    }

    private static StaffRecord fromAml(StaffArea area, UUID id, AmlCaseDTO row, RecordCommand command) {
        return overlay(area, id, row.getCustomerId(), null, code(row.getAmlCaseStatus()), null, null, row.getNotes(), command);
    }

    private static StaffRecord closed(RecordCommand command) {
        return new StaffRecord(command.recordId(), command.area().name(), command.customerPartyId(), command.actorId(),
                command.status(), command.reason(), command.distributorId(), command.note());
    }

    private static StaffRecord overlay(StaffArea area, UUID id, UUID customer, UUID actor, String status,
                                       String reference, UUID distributor, String note, RecordCommand command) {
        if (command == null) {
            return new StaffRecord(id, area.name(), customer, actor, status, reference, distributor, note);
        }
        UUID nextDistributor = command.distributorId() != null ? command.distributorId() : distributor;
        String nextNote = command.note() != null ? command.note() : note;
        String nextStatus = status == null || status.isBlank() ? command.status() : status;
        return new StaffRecord(id, area.name(), command.customerPartyId(), command.actorId(), nextStatus, reference,
                nextDistributor, nextNote);
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
