package revolut.interview.controller.dto;

import java.math.BigDecimal;

public class CreateAccountRequest {
    private BigDecimal initialBalance;

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}
