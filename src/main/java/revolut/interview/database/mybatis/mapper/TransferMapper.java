package revolut.interview.database.mybatis.mapper;

import org.apache.ibatis.annotations.*;
import revolut.interview.database.entity.Transfer;

import java.util.List;

public interface TransferMapper {
    @Select("select * from transfer")
    @ResultMap("transferResultMap")
    List<Transfer> findAll();

    @Insert("insert into transfer (from_acc, to_acc, amount) values (#{from}, #{to}, #{amount})")
    void save(Transfer transfer);

    @Select("select * from transfer where id=#{id}")
    @Results(id = "transferResultMap", value = {
            @Result(property = "transferId", column = "id", id = true),
            @Result(property = "amount", column = "amount"),
            @Result(property = "from", column = "from_acc"),
            @Result(property = "to", column = "to_acc")
    })
    Transfer getTransfer(Long id);
}
