package vadcom.money;

import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.Date;

public class MoneyTransfer {
    public static final String NAME_PARAM = "name";
    public static final String ROOT_PATH = "/vadcom/MoneyTransfers/1.0.0";
    Store store=new Store();

    void createAccount(Context context){
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
            store.removeAccount(name);
            context.json(store.getAccount(name));
        } catch (Exception e) {
            context.status(400);
            context.result(e.getMessage());
        }
    }

    private void listTransaction(Context context){
        context.json(store.getTransactions());
    }

    void moveMoney(Context context){
        try {
            Transaction transaction = context.bodyAsClass(Transaction.class);
            Account source=store.getAccount(transaction.getSourceAccount());
            Account destination=store.getAccount(transaction.getDestinationAccount());
            if (source.getAmount()>=transaction.getAmount()) {
                double money=transaction.getAmount();
                source.changeAmount(-money);
                destination.changeAmount(money);
                store.setAccount(source);
                store.setAccount(destination);
                store.addTransaction(transaction);
                context.status(200);
            } else {
                throw new IllegalStateException("insufficient amount on source account");
            }
        } catch (Exception e) {
            context.status(400);
            context.result(e.getMessage());
        }
    }


    public static void main(String[] args) {
        MoneyTransfer server=new MoneyTransfer();
        Javalin app = Javalin.create().start(7000);
        app.get(ROOT_PATH, ctx -> ctx.result("Money transfer service started"));
        app.get(ROOT_PATH+"/account", server::listAccount);
        app.get(ROOT_PATH+"/account/:"+NAME_PARAM, server::getAccount);
        app.delete(ROOT_PATH+"/account/:"+NAME_PARAM, server::deleteAccount);
        app.post(ROOT_PATH+"/account", server::createAccount);

        app.get(ROOT_PATH+"/transaction", server::listTransaction);
        app.post(ROOT_PATH+"/transaction", server::moveMoney);

    }
}
