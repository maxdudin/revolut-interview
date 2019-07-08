package revolut.interview.exception;

import java.math.BigDecimal;

public class NotEnoughMoneyException extends RuntimeException {
    public NotEnoughMoneyException(Long accountId, BigDecimal amount) {
        super(String.format("Not enough money for account with id=%s to send %s", accountId.toString(), amount.toString()));
    }
}
