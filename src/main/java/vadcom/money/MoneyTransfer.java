package vadcom.money;

import io.javalin.Javalin;

import static vadcom.money.AccountController.NAME_PARAM;

public class MoneyTransfer {
    static final String ROOT_PATH = "/vadcom/MoneyTransfers/1.0.0";
    static final String TRANSACTION_PATH = "/transaction";
    static final String ACCOUNT_PATH = "/account";

    private final AccountController accountController;
    private final TransactionController transactionController;


    private MoneyTransfer(Store store) {
        this.accountController = new AccountController(store);
        this.transactionController = new TransactionController(store);
    }


    private void start() {
        try (Javalin javalin = Javalin.create()) {
            Javalin app = javalin.start(7000);
            app.get(ROOT_PATH + ACCOUNT_PATH, accountController::listAccount);
            app.post(ROOT_PATH + ACCOUNT_PATH, accountController::createAccount);
            app.put(ROOT_PATH + ACCOUNT_PATH + "/{" + NAME_PARAM + "}", accountController::editAccount);
            app.get(ROOT_PATH + ACCOUNT_PATH + "/{" + NAME_PARAM + "}", accountController::getAccount);
            app.delete(ROOT_PATH + ACCOUNT_PATH + "/{" + NAME_PARAM + "}", accountController::deleteAccount);
            app.get(ROOT_PATH + TRANSACTION_PATH, transactionController::listTransaction);
            app.post(ROOT_PATH + TRANSACTION_PATH, transactionController::moveMoney);
        }
    }

    public static void main(String[] args) {
        new MoneyTransfer(new MemoryStore()).start();
    }
}
