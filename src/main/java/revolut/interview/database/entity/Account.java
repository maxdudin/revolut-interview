package revolut.interview.database.entity;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.StringJoiner;

public class Account {
    private BigInteger accountId;

    @NotNull
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (!getAccountId().equals(account.getAccountId())) return false;
        return getBalance().equals(account.getBalance());

    }

    @Override
    public int hashCode() {
        int result = getAccountId().hashCode();
        result = 31 * result + getBalance().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Account.class.getSimpleName() + "[", "]")
                .add("accountId=" + accountId)
                .add("balance=" + balance)
                .toString();
    }
}
