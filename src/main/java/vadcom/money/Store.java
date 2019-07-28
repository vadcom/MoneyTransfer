package vadcom.money;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

public interface Store {
    Collection<Account> getAccounts();

    Lock getAccountLock(String name);

    Account getAccount(String name);

    Account removeAccount(String name);

    void setAccount(Account account);

    void addAccount(Account account);

    List<Transaction> getTransactions();

    void addTransaction(Transaction transaction);
}
