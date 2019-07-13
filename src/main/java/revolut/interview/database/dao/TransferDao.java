package revolut.interview.database.dao;

import io.micronaut.validation.Validated;
import revolut.interview.database.entity.Transfer;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

@Validated
public interface TransferDao {
    void doTransfer(@NotNull Long from, @NotNull Long to, @PositiveOrZero BigDecimal amount);
    List<Transfer> getTransfers();
    Transfer getTransfer(Long id);
}
