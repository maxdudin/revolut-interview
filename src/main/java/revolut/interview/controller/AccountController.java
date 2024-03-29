package revolut.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import revolut.interview.controller.dto.CreateAccountRequest;
import revolut.interview.controller.dto.UpdateBalanceRequest;
import revolut.interview.database.dao.AccountDao;
import revolut.interview.database.entity.Account;
import revolut.interview.exception.AccountNotFoundRequestException;
import revolut.interview.service.TransferService;

import javax.inject.Inject;
import javax.validation.ValidationException;
import java.util.List;

@Controller("${revolut.account-url}")
@Produces(MediaType.APPLICATION_JSON)
public class AccountController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountController.class);

    @Inject
    private AccountDao accountDao;

    @Inject
    private TransferService transferService;

    private ObjectMapper mapper = new ObjectMapper();

    @Get
    public List<Account> getAccounts() {
        return accountDao.getAllAccounts();
    }

    @Get("/{accountId}")
    public HttpResponse<String> getAccount(@PathVariable("accountId") Long accountId) {
        try {
            LOGGER.debug("Trying to get an account with id={}", accountId);
            Account account = accountDao.getAccount(accountId);
            if (account == null) {
                throw new AccountNotFoundRequestException(accountId);
            }

            return HttpResponse.ok(mapper.writeValueAsString(account));
        } catch (ValidationException | AccountNotFoundRequestException e) {
            return HttpResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return HttpResponse.serverError("Server error has occurred, please try again later");
        }
    }

    @Put("/{accountId}")
    public HttpResponse<String> updateBalance(@PathVariable("accountId") Long accountId, @Body UpdateBalanceRequest req) {
        try {
            LOGGER.debug("Trying to update an account id={} with balance={}", accountId, req.getBalance());
            transferService.updateBalance(accountId, req.getBalance());
            return HttpResponse.ok("Balance has been updated!");
        } catch (ValidationException | AccountNotFoundRequestException e) {
            return HttpResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return HttpResponse.serverError("Server error has occurred, please try again later");
        }
    }

    @Post
    public HttpResponse<String> saveNewAccount(@Body CreateAccountRequest req) {
        try {
            LOGGER.debug("Trying to save an account with balance={}", req.getInitialBalance());
            Account account = new Account();
            account.setBalance(req.getInitialBalance());
            accountDao.saveAccount(account);
            return HttpResponse.created("Account has been created");
        } catch (ValidationException e) {
            return HttpResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return HttpResponse.serverError("Server error has occurred, please try again later");
        }
    }
}
