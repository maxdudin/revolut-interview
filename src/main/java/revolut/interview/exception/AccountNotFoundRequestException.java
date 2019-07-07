package revolut.interview.exception;

import java.math.BigInteger;

public class AccountNotFoundRequestException extends RuntimeException {
    public AccountNotFoundRequestException(BigInteger accountId) {
        super(String.format("Account with id=%s not found", accountId));
    }
}
