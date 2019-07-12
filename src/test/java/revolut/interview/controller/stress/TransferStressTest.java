package revolut.interview.controller.stress;

import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import revolut.interview.database.dao.AccountDao;
import revolut.interview.database.dao.TransferDao;
import revolut.interview.database.entity.Account;
import revolut.interview.service.TransferService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
public class TransferStressTest {
    @Inject
    private TransferService transferService;

    @Inject
    private AccountDao accountDao;

    @Inject
    private TransferDao transferDao;


    @Test
    public void doTransferStressTest() throws InterruptedException {
        List<Account> existingAccounts = accountDao.getAllAccounts();
        BigDecimal initialBalance = BigDecimal.valueOf(1_000_000);
        Long accountAId = (long) (existingAccounts.size() + 1);
        Long accountBId = (long) (existingAccounts.size() + 2);

        Account accountA = new Account();
        accountA.setBalance(initialBalance);
        accountDao.saveAccount(accountA);

        Account accountB = new Account();
        accountB.setBalance(initialBalance);
        accountDao.saveAccount(accountB);

        ExecutorService executorService = Executors.newCachedThreadPool();
        BigDecimal transferAmount = BigDecimal.valueOf(1);

        Callable<Void> transferFromAToB = () -> {
            transferService.processTransaction(accountAId, accountBId, transferAmount);
            return null;
        };

        Callable<Void> transferFromBToA = () -> {
            transferService.processTransaction(accountBId, accountAId, transferAmount);
            return null;
        };

        var taskList = IntStream.concat(IntStream.generate(() -> 0).limit(100), IntStream.generate(() -> 1).limit(100))
                .mapToObj(i -> i > 0 ? transferFromAToB : transferFromBToA)
                .collect(Collectors.toList());

        Collections.shuffle(taskList);
        List<Future<Void>> futures = executorService.invokeAll(taskList);

        Account accountAUpdated = accountDao.getAccount(accountAId);
        Account accountBUpdated = accountDao.getAccount(accountBId);

        assertEquals(0, initialBalance.compareTo(accountAUpdated.getBalance()));
        assertEquals(0, initialBalance.compareTo(accountBUpdated.getBalance()));
    }
}
