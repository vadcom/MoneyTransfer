package vadcom.money;

import io.javalin.http.Context;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

public class TransactionController {
    private final Store store;

    public TransactionController(Store store) {
        this.store = store;
    }

    void listTransaction(Context context) {
        context.status(200);
        context.json(store.getTransactions());
    }

    void moveMoney(Context context) {
        try {
            lockedTransactionOperation(context.bodyAsClass(Transaction.class), transaction -> {
                Account source = store.getAccount(transaction.getSourceAccount());
                Account destination = store.getAccount(transaction.getDestinationAccount());
                if (source.getAmount().compareTo(transaction.getAmount()) != -1) {
                    BigDecimal money = transaction.getAmount();
                    source.changeAmount(money.negate());
                    destination.changeAmount(money);
                    store.setAccount(source);
                    store.setAccount(destination);
                    store.addTransaction(transaction);
                    context.status(200);
                } else {
                    throw new IllegalStateException("insufficient amount on source account");
                }
            });
        } catch (Exception e) {
            System.err.println(e.getMessage());
            context.status(400);
            context.result(e.getMessage());
        }
    }

    private void lockedTransactionOperation(Transaction transaction, Consumer<Transaction> consumer) {
        boolean wait = true;
        while (wait) {
            Lock sourceAccountLock = store.getAccountLock(transaction.getSourceAccount());
            Lock destinationAccountLock = store.getAccountLock(transaction.getDestinationAccount());
            if (sourceAccountLock.tryLock()) {
                if (destinationAccountLock.tryLock()) {
                    try {
                        consumer.accept(transaction);
                        wait = false;
                    } finally {
                        destinationAccountLock.unlock();
                        sourceAccountLock.unlock();
                    }
                } else {
                    sourceAccountLock.unlock();
                }
            }
        }
    }
}