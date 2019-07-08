package revolut.interview.database.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.StringJoiner;

public class Transfer {
    private Long transferId;

    private Long from;

    private Long to;

    private BigDecimal amount;

    private Timestamp timestamp;

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transfer transfer = (Transfer) o;

        if (!Objects.equals(transferId, transfer.transferId)) return false;
        if (getFrom() != null ? !getFrom().equals(transfer.getFrom()) : transfer.getFrom() != null) return false;
        if (getTo() != null ? !getTo().equals(transfer.getTo()) : transfer.getTo() != null) return false;
        if (getAmount() != null ? !getAmount().equals(transfer.getAmount()) : transfer.getAmount() != null)
            return false;
        return getTimestamp() != null ? getTimestamp().equals(transfer.getTimestamp()) : transfer.getTimestamp() == null;

    }

    @Override
    public int hashCode() {
        int result = transferId != null ? transferId.hashCode() : 0;
        result = 31 * result + (getFrom() != null ? getFrom().hashCode() : 0);
        result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
        result = 31 * result + (getAmount() != null ? getAmount().hashCode() : 0);
        result = 31 * result + (getTimestamp() != null ? getTimestamp().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Transfer.class.getSimpleName() + "[", "]")
                .add("transferId=" + transferId)
                .add("from=" + from)
                .add("to=" + to)
                .add("amount=" + amount)
                .add("timestamp=" + timestamp)
                .toString();
    }
}
