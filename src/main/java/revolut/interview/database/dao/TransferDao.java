package revolut.interview.database.dao;

import revolut.interview.database.entity.Transfer;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {
    void doTransfer(Long from, Long to, BigDecimal amount);
    List<Transfer> getTransfers();
    Transfer getTransfer(Long id);
}
