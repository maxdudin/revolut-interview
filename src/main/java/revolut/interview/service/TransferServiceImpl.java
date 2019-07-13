package revolut.interview.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import revolut.interview.database.dao.AccountDao;
import revolut.interview.database.dao.TransferDao;
import revolut.interview.exception.SameSenderAndReceiverRequestException;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
public class TransferServiceImpl implements TransferService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferServiceImpl.class);

    @Inject
    private final TransferDao transferDao;

    @Inject
    private final AccountDao accountDao;

    private Map<Long, Lock> locks = new ConcurrentHashMap<>();

    public TransferServiceImpl(TransferDao transferDao, AccountDao accountDao) {
        this.transferDao = transferDao;
        this.accountDao = accountDao;
    }

    @Override
    public void processTransaction(Long from, Long to, BigDecimal amount) {
        LOGGER.debug("Processing transaction: from={}, to={}, amount={}", from, to, amount);

        if (from.equals(to)) {
            throw new SameSenderAndReceiverRequestException();
        }

        Long youngerAccount = from.compareTo(to) > 0 ? to : from;
        Long olderAccount = youngerAccount.equals(from) ? to : from;
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
    public void updateBalance(Long accountId, BigDecimal amount) {
        LOGGER.debug("Processing balance update: accountId={}, amount={}", accountId, amount);

        Lock accountLock = getLock(accountId);
        try {
            accountLock.lock();
            accountDao.updateBalance(accountId, amount);
        } finally {
            accountLock.unlock();
        }
    }

    private Lock getLock(Long accountId) {
        return locks.computeIfAbsent(accountId, key -> new ReentrantLock());
    }
}
