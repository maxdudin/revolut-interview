package revolut.interview.controller.unit;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import revolut.interview.database.dao.AccountDao;
import revolut.interview.database.dao.TransferDao;
import revolut.interview.database.entity.Account;
import revolut.interview.service.TransferService;
import revolut.interview.service.TransferServiceImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class TransferServiceUnitTest {
    @Test
    public void whenTransferServiceDoesTransferWithMocks_expectTransferSuccessful() throws InterruptedException {
        BigDecimal transferAmount = BigDecimal.valueOf(1.11);

        Account accountA = new Account();
        BigDecimal accountAInitialBalance = BigDecimal.valueOf(1_000_000);
        accountA.setAccountId(1L);
        accountA.setBalance(accountAInitialBalance);

        Account accountB = new Account();
        BigDecimal accountBInitialBalance = BigDecimal.valueOf(2_000_000);
        accountB.setAccountId(2L);
        accountB.setBalance(accountBInitialBalance);

        TransferDao transferDao = mock(TransferDao.class);
        AccountDao accountDao = mock(AccountDao.class);

        doAnswer((Answer<Void>) invocationOnMock -> {
            accountA.setBalance(accountA.getBalance().subtract(transferAmount));
            accountB.setBalance(accountB.getBalance().add(transferAmount));
            return null;
        }).when(transferDao).doTransfer(1L, 2L, transferAmount);

        doAnswer((Answer<Void>) invocationOnMock -> {
            accountB.setBalance(accountB.getBalance().subtract(transferAmount));
            accountA.setBalance(accountA.getBalance().add(transferAmount));
            return null;
        }).when(transferDao).doTransfer(2L, 1L, transferAmount);

        TransferService transferService = new TransferServiceImpl(transferDao, accountDao);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Callable<Void> transferFromAToB = () -> {
            transferService.processTransaction(1L, 2L, transferAmount);
            return null;
        };

        Callable<Void> transferFromBToA = () -> {
            transferService.processTransaction(2L, 1L, transferAmount);
            return null;
        };

        var taskList = IntStream.concat(IntStream.generate(() -> 0).limit(100_000), IntStream.generate(() -> 1).limit(100_000))
                .mapToObj(i -> i > 0 ? transferFromAToB : transferFromBToA)
                .collect(Collectors.toList());

        Collections.shuffle(taskList);
        List<Future<Void>> futures = executorService.invokeAll(taskList);

        assertEquals(0, accountAInitialBalance.compareTo(accountA.getBalance()));
        assertEquals(0, accountBInitialBalance.compareTo(accountB.getBalance()));
    }

}
