package revolut.interview.database.mybatis.mapper;

import org.apache.ibatis.annotations.*;
import revolut.interview.database.entity.Account;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public interface AccountMapper {
    @Select("select * from account where id=#{id}")
    @Results(id = "accountResultMap", value = {
            @Result(property = "accountId", column = "id", id = true),
            @Result(property = "balance", column = "balance")
    })
    Account findById(Long id);

    @Insert("insert into account(balance) values(#{balance})")
    @Options(useGeneratedKeys = true)
    void save(Account account);

    @Update("update account set balance=#{balance} where id=#{id}")
    void setBalance(Long id, @NotNull @PositiveOrZero BigDecimal balance);

    @Select("select * from account")
    @ResultMap("accountResultMap")
    List<Account> findAll();
}
