package uk.gov.companieshouse.accounts.filing.transformer;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.accounts.filing.utils.mapping.ImmutableConverter;
import uk.gov.companieshouse.api.model.transaction.Resource;
import uk.gov.companieshouse.api.model.transaction.Transaction;

@Component
public class TransactionTransformerImpl implements TransactionTransformer {
    
    private String resourceKind;


    @Override
    public void setupTransactionResources(final Transaction transaction, final String accountsFilingId) {
        
        final var uri = String.format("/transactions/%s/account-filing/%s", transaction.getId(), accountsFilingId);
        
        Map<String, String> links = ImmutableConverter.toMutableMap(
            Map.of(
                "costs", uri+"/costs",
                "resource", uri,
                "validation_status", uri+"/validation-status"
            )
        );
        
        final Resource resource = new Resource();
        resource.setKind(resourceKind);
        resource.setUpdatedAt(LocalDateTime.now());
        resource.setLinks(links);
        transaction.getResources().put(uri, resource);
    }
}
