package uk.gov.companieshouse.accounts.filing.model.types;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class AccountsTypeTest {
    

    @Test
    @DisplayName("with valid AccountsType")
    void testValidAccountsTypeFULL() {
        AccountsType resultAccountsType = AccountsType.fromStemCode("01");
        assertEquals(AccountsType.AUDITED_FULL, resultAccountsType);
    }

    @Test
    @DisplayName("with invalid AccountsType")
    void testInvalidAccountsType() {
        AccountsType resultAccountsType = AccountsType.fromStemCode("null");
        assertEquals(AccountsType.UNKNOWN, resultAccountsType);
    }

    @Test
    @DisplayName("AccountsType return the right value for stemCode and Type")
    void testCorrectAccountType() {
        assertEquals("AUDITED FULL", AccountsType.AUDITED_FULL.getType());
        assertEquals("01", AccountsType.AUDITED_FULL.getStemCode());
    }

}
