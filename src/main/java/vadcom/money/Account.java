package vadcom.money;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Account {
    private String name;
    private BigDecimal amount;
    private Date creationDate;
    @JsonIgnore
    final private Lock lock = new ReentrantLock();

    public Account(String name, BigDecimal amount) {
        this(name, amount, new Date());
    }

    public Account(String name, BigDecimal amount, Date creationDate) {
        this.name = name;
        this.amount = amount;
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void changeAmount(BigDecimal delta) {
        this.amount = this.amount.add(delta);
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Lock getLock() {
        return lock;
    }
}
