package revolut.interview.service;

import revolut.interview.database.dao.AccountDao;
import revolut.interview.database.dao.TransferDao;
import revolut.interview.exception.SameSenderAndReceiverRequestException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class TransferServiceImpl implements TransferService {
    @Inject
    private final TransferDao transferDao;

    @Inject
    private final AccountDao accountDao;

    private Map<BigInteger, Lock> locks = new ConcurrentHashMap<>();

    public TransferServiceImpl(TransferDao transferDao, AccountDao accountDao) {
        this.transferDao = transferDao;
        this.accountDao = accountDao;
    }

    @Override
    public void processTransaction(BigInteger from, BigInteger to, BigDecimal amount) {
        if (from.equals(to)) {
            throw new SameSenderAndReceiverRequestException();
        }

        BigInteger youngerAccount = from.compareTo(to) > 0 ? to : from;
        BigInteger olderAccount = youngerAccount.equals(from) ? to : from;
        Lock youngerLock = getLock(youngerAccount);
        Lock olderLock = getLock(olderAccount);
        try {
            youngerLock.lock();
            try {
                olderLock.lock();
                transferDao.doTransfer(from, to, amount);

            } finally {
                olderLock.unlock();
            }

        } finally {
            youngerLock.unlock();
        }
    }

    @Override
    public void updateBalance(BigInteger accountId, BigDecimal amount) {
        Lock accountLock = locks.get(accountId);
        try {
            accountLock.lock();
            accountDao.updateBalance(accountId, amount);
        } finally {
            accountLock.unlock();
        }
    }

    private Lock getLock(BigInteger accountId) {
        return locks.computeIfAbsent(accountId, key -> new ReentrantLock());
    }
}
