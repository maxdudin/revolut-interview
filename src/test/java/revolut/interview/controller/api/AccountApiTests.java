package revolut.interview.controller.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.annotation.MicronautTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import revolut.interview.controller.common.CommonUtils;
import revolut.interview.controller.dto.CreateAccountRequest;
import revolut.interview.controller.dto.UpdateBalanceRequest;
import revolut.interview.database.entity.Account;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class AccountApiTests {
    @Inject
    private EmbeddedServer embeddedServer;

    @Value("${revolut.account-url}")
    private String accountUrl;

    @Value("${revolut.transfer-url}")
    private String transferUrl;

    private CommonUtils commonUtils;
    private HttpClient httpClient;
    private ObjectMapper mapper = new ObjectMapper();


    @BeforeEach
    public void init() {
        httpClient = embeddedServer.getApplicationContext().createBean(HttpClient.class, embeddedServer.getURL());
        commonUtils = embeddedServer.getApplicationContext().createBean(CommonUtils.class);
    }

    @Test
    public void whenHttpClientInvokesGetExistingAccount_expectItReturned() {
        Account accountPreCreated = new Account();
        accountPreCreated.setAccountId(1L);
        accountPreCreated.setBalance(BigDecimal.valueOf(7000.00));
        HttpResponse rsp = httpClient.toBlocking().exchange(HttpRequest.GET(accountUrl + "/1"), Account.class);
        assertEquals(200, rsp.getStatus().getCode());
        assertEquals(accountPreCreated.getAccountId(), ((Account) rsp.getBody(Account.class).get()).getAccountId());
        assertEquals(0, accountPreCreated.getBalance().compareTo(((Account) rsp.getBody(Account.class).get()).getBalance()));
    }

    @Test
    public void whenHttpClientInvokesAccountCreation_expectItInAccountsList() throws IOException {
        CreateAccountRequest req = new CreateAccountRequest();
        BigDecimal balanceToAdd = BigDecimal.valueOf(123.45);
        req.setInitialBalance(balanceToAdd);
        HttpResponse createRsp = httpClient.toBlocking().exchange(HttpRequest.POST(accountUrl, req));
        assertEquals(201, createRsp.getStatus().getCode());
        HttpResponse getAllAccountsRsp = httpClient.toBlocking().exchange(HttpRequest.GET(accountUrl), String.class);
        String res = (String) getAllAccountsRsp.getBody(String.class).get();
        List<Account> accountList = mapper.readValue(res, new TypeReference<List<Account>>() {
        });
        assertTrue(accountList.stream().anyMatch(ac -> balanceToAdd.compareTo(ac.getBalance()) == 0));
    }

    @Test
    public void whenHttpClientInvokesAccountsBalanceUpdate_expectItUpdated() throws IOException {
        BigDecimal initialBalance = BigDecimal.valueOf(1111.11);
        Long accountIdToBeInserted = commonUtils.createNewValidAccount(initialBalance, httpClient);

        List<Account> accountListAfterInsertion = commonUtils.getCurrentAccountsList(httpClient);
        assertTrue(accountListAfterInsertion.stream()
                .anyMatch(ac -> initialBalance.compareTo(ac.getBalance()) == 0 && ac.getAccountId().equals(accountIdToBeInserted)));

        BigDecimal updatedBalance = BigDecimal.valueOf(11.11);
        UpdateBalanceRequest updateBalanceReq = new UpdateBalanceRequest();
        updateBalanceReq.setAccountId(accountIdToBeInserted);
        updateBalanceReq.setBalance(updatedBalance);
        HttpResponse updateRsp = httpClient.toBlocking().exchange(HttpRequest.PUT(accountUrl + "/" + accountIdToBeInserted, updateBalanceReq));
        assertEquals(200, updateRsp.getStatus().getCode());

        List<Account> accountListAfterUpdate = commonUtils.getCurrentAccountsList(httpClient);
        assertTrue(accountListAfterUpdate.stream()
                .anyMatch(ac -> updatedBalance.compareTo(ac.getBalance()) == 0 && ac.getAccountId().equals(accountIdToBeInserted)));
    }

    @Test
    public void whetHttpClientInvokesSaveAccountWithNegativeBalance_expectBadRequest() {
        BigDecimal initialBalance = BigDecimal.valueOf(-111.11);

        CreateAccountRequest req = new CreateAccountRequest();
        req.setInitialBalance(initialBalance);

        try {
            httpClient.toBlocking().exchange(HttpRequest.POST(accountUrl, req));
        } catch (Exception e) {
            assertTrue(e instanceof HttpClientResponseException);
            assertEquals(400, ((HttpClientResponseException) e).getStatus().getCode());
        }
    }

    @Test
    public void whenHttpClientInvokesUpdateAccountWithNegativeBalance_expectBadRequest() {
        BigDecimal newBalance = BigDecimal.valueOf(-111.11);

        UpdateBalanceRequest req = new UpdateBalanceRequest();
        req.setBalance(newBalance);
        req.setAccountId(1L);

        try {
            httpClient.toBlocking().exchange(HttpRequest.PUT(accountUrl + "/1", req));
        } catch (Exception e) {
            assertTrue(e instanceof HttpClientResponseException);
            assertEquals(400, ((HttpClientResponseException) e).getStatus().getCode());
        }
    }

    @Test
    public void whenHttpClientInvokesGetNonExistingAccount_expectBadRequest() {
        try {
            httpClient.toBlocking().exchange(HttpRequest.GET(accountUrl + "/" + Long.MAX_VALUE), Account.class);
        } catch (Exception e) {
            assertTrue(e instanceof HttpClientResponseException);
            assertEquals(400, ((HttpClientResponseException) e).getStatus().getCode());
        }
    }
}
