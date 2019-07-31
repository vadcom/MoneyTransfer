package vadcom.money;

import io.restassured.RestAssured;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static vadcom.money.MoneyTransfer.*;

public class MoneyTransferTest {
    private final static String ROOT_API_URL = "http://localhost:7000" + ROOT_PATH;
    private static final String VADIM = "Vadim";
    private static final String DMITRY = "Dmitry";
    public static final String BANK = "Bank";

    @Test
    public void accountTest() {
        Account bank = new Account(BANK, 1000);

        RestAssured.given().body(bank)
                .when()
                .post(ROOT_API_URL + ACCOUNT_PATH)
                .then().assertThat().statusCode(200);

        Account readedAccount = RestAssured
                .given()
                .when().get(ROOT_API_URL + ACCOUNT_PATH + "/Bank")
                .then()
                .assertThat().statusCode(200)
                .extract().body().as(Account.class);

        assertEquals(BANK, readedAccount.getName());
        assertEquals(1000.0, readedAccount.getAmount(), 0.1);


        RestAssured.given().body(new Account(BANK, 500))
                .when()
                .put(ROOT_API_URL + ACCOUNT_PATH + "/Bank")
                .then().assertThat().statusCode(200);

        readedAccount = RestAssured
                .given()
                .when().get(ROOT_API_URL + ACCOUNT_PATH + "/Bank")
                .then()
                .assertThat().statusCode(200)
                .extract().body().as(Account.class);

        assertEquals(500.0, readedAccount.getAmount(), 0.1);

        RestAssured.delete(ROOT_API_URL + ACCOUNT_PATH + "/Bank")
                .then().assertThat().statusCode(200);

        Account[] readedAccounts = RestAssured
                .given()
                .when().get(ROOT_API_URL + ACCOUNT_PATH)
                .then()
                .assertThat().statusCode(200)
                .extract().body().as(Account[].class);
        for (Account account : readedAccounts) {
            assertNotEquals(BANK, account.getName());
        }

    }

    @Test
    public void transferMoneyTest() {
        try {
            RestAssured.given().body(new Account(VADIM, 0))
                    .when()
                    .post(ROOT_API_URL + "/account")
                    .then().assertThat().statusCode(200);
            RestAssured.given().body(new Account(DMITRY, 500))
                    .when()
                    .post(ROOT_API_URL + "/account")
                    .then().assertThat().statusCode(200);

            RestAssured.given().body(new Transaction(DMITRY, VADIM, 100))
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

            Transaction[] transaction = RestAssured
                    .given()
                    .when().get(ROOT_API_URL + TRANSACTION_PATH)
                    .then()
                    .extract().body().as(Transaction[].class);
            Transaction lastTransaction = transaction[transaction.length - 1];

            assertEquals(VADIM, lastTransaction.getDestinationAccount());
            assertEquals(DMITRY, lastTransaction.getSourceAccount());
            assertEquals(100, lastTransaction.getAmount(), 0.1);

        } finally {
            RestAssured.delete(ROOT_API_URL + ACCOUNT_PATH + "/Vadim")
                    .then().assertThat().statusCode(200);
            RestAssured.delete(ROOT_API_URL + ACCOUNT_PATH + "/Dmitry")
                    .then().assertThat().statusCode(200);
        }
    }

    @Test
    public void multiThreadTest() throws InterruptedException {
        Executor executor = Executors.newFixedThreadPool(20);
        try {
            final int bankAmount = 1000000;
            final int iterations = 500;
            RestAssured.given().body(new Account(BANK, bankAmount))
                    .when()
                    .post(ROOT_API_URL + "/account")
                    .then().assertThat().statusCode(200);
            RestAssured.given().body(new Account(VADIM, 500))
                    .when()
                    .post(ROOT_API_URL + "/account")
                    .then().assertThat().statusCode(200);
            RestAssured.given().body(new Account(DMITRY, 500))
                    .when()
                    .post(ROOT_API_URL + "/account")
                    .then().assertThat().statusCode(200);
            CountDownLatch countDownLatch = new CountDownLatch(iterations * 3);
            for (int i = 0; i < iterations; i++) {
                executor.execute(() -> {
                    try {
                        RestAssured.given().body(new Transaction(BANK, VADIM, 20))
                                .when()
                                .post(ROOT_API_URL + TRANSACTION_PATH)
                                .then().assertThat().statusCode(200);
//                        System.out.println("Move 20 from Bank to Vadim");
                    } finally {
                        countDownLatch.countDown();
                    }
                });

                executor.execute(() -> {
                    try {
                        RestAssured.given().body(new Transaction(VADIM, DMITRY, 10))
                                .when()
                                .post(ROOT_API_URL + TRANSACTION_PATH)
                                .then().assertThat().statusCode(200);
//                        System.out.println("Move 10 from Vadim to Dmitry");
                    } finally {
                        countDownLatch.countDown();
                    }
                });

                executor.execute(() -> {
                    try {

                        RestAssured.given().body(new Transaction(DMITRY, BANK, 5))
                                .when()
                                .post(ROOT_API_URL + TRANSACTION_PATH)
                                .then().assertThat().statusCode(200);
//                        System.out.println("Move 5 from Dmitry to Bank");
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            countDownLatch.await();

            Account readedAccount = RestAssured
                    .given()
                    .when().get(ROOT_API_URL + ACCOUNT_PATH + "/Bank")
                    .then()
                    .assertThat().statusCode(200)
                    .extract().body().as(Account.class);

            assertEquals(BANK, readedAccount.getName());
            assertEquals(bankAmount - 15 * iterations, readedAccount.getAmount(), 0.1);

        } finally {
            RestAssured.delete(ROOT_API_URL + ACCOUNT_PATH + "/Bank")
                    .then().assertThat().statusCode(200);
            RestAssured.delete(ROOT_API_URL + ACCOUNT_PATH + "/Vadim")
                    .then().assertThat().statusCode(200);
            RestAssured.delete(ROOT_API_URL + ACCOUNT_PATH + "/Dmitry")
                    .then().assertThat().statusCode(200);
        }
    }
}