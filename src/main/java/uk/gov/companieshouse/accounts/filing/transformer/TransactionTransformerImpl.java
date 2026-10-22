package uk.gov.companieshouse.accounts.filing.transformer;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.types.AccountsType;
import uk.gov.companieshouse.accounts.filing.utils.mapping.ImmutableConverter;
import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;

@Component
public class TransactionTransformerImpl implements TransactionTransformer {
    
    private String resourceKind;

    @Value("${revised.cic.accounts.flag}")
    private boolean isRevisedCicEnabled;

    @Override
    public void setupTransactionResources(final Transaction transaction, final String accountsFilingId, final String packageType, final AccountsFilingEntry accountsFilingEntry) {
        
        final var uri = String.format("/transactions/%s/accounts-filing/%s", transaction.getId(), accountsFilingId);
        
        Map<String, String> links = ImmutableConverter.toMutableMap(
            Map.of(
                "resource", uri,
                "validation_status", uri+"/validation-status"
            )
        );

        if(!isAmendedAccounts(accountsFilingEntry)){
            if(PackageTypeApi.CIC.toString().equals(packageType)  || PackageTypeApi.OVERSEAS.toString().equals(packageType)) {
                links.put("costs", uri + "/costs");
            }
        }
        final Resource resource = new Resource();
        resource.setKind(resourceKind);
        resource.setUpdatedAt(LocalDateTime.now());
        resource.setLinks(links);
        if (transaction.getResources() == null) {
            transaction.setResources(new HashMap<>());
        }
        transaction.getResources().put(uri, resource);
    }

    private boolean isAmendedAccounts(final AccountsFilingEntry accountsFilingEntry) {
        final String accountsType = accountsFilingEntry == null ? null : accountsFilingEntry.getAccountsType();
        if (accountsType == null) {
            return false;
        }
        return Arrays.stream(AccountsType.values())
                .filter(type -> type.name().endsWith("_AMENDED"))
                .anyMatch(type -> type.getStemCode().equals(accountsType) || type.getType().equals(accountsType));
    }
}
