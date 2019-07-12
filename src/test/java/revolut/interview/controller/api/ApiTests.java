package revolut.interview.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import revolut.interview.controller.dto.CreateAccountRequest;
import revolut.interview.controller.dto.DoTransferRequest;
import revolut.interview.controller.dto.UpdateBalanceRequest;
import revolut.interview.database.entity.Account;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
public class ApiTests {
    @Inject
    private EmbeddedServer embeddedServer;

    private HttpClient httpClient;
    private ObjectMapper mapper = new ObjectMapper();

    public ApiTests() {
    }

    @BeforeEach
    public void init() {
        httpClient = embeddedServer.getApplicationContext().createBean(HttpClient.class, embeddedServer.getURL());
    }

    /**
     * Covers GET single account
     */
    @Test
    public void whenHttpClientInvokesGetExistingAccount_expectItReturned() {
        Account accountPreCreated = new Account();
        accountPreCreated.setAccountId(1L);
        accountPreCreated.setBalance(BigDecimal.valueOf(7000.00));
        HttpResponse rsp = httpClient.toBlocking().exchange(HttpRequest.GET("/accounts/1"), Account.class);
        assertEquals(200, rsp.getStatus().getCode());
        assertEquals(accountPreCreated.getAccountId(), ((Account) rsp.getBody(Account.class).get()).getAccountId());
        assertEquals(0, accountPreCreated.getBalance().compareTo(((Account) rsp.getBody(Account.class).get()).getBalance()));
    }

    /**
     * Covers POST new account and GET a list of accounts
     */
    @Test
    public void whenHttpClientInvokesAccountCreation_expectItInAccountsList() throws IOException {
        CreateAccountRequest req = new CreateAccountRequest();
        BigDecimal balanceToAdd = BigDecimal.valueOf(123.45);
        req.setInitialBalance(balanceToAdd);
        HttpResponse createRsp = httpClient.toBlocking().exchange(HttpRequest.POST("/accounts", req));
        assertEquals(201, createRsp.getStatus().getCode());
        HttpResponse getAllAccountsRsp = httpClient.toBlocking().exchange(HttpRequest.GET("/accounts"), String.class);
        String res = (String) getAllAccountsRsp.getBody(String.class).get();
        List<Account> accountList = mapper.readValue(res, new TypeReference<List<Account>>() {
        });
        assertTrue(accountList.stream().anyMatch(ac -> balanceToAdd.compareTo(ac.getBalance()) == 0));
    }

    /**
     * Covers GET a list of accounts and a PUT account balance update operation
     */
    @Test
    public void whenHttpClientInvokesAccountsBalanceUpdate_expectItUpdated() throws IOException {
        BigDecimal initialBalance = BigDecimal.valueOf(1111.11);
        Long accountIdToBeInserted = createNewAccount(initialBalance);

        List<Account> accountListAfterInsertion = getCurrentAccountsList();
        assertTrue(accountListAfterInsertion.stream()
                .anyMatch(ac -> initialBalance.compareTo(ac.getBalance()) == 0 && ac.getAccountId().equals(accountIdToBeInserted)));

        BigDecimal updatedBalance = BigDecimal.valueOf(11.11);
        UpdateBalanceRequest updateBalanceReq = new UpdateBalanceRequest();
        updateBalanceReq.setAccountId(accountIdToBeInserted);
        updateBalanceReq.setBalance(updatedBalance);
        HttpResponse updateRsp = httpClient.toBlocking().exchange(HttpRequest.PUT("/accounts/" + accountIdToBeInserted, updateBalanceReq));
        assertEquals(200, updateRsp.getStatus().getCode());

        List<Account> accountListAfterUpdate = getCurrentAccountsList();
        assertTrue(accountListAfterUpdate.stream()
                .anyMatch(ac -> updatedBalance.compareTo(ac.getBalance()) == 0 && ac.getAccountId().equals(accountIdToBeInserted)));
    }

    @Test
    public void whenHttpClientInvokesDoTransfer_expectItSuccessfulAndConsistent() throws IOException {
        BigDecimal initialBalance1 = BigDecimal.valueOf(10000.00);
        BigDecimal initialBalance2 = BigDecimal.valueOf(20000.00);
        BigDecimal transferAmount = BigDecimal.valueOf(9999.99);

        Long accountId1 = createNewAccount(initialBalance1);
        Long accountId2 = createNewAccount(initialBalance2);

        DoTransferRequest req = new DoTransferRequest();
        req.setFrom(accountId2);
        req.setTo(accountId1);
        req.setAmount(transferAmount);

        HttpResponse doTransferRsp = httpClient.toBlocking().exchange(HttpRequest.POST("/transfers", req), String.class);
        assertEquals(201, doTransferRsp.getStatus().getCode());
        List<Account> accountList = getCurrentAccountsList();

        BigDecimal resultingBalance1 = initialBalance1.add(transferAmount);
        BigDecimal resultingBalance2 = initialBalance2.subtract(transferAmount);
        assertTrue(accountList.stream().anyMatch(ac -> resultingBalance1.compareTo(ac.getBalance()) == 0));
        assertTrue(accountList.stream().anyMatch(ac -> resultingBalance2.compareTo(ac.getBalance()) == 0));
    }

    private List<Account> getCurrentAccountsList() throws IOException {
        HttpResponse getAllAccountsRsp = httpClient.toBlocking().exchange(HttpRequest.GET("/accounts"), String.class);
        assertEquals(200, getAllAccountsRsp.getStatus().getCode());
        String getAllAccountsRes = (String) getAllAccountsRsp.getBody(String.class).get();
        List<Account> accountList = mapper.readValue(getAllAccountsRes, new TypeReference<List<Account>>() {
        });
        return accountList;
    }

    private Long createNewAccount(BigDecimal initialBalance) throws IOException {
        List<Account> accountList = getCurrentAccountsList();

        Long accountIdToBeInserted = (long) (accountList.size() + 1);
        CreateAccountRequest req = new CreateAccountRequest();
        req.setInitialBalance(initialBalance);
        HttpResponse createRsp = httpClient.toBlocking().exchange(HttpRequest.POST("/accounts", req));
        assertEquals(201, createRsp.getStatus().getCode());
        return accountIdToBeInserted;
    }
}
