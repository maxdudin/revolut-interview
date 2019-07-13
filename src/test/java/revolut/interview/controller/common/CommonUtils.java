package revolut.interview.controller.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import revolut.interview.controller.dto.CreateAccountRequest;
import revolut.interview.database.entity.Account;

import javax.inject.Singleton;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Singleton
public class CommonUtils {
    private ObjectMapper mapper = new ObjectMapper();

    @Value("${revolut.account-url}")
    private String accountUrl;

    @Value("${revolut.transfer-url}")
    private String transferUrl;

    public List<Account> getCurrentAccountsList(HttpClient httpClient) throws IOException {
        HttpResponse getAllAccountsRsp = httpClient.toBlocking().exchange(HttpRequest.GET(accountUrl), String.class);
        assertEquals(200, getAllAccountsRsp.getStatus().getCode());
        String getAllAccountsRes = (String) getAllAccountsRsp.getBody(String.class).get();
        List<Account> accountList = mapper.readValue(getAllAccountsRes, new TypeReference<List<Account>>() {});
        return accountList;
    }

    public Long createNewValidAccount(BigDecimal initialBalance, HttpClient httpClient) throws IOException {
        List<Account> accountList = getCurrentAccountsList(httpClient);

        Long accountIdToBeInserted = (long) (accountList.size() + 1);
        CreateAccountRequest req = new CreateAccountRequest();
        req.setInitialBalance(initialBalance);
        HttpResponse createRsp = httpClient.toBlocking().exchange(HttpRequest.POST(accountUrl, req));
        assertEquals(201, createRsp.getStatus().getCode());
        return accountIdToBeInserted;
    }
}
