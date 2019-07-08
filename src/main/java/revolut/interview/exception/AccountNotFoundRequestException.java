package revolut.interview.exception;

public class AccountNotFoundRequestException extends RuntimeException {
    public AccountNotFoundRequestException(Long accountId) {
        super(String.format("Account with id=%s not found", accountId));
    }
}
