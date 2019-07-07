package revolut.interview.database.mybatis.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import revolut.interview.database.entity.Account;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface AccountMapper {
    @Select("select * from account where id=#{id}")
    Account findById(BigInteger id);

    @Insert("insert into account(balance) values(#{balance})")
    @Options(useGeneratedKeys = true)
    void save(Account account);

    @Update("update account set balance=#{balance} where id=#{id}")
    void setBalance(BigInteger id, @NotNull @PositiveOrZero BigDecimal balance);

    @Select("select * from account")
    List<Account> findAll();
}
