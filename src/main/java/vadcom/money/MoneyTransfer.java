package vadcom.money;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

public class MoneyTransfer {
    static final String ROOT_PATH = "/vadcom/MoneyTransfers/1.0.0";
    static final String TRANSACTION_PATH = "/transaction";
    static final String ACCOUNT_PATH = "/account";
    private static final String NAME_PARAM = "name";
    private Store store;

    public MoneyTransfer(Store store) {
        this.store = store;
    }


    private void createAccount(Context context){
        try {
            Account account = context.bodyAsClass(Account.class);
            store.addAccount(account);
            context.status(200);
        } catch (Exception e) {
            context.status(400);
            context.result(e.getMessage());
        }
    }

    private void listAccount(Context context){
        context.json(store.getAccounts());
    }

    private void getAccount(Context context){
        String name=context.pathParam(NAME_PARAM);
        context.json(store.getAccount(name));
    }

    private void deleteAccount(Context context){
        try {
            String name=context.pathParam(NAME_PARAM);
            Lock lock=store.getAccount(name).getLock();
            lock.lock();
            try {
                store.removeAccount(name);
            } finally {
                lock.unlock();
            }
            context.status(200);
        } catch (Exception e) {
            context.status(400);
            context.result(e.getMessage());
        }
    }

    private void listTransaction(Context context){
        context.json(store.getTransactions());
    }

    private void moveMoney(Context context){
        try {
            lockedTransactionOperation(context.bodyAsClass(Transaction.class), transaction-> {
                Account source = store.getAccount(transaction.getSourceAccount());
                Account destination = store.getAccount(transaction.getDestinationAccount());
                if (source.getAmount() >= transaction.getAmount()) {
                    double money = transaction.getAmount();
                    source.changeAmount(-money);
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
            context.status(400);
            context.result(e.getMessage());
        }
    }

    private void lockedTransactionOperation(Transaction transaction, Consumer<Transaction> consumer){
        boolean wait=true;
        while(wait){
            Lock A=store.getAccountLock(transaction.getSourceAccount());
            Lock B=store.getAccountLock(transaction.getDestinationAccount());
            if(A.tryLock()){
                if(B.tryLock())
                {
                    try{
                        consumer.accept(transaction);
                        wait=false;
                    } finally{
                        B.unlock();
                        A.unlock();
                    }
                } else{
                    A.unlock();
                }
            }
        }
    }



    public static void main(String[] args) {
        MoneyTransfer server=new MoneyTransfer(new MemoryStore());
        Javalin app = Javalin.create().start(7000);

        app.get(ROOT_PATH, ctx -> ctx.result("Money transfer service started"));

        app.get(ROOT_PATH+ ACCOUNT_PATH, server::listAccount);
        app.get(ROOT_PATH+ACCOUNT_PATH+"/:"+NAME_PARAM, server::getAccount);
        app.delete(ROOT_PATH+ACCOUNT_PATH+"/:"+NAME_PARAM, server::deleteAccount);
        app.post(ROOT_PATH+ACCOUNT_PATH, server::createAccount);

        app.get(ROOT_PATH+TRANSACTION_PATH, server::listTransaction);
        app.post(ROOT_PATH+TRANSACTION_PATH, server::moveMoney);
    }
}
