package revolut.interview.database.dao;

import io.micronaut.validation.Validated;
import revolut.interview.database.entity.Account;

import javax.validation.Valid;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

@Validated
public interface AccountDao {
    Account getAccount(Long id);
    List<Account> getAllAccounts();
    void updateBalance(Long id, @PositiveOrZero BigDecimal balance);
    void saveAccount(@Valid Account account);
}
