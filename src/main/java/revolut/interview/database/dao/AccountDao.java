package revolut.interview.database.dao;

import revolut.interview.database.entity.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {
    Account getAccount(Long id);
    List<Account> getAllAccounts();
    void updateBalance(Long id, BigDecimal balance);
    void saveAccount(Account account);
}
