package revolut.interview.controller.dto;

import java.math.BigDecimal;
import java.math.BigInteger;

public class DoTransferRequest {
    private BigInteger from;
    private BigInteger to;
    private BigDecimal amount;

    public BigInteger getFrom() {
        return from;
    }

    public void setFrom(BigInteger from) {
        this.from = from;
    }

    public BigInteger getTo() {
        return to;
    }

    public void setTo(BigInteger to) {
        this.to = to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
