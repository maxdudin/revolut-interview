package revolut.interview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
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
    @Inject
    private TransferDao transferDao;

    @Inject
    private TransferService transferService;

    private ObjectMapper mapper = new ObjectMapper();

    @Post
    public HttpResponse<String> doTransfer(@Body DoTransferRequest req) {
        try {
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
            Transfer transfer = transferDao.getTransfer(id);
            return HttpResponse.ok(mapper.writeValueAsString(transfer));
        } catch (ValidationException | AccountNotFoundRequestException e) {
            return HttpResponse.badRequest(e.getMessage());
        } catch (Exception e) {
            return HttpResponse.serverError(e.getMessage());
        }
    }
}
