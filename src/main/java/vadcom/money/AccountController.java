package vadcom.money;

import io.javalin.http.Context;

import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;

class AccountController {
    static final String NAME_PARAM = "name";
    private final Store store;

    AccountController(Store store) {
        this.store = store;
    }

    void createAccount(Context context) {
        try {
            Account account = context.bodyAsClass(Account.class);
            store.addAccount(account);
            context.status(200);
        } catch (Exception e) {
            context.status(400);
            context.result(e.getMessage());
        }
    }

    void listAccount(Context context) {
        context.json(store.getAccounts());
    }

    void getAccount(Context context) {
        String name = context.pathParam(NAME_PARAM);
        context.json(store.getAccount(name));
    }

    void editAccount(Context context) {
        final Account newAccount = context.bodyAsClass(Account.class);
        try {
            lockedAccountOperation(context.pathParam(NAME_PARAM), name->{
                Account storedAccount=store.removeAccount(name);
                storedAccount.setName(newAccount.getName());
                storedAccount.setAmount(newAccount.getAmount());
                store.setAccount(storedAccount);
            });
            context.status(200);
        } catch (Exception e) {
            context.status(400);
            context.result(e.getMessage());
        }
    }

    void deleteAccount(Context context) {
        try {
            lockedAccountOperation(context.pathParam(NAME_PARAM), store::removeAccount);
            context.status(200);
        } catch (Exception e) {
            context.status(400);
            context.result(e.getMessage());
        }
    }

    private void lockedAccountOperation(String name, Consumer<String> accountOperation) {
        Lock lock = store.getAccount(name).getLock();
        lock.lock();
        try {
            accountOperation.accept(name);
        } finally {
            lock.unlock();
        }
    }
}