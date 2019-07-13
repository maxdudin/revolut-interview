package revolut.interview.database.dao.impl;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import revolut.interview.database.dao.AccountDao;
import revolut.interview.database.entity.Account;
import revolut.interview.database.mybatis.mapper.AccountMapper;

import javax.inject.Singleton;
import java.math.BigDecimal;
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
    public Account getAccount(Long id) {
        return findById(id);
    }

    @Override
    public List<Account> getAllAccounts() {
        return findAll();
    }

    @Override
    public void updateBalance(Long id, BigDecimal balance) {
        try (SqlSession session = sqlSessionFactory.openSession(TransactionIsolationLevel.REPEATABLE_READ)) {
            getAccountMapper(session).setBalance(id, balance);
            session.commit();
        }
    }

    @Override
    public void saveAccount(Account account) {
        save(account);
    }

    private Account findById(Long id) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            return getAccountMapper(session).findById(id);
        } catch (Exception e) {
            System.out.println(e);
            throw e;
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
