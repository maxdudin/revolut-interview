package revolut.interview.database.dao;

import revolut.interview.database.entity.Account;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface AccountDao {
    Account getAccount(BigInteger id);
    List<Account> getAllAccounts();
    void updateBalance(BigInteger id, BigDecimal balance);
    void saveAccount(Account account);
}
