package revolut.interview.database.dao.impl;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import revolut.interview.database.dao.AccountDao;
import revolut.interview.database.entity.Account;
import revolut.interview.database.mybatis.mapper.AccountMapper;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Singleton
public class AccountDaoImpl implements AccountDao {
    private final SqlSessionFactory sqlSessionFactory;

    public AccountDaoImpl(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    private AccountMapper getAccountMapper(SqlSession sqlSession) {
        return sqlSession.getMapper(AccountMapper.class);
    }

    @Override
    public Account getAccount(BigInteger id) {
        return findById(id);
    }

    @Override
    public List<Account> getAllAccounts() {
        return findAll();
    }

    @Override
    public void updateBalance(BigInteger id, BigDecimal balance) {
        try (SqlSession session = sqlSessionFactory.openSession(TransactionIsolationLevel.REPEATABLE_READ)) {
            getAccountMapper(session).setBalance(id, balance);
        }
    }

    @Override
    public void saveAccount(Account account) {
        save(account);
    }

    private Account findById(BigInteger id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return getAccountMapper(session).findById(id);
        }
    }

    private void save(Account account) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            getAccountMapper(session).save(account);
            session.commit();
        }
    }

    private List<Account> findAll() {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return getAccountMapper(session).findAll();
        }
    }
}
