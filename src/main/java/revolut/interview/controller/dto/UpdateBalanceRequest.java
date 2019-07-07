package revolut.interview.controller.dto;

import java.math.BigDecimal;
import java.math.BigInteger;

public class UpdateBalanceRequest {
    private BigInteger accountId;
    private BigDecimal balance;

    public BigInteger getAccountId() {
        return accountId;
    }

    public void setAccountId(BigInteger accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
