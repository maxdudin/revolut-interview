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
import revolut.interview.controller.dto.DoTransferRequest;
import revolut.interview.database.entity.Account;
import revolut.interview.database.entity.Transfer;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
public class TransfersApiTests {
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
    public void whenHttpClientInvokesDoTransfer_expectItSuccessfulAndConsistent() throws IOException {
        BigDecimal initialBalance1 = BigDecimal.valueOf(10000.00);
        BigDecimal initialBalance2 = BigDecimal.valueOf(20000.00);
        BigDecimal transferAmount = BigDecimal.valueOf(9999.99);

        Long accountId1 = commonUtils.createNewValidAccount(initialBalance1, httpClient);
        Long accountId2 = commonUtils.createNewValidAccount(initialBalance2, httpClient);

        DoTransferRequest req = new DoTransferRequest();
        req.setFrom(accountId2);
        req.setTo(accountId1);
        req.setAmount(transferAmount);

        HttpResponse doTransferRsp = httpClient.toBlocking().exchange(HttpRequest.POST(transferUrl, req), String.class);
        assertEquals(201, doTransferRsp.getStatus().getCode());
        List<Account> accountList = commonUtils.getCurrentAccountsList(httpClient);

        BigDecimal resultingBalance1 = initialBalance1.add(transferAmount);
        BigDecimal resultingBalance2 = initialBalance2.subtract(transferAmount);
        assertTrue(accountList.stream().anyMatch(ac -> resultingBalance1.compareTo(ac.getBalance()) == 0));
        assertTrue(accountList.stream().anyMatch(ac -> resultingBalance2.compareTo(ac.getBalance()) == 0));
    }

    @Test
    public void whenHttpClientInvokesDoTransfer_expectNewTransferCreated() throws IOException {
        HttpResponse getTransfersRsp =  httpClient.toBlocking().exchange(HttpRequest.GET(transferUrl), String.class);
        assertEquals(200, getTransfersRsp.getStatus().getCode());
        String getAllTransfersRes = (String) getTransfersRsp.getBody(String.class).get();
        List<Transfer> accountList = mapper.readValue(getAllTransfersRes, new TypeReference<List<Transfer>>() {});

        BigDecimal initialBalance1 = BigDecimal.valueOf(100000.00);
        BigDecimal initialBalance2 = BigDecimal.valueOf(200000.00);
        BigDecimal transferAmount = BigDecimal.valueOf(5555.55);

        Long accountId1 = commonUtils.createNewValidAccount(initialBalance1, httpClient);
        Long accountId2 = commonUtils.createNewValidAccount(initialBalance2, httpClient);

        DoTransferRequest req = new DoTransferRequest();
        req.setFrom(accountId2);
        req.setTo(accountId1);
        req.setAmount(transferAmount);

        HttpResponse doTransferRsp = httpClient.toBlocking().exchange(HttpRequest.POST(transferUrl, req), String.class);
        assertEquals(201, doTransferRsp.getStatus().getCode());

        HttpResponse getTransferRsp = httpClient.toBlocking().exchange(HttpRequest.GET(transferUrl + "/" + (accountList.size() + 1)), Transfer.class);
        assertEquals(0, transferAmount.compareTo(((Transfer) getTransferRsp.getBody(Transfer.class).get()).getAmount()));
    }

    @Test
    public void whenHttpClientInvokesDoNegativeTransfer_expectBadRequest() {
        DoTransferRequest req = new DoTransferRequest();
        req.setFrom(1L);
        req.setTo(2L);
        req.setAmount(BigDecimal.valueOf(-11.11));

        try {
            httpClient.toBlocking().exchange(HttpRequest.POST(transferUrl, req), String.class);
        } catch (Exception e) {
            assertTrue(e instanceof HttpClientResponseException);
            assertEquals(400, ((HttpClientResponseException) e).getStatus().getCode());
        }
    }

    @Test
    public void whenHttpClientInvokesDoTransferWithLackOfMoneyOnBalance_expectBadRequest() {
        DoTransferRequest req = new DoTransferRequest();
        req.setFrom(1L);
        req.setTo(2L);
        req.setAmount(BigDecimal.valueOf(Long.MAX_VALUE));

        try {
            httpClient.toBlocking().exchange(HttpRequest.POST(transferUrl, req), String.class);
        } catch (Exception e) {
            assertTrue(e instanceof HttpClientResponseException);
            assertEquals(400, ((HttpClientResponseException) e).getStatus().getCode());
        }
    }

    @Test
    public void whenHttpClientInvokesSendMoneySenderIsEqualToReceiver_expectBadRequest() {
        DoTransferRequest req = new DoTransferRequest();
        req.setFrom(1L);
        req.setTo(1L);
        req.setAmount(BigDecimal.valueOf(1L));

        try {
            httpClient.toBlocking().exchange(HttpRequest.POST(transferUrl, req), String.class);
        } catch (Exception e) {
            assertTrue(e instanceof HttpClientResponseException);
            assertEquals(400, ((HttpClientResponseException) e).getStatus().getCode());
        }
    }
}
