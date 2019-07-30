package vadcom.money;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

public class MemoryStore implements Store {
    private Map<String,Account> accounts=new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Transaction> transactions=new ConcurrentLinkedQueue<>();
    private AtomicInteger transactionId=new AtomicInteger(1);

    @Override
    public Collection<Account> getAccounts(){
        return accounts.values();
    }

    @Override
    public Lock getAccountLock(String name) {
        return getAccount(name).getLock();
    }

    @Override
    public Account getAccount(String name){
        checkAccountPresent(name);
        return accounts.get(name);
    }

    @Override
    public Account removeAccount(String name){
            return accounts.remove(name);
    }

    private void checkAccountPresent(String name) {
        if (!accounts.containsKey(name)) {
            throw new IllegalArgumentException("Account ["+name+"] not found");
        }
    }


    @Override
    public void setAccount(Account account){
        accounts.put(account.getName(),account);
    }


    @Override
    public void addAccount(Account account) {
        checkAccountAbsence(account);
        account.setCreationDate(new Date());
        accounts.put(account.getName(),account);
    }

    private void checkAccountAbsence(Account account) {
        if (accounts.containsKey(account.getName())) {
            throw new IllegalArgumentException("Account ["+account.getName()+"] already present");
        }
    }


    @Override
    public List<Transaction> getTransactions(){
        return Collections.unmodifiableList(new ArrayList<>(transactions));
    }

    @Override
    public void addTransaction(Transaction transaction){
        transaction.setDate(new Date());
        transaction.setId(transactionId.getAndIncrement());
        transactions.add(transaction);
    }

}
