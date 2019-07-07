package revolut.interview.service;

import javax.annotation.concurrent.ThreadSafe;
import java.math.BigDecimal;
import java.math.BigInteger;

@ThreadSafe
public interface TransferService {
    void processTransaction(BigInteger from, BigInteger to, BigDecimal amount);
    void updateBalance(BigInteger accountId, BigDecimal amount);
}
