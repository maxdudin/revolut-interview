package revolut.interview.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import revolut.interview.controller.dto.CreateAccountRequest;
import revolut.interview.controller.dto.UpdateBalanceRequest;
import revolut.interview.database.entity.Account;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiTests {
    private static EmbeddedServer embeddedServer;
    private static HttpClient httpClient;
    private static ObjectMapper mapper = new ObjectMapper();


    @BeforeAll
    public static void init() {
        embeddedServer = ApplicationContext.run(EmbeddedServer.class);
        httpClient = embeddedServer.getApplicationContext().createBean(HttpClient.class, embeddedServer.getURL());
    }

    @AfterAll
    public static void cleanup() {
        if (embeddedServer != null) {
            embeddedServer.stop();
        }
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
        List<Account> accountList = mapper.readValue(res, new TypeReference<List<Account>>(){});
        assertTrue(accountList.stream().anyMatch(ac -> balanceToAdd.compareTo(ac.getBalance()) == 0));
    }

    /**
     * Covers GET a list of accounts and a PUT account balance update operation
     */
    @Test
    public void whenHttpClientInvokesAccountsBalanceUpdate_expectItUpdated() throws IOException {
        HttpResponse getAllAccountsRsp = httpClient.toBlocking().exchange(HttpRequest.GET("/accounts"), String.class);
        assertEquals(200, getAllAccountsRsp.getStatus().getCode());
        String getAllAccountsRes = (String) getAllAccountsRsp.getBody(String.class).get();
        List<Account> accountList = mapper.readValue(getAllAccountsRes, new TypeReference<List<Account>>(){});

        Long accountIdToBeInserted = (long) (accountList.size() + 1);
        CreateAccountRequest req = new CreateAccountRequest();
        BigDecimal initialBalance = BigDecimal.valueOf(1111.11);
        req.setInitialBalance(initialBalance);
        HttpResponse createRsp = httpClient.toBlocking().exchange(HttpRequest.POST("/accounts", req));
        assertEquals(201, createRsp.getStatus().getCode());

        HttpResponse afterInsertionAccountsRsp = httpClient.toBlocking().exchange(HttpRequest.GET("/accounts"), String.class);
        assertEquals(200, afterInsertionAccountsRsp.getStatus().getCode());
        String afterInsertionAccountsRes = (String) afterInsertionAccountsRsp.getBody(String.class).get();
        List<Account> accountListAfterInsertion = mapper.readValue(afterInsertionAccountsRes, new TypeReference<List<Account>>(){});
        assertTrue(accountListAfterInsertion.stream()
                .anyMatch(ac -> initialBalance.compareTo(ac.getBalance()) == 0 && ac.getAccountId().equals(accountIdToBeInserted)));

        BigDecimal updatedBalance = BigDecimal.valueOf(11.11);
        UpdateBalanceRequest updateBalanceReq = new UpdateBalanceRequest();
        updateBalanceReq.setAccountId(accountIdToBeInserted);
        updateBalanceReq.setBalance(updatedBalance);
        HttpResponse updateRsp = httpClient.toBlocking().exchange(HttpRequest.PUT("/accounts/" + accountIdToBeInserted, updateBalanceReq));
        assertEquals(200, updateRsp.getStatus().getCode());

        HttpResponse afterUpdateAccountsRsp = httpClient.toBlocking().exchange(HttpRequest.GET("/accounts"), String.class);
        assertEquals(200, afterUpdateAccountsRsp.getStatus().getCode());
        String afterUpdateRes = (String) afterUpdateAccountsRsp.getBody(String.class).get();
        List<Account> accountListAfterUpdate = mapper.readValue(afterUpdateRes, new TypeReference<List<Account>>(){});
        assertTrue(accountListAfterUpdate.stream()
                .anyMatch(ac -> updatedBalance.compareTo(ac.getBalance()) == 0 && ac.getAccountId().equals(accountIdToBeInserted)));
    }
}
