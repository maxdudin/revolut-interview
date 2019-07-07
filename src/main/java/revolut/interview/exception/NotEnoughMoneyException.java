package revolut.interview.exception;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NotEnoughMoneyException extends RuntimeException {
    public NotEnoughMoneyException(BigInteger accountId, BigDecimal amount) {
        super(String.format("Not enough money for account with id=%s to send %s", accountId.toString(), amount.toString()));
    }
}
