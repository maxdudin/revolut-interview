package revolut.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.runtime.server.EmbeddedServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import revolut.interview.database.entity.Account;

import java.math.BigDecimal;

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

    @Test
    public void whenHttpClientInvokesGetExistingAccount_expectItSuccessful() {
        Account accountPreCreated = new Account();
        accountPreCreated.setAccountId(1L);
        accountPreCreated.setBalance(BigDecimal.valueOf(7000.00));
        HttpResponse rsp = httpClient.toBlocking().exchange(HttpRequest.GET("/accounts/1"), Account.class);
        assertEquals(200, rsp.getStatus().getCode());
        assertEquals(accountPreCreated.getAccountId(), ((Account) rsp.getBody(Account.class).get()).getAccountId());
        assertTrue(accountPreCreated.getBalance().compareTo( ((Account) rsp.getBody(Account.class).get()).getBalance()) == 0);
    }
}
