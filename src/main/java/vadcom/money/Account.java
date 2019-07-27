package vadcom.money;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Date;


public class Account {
    private String name;
    private double amount;
    private Date creationDate;

    public Account() {
    }

    public Account(String name, double amount) {
        this(name,amount,new Date());
    }

    public Account(String name, double amount, Date creationDate) {
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

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void changeAmount(double delta) {
        this.amount += delta;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
