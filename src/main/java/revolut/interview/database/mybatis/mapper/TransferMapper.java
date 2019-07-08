package revolut.interview.database.mybatis.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import revolut.interview.database.entity.Transfer;

import java.math.BigInteger;
import java.util.List;

public interface TransferMapper {
    @Select("select * from transfer")
    List<Transfer> findAll();

    @Insert("insert into transfer (from, to, amount) values (#{from}, #{to}, #{amount})")
    void save(Transfer transfer);

    @Select("select * from transfer where id=#{id}")
    Transfer getTransfer(BigInteger id);
}
