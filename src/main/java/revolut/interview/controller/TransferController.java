package revolut.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import revolut.interview.controller.dto.DoTransferRequest;
import revolut.interview.database.dao.TransferDao;
import revolut.interview.database.entity.Transfer;
import revolut.interview.exception.AccountNotFoundRequestException;
import revolut.interview.exception.NotEnoughMoneyException;
import revolut.interview.exception.SameSenderAndReceiverRequestException;
import revolut.interview.service.TransferService;

import javax.inject.Inject;
import javax.validation.ValidationException;
import java.util.List;

@Controller("${revolut.transfer-url}")
public class TransferController {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferController.class);

    @Inject
    private TransferDao transferDao;

    @Inject
    private TransferService transferService;

    private ObjectMapper mapper = new ObjectMapper();

    @Post
    public HttpResponse<String> doTransfer(@Body DoTransferRequest req) {
        try {
            LOGGER.error("Trying to send amount={} from id={} to id={}", req.getAmount(), req.getFrom(), req.getTo());
            transferService.processTransaction(req.getFrom(), req.getTo(), req.getAmount());
            return HttpResponse.created("Transfer has been done");
        } catch (ValidationException | AccountNotFoundRequestException | NotEnoughMoneyException | SameSenderAndReceiverRequestException e) {
            return HttpResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return HttpResponse.serverError(e.getMessage());
        }
    }

    @Get
    public List<Transfer> getAllTransfer() {
        return transferDao.getTransfers();
    }

    @Get("/{transferId}")
    public HttpResponse<String> getTransfer(@PathVariable("transferId") Long id) {
        try {
            LOGGER.error("Trying to get an account with id={}", id);
            Transfer transfer = transferDao.getTransfer(id);
            return HttpResponse.ok(mapper.writeValueAsString(transfer));
        } catch (ValidationException | AccountNotFoundRequestException e) {
            return HttpResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return HttpResponse.serverError(e.getMessage());
        }
    }
}
