package revolut.interview.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import revolut.interview.database.dao.TransferDao;
import revolut.interview.database.entity.Transfer;
import revolut.interview.service.TransferService;

import javax.inject.Inject;

@Controller("/transfers")
public class TransferController {
    @Inject
    private TransferDao transferDao;

    @Inject
    private TransferService transferService;

    @Post
    public HttpResponse doTransfer() {
        return HttpResponse.ok();
    }

//    @Get("/{transferId}")
//    public Transfer getTransfer(Long id) {
//
//    }
}
