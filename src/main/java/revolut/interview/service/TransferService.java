package revolut.interview.service;

import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;

@ThreadSafe
public interface TransferService {
    void processTransaction(Long from, Long to, BigDecimal amount);
    void updateBalance(Long accountId, BigDecimal amount);
}
