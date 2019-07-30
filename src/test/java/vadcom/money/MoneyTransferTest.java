package vadcom.money;

import io.restassured.RestAssured;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static vadcom.money.MoneyTransfer.*;

public class MoneyTransferTest {
    private final static String ROOT_API_URL="http://localhost:7000"+ROOT_PATH;
    private static final String VADIM = "Vadim";
    private static final String DMITRY = "Dmitry";

    @Test
    public void accountTest() {
        Account bank=new Account("Bank",1000);

        RestAssured.given().body(bank)
                .when()
                .post(ROOT_API_URL+ACCOUNT_PATH)
                .then().assertThat().statusCode(200);

        Account readedAccount=RestAssured
                .given()
                .when().get(ROOT_API_URL+ACCOUNT_PATH+"/Bank")
                .then()
                .assertThat().statusCode(200)
                .extract().body().as(Account.class);

        assertEquals("Bank", readedAccount.getName());
        assertEquals(1000.0, readedAccount.getAmount(),0.1);


        RestAssured.given().body(new Account("Bank",500))
                .when()
                .put(ROOT_API_URL+ACCOUNT_PATH+"/Bank")
                .then().assertThat().statusCode(200);

        readedAccount=RestAssured
                .given()
                .when().get(ROOT_API_URL+ACCOUNT_PATH+"/Bank")
                .then()
                .assertThat().statusCode(200)
                .extract().body().as(Account.class);

        assertEquals(500.0, readedAccount.getAmount(),0.1);

        RestAssured.delete(ROOT_API_URL+ACCOUNT_PATH+"/Bank")
                .then().assertThat().statusCode(200);

        Account[] readedAccounts=RestAssured
                .given()
                .when().get(ROOT_API_URL+ACCOUNT_PATH)
                .then()
                .assertThat().statusCode(200)
                .extract().body().as(Account[].class);
        for (Account account : readedAccounts) {
            assertNotEquals("Bank",account.getName());
        }

    }

    @Test
    public void transferMoneyTest() {
        try {
            RestAssured.given().body(new Account(VADIM,0))
                    .when()
                    .post(ROOT_API_URL + "/account")
                    .then().assertThat().statusCode(200);
            RestAssured.given().body(new Account(DMITRY,500))
                    .when()
                    .post(ROOT_API_URL + "/account")
                    .then().assertThat().statusCode(200);

            RestAssured.given().body(new Transaction(DMITRY, VADIM,100))
                    .when()
                    .post(ROOT_API_URL + TRANSACTION_PATH)
                    .then().assertThat().statusCode(200);


            Account vadimAccount = RestAssured
                    .given()
                    .when().get(ROOT_API_URL + ACCOUNT_PATH + "/Vadim")
                    .then()
                    .extract().body().as(Account.class);

            assertEquals(100, vadimAccount.getAmount(), 0.1);

            Account dmitryAccount = RestAssured
                    .given()
                    .when().get(ROOT_API_URL + ACCOUNT_PATH + "/Dmitry")
                    .then()
                    .extract().body().as(Account.class);

            assertEquals(400, dmitryAccount.getAmount(), 0.1);

            Transaction[] transaction=RestAssured
                    .given()
                    .when().get(ROOT_API_URL + TRANSACTION_PATH)
                    .then()
                    .extract().body().as(Transaction[].class);
            Transaction lastTransaction=transaction[transaction.length-1];

            assertEquals(VADIM,lastTransaction.getDestinationAccount());
            assertEquals(DMITRY,lastTransaction.getSourceAccount());
            assertEquals(100,lastTransaction.getAmount(),0.1);

        } finally {
            RestAssured.delete(ROOT_API_URL+ACCOUNT_PATH+"/Vadim")
                    .then().assertThat().statusCode(200);
            RestAssured.delete(ROOT_API_URL+ACCOUNT_PATH+"/Dmitry")
                    .then().assertThat().statusCode(200);
        }
    }

}