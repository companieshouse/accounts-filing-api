package uk.gov.companieshouse.accounts.filing.service.company;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.accounts.filing.model.AccountsFilingEntry;
import uk.gov.companieshouse.accounts.filing.model.CompanyResponse;
import uk.gov.companieshouse.accounts.filing.repository.AccountsFilingRepository;
import uk.gov.companieshouse.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {
    @Mock
    AccountsFilingRepository accountsFilingRepository;
    @Mock
    Logger logger;

    CompanyServiceImpl service;

    @BeforeEach
    void setUp(){
        service = new CompanyServiceImpl(
                logger, accountsFilingRepository);
    }

    @Test
    @DisplayName("When company number and transactionId are save to the repository")
    void testSaveCompanyNumberAndTransactionId() {
        AccountsFilingEntry mockEntry = new AccountsFilingEntry("abc123", null, null,null, "12345", "test123", null, null);
        when(accountsFilingRepository.save(any(AccountsFilingEntry.class))).thenReturn(mockEntry);
        CompanyResponse mockAccountsFilingId = service.saveCompanyNumberAndTransactionId("12345", "test123");
        assertEquals(mockEntry.getAccountsFilingId(), mockAccountsFilingId.accountsFilingId());
    }

    @Test
    @DisplayName("When company number and transactionId are failed to save to the repository")
    void testFailedToSaveCompanyNumberAndTransactionId() {
        when(accountsFilingRepository.save(any(AccountsFilingEntry.class))).thenThrow(new RuntimeException("mockException"));
        RuntimeException exception = assertThrows(RuntimeException.class,() -> service.saveCompanyNumberAndTransactionId("12345", "test123"));
        assertEquals("mockException", exception.getMessage());
    }
}
