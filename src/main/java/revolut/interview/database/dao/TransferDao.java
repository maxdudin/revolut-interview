package revolut.interview.database.dao;

import revolut.interview.database.entity.Transfer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface TransferDao {
    void doTransfer(BigInteger from, BigInteger to, BigDecimal amount);
    List<Transfer> getTransfers();
    Transfer getTransfer(BigInteger id);
}
