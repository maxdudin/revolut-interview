package revolut.interview.database.dao.impl;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import revolut.interview.database.dao.TransferDao;
import revolut.interview.database.entity.Account;
import revolut.interview.database.entity.Transfer;
import revolut.interview.database.mybatis.mapper.AccountMapper;
import revolut.interview.database.mybatis.mapper.TransferMapper;
import revolut.interview.exception.AccountNotFoundRequestException;
import revolut.interview.exception.NotEnoughMoneyException;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.List;

@Singleton
public class TransferDaoImpl implements TransferDao {
    private final SqlSessionFactory sqlSessionFactory;

    public TransferDaoImpl(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    private TransferMapper getTransferMapper(SqlSession sqlSession) {
        return sqlSession.getMapper(TransferMapper.class);
    }

    private AccountMapper getAccountMapper(SqlSession sqlSession) {
        return sqlSession.getMapper(AccountMapper.class);
    }

    @Override
    public void doTransfer(Long from, Long to, BigDecimal amount) {
        try (SqlSession session = sqlSessionFactory.openSession(TransactionIsolationLevel.REPEATABLE_READ)) {
            Account fromAccount = getAccountMapper(session).findById(from);

            if (fromAccount == null) {
                throw new AccountNotFoundRequestException(from);
            }

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new NotEnoughMoneyException(from, amount);
            }

            getAccountMapper(session).setBalance(from, fromAccount.getBalance().subtract(amount));
            Account toAccount = getAccountMapper(session).findById(to);

            if (toAccount == null) {
                throw new AccountNotFoundRequestException(to);
            }

            getAccountMapper(session).setBalance(to, toAccount.getBalance().add(amount));
            Transfer transfer = new Transfer();
            transfer.setAmount(amount);
            transfer.setFrom(from);
            transfer.setTo(to);
            getTransferMapper(session).save(transfer);
            session.commit();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<Transfer> getTransfers() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return getTransferMapper(session).findAll();
        }
    }

    @Override
    public Transfer getTransfer(Long id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            Transfer transfer = getTransferMapper(session).getTransfer(id);
            if (transfer == null) {
                throw new AccountNotFoundRequestException(id);
            }

            return transfer;
        }
    }
}
