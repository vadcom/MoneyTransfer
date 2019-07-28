package vadcom.money;

import io.restassured.RestAssured;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static vadcom.money.MoneyTransfer.*;

public class MoneyTransferTest {
    private final static String ROOT_API_URL="http://localhost:7000"+ROOT_PATH;
    public static final String BANK = "{" +
            "  \"name\": \"Bank\",\n" +
            "  \"amount\": 1000.0\n" +
            "}";
    public static final String VADIM = "{" +
            "  \"name\": \"Vadim\",\n" +
            "  \"amount\": 0.0\n" +
            "}";

    public static final String DMITRY = "{" +
            "  \"name\": \"Dmitry\",\n" +
            "  \"amount\": 500.0\n" +
            "}";

    public static final String MOVE_MONEY = "{\n" +
            "  \"sourceAccount\": \"Dmitry\",\n" +
            "  \"destinationAccount\": \"Vadim\",\n" +
            "  \"amount\": 100\n" +
            "}";


    @Test
    public void accountTest() {
        RestAssured.given().body(BANK)
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
            RestAssured.given().body(VADIM)
                    .when()
                    .post(ROOT_API_URL + "/account")
                    .then().assertThat().statusCode(200);
            RestAssured.given().body(DMITRY)
                    .when()
                    .post(ROOT_API_URL + "/account")
                    .then().assertThat().statusCode(200);

            RestAssured.given().body(MOVE_MONEY)
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

            assertEquals("Vadim",lastTransaction.getDestinationAccount());
            assertEquals("Dmitry",lastTransaction.getSourceAccount());
            assertEquals(100,lastTransaction.getAmount(),0.1);

        } finally {
            RestAssured.delete(ROOT_API_URL+ACCOUNT_PATH+"/Vadim")
                    .then().assertThat().statusCode(200);
            RestAssured.delete(ROOT_API_URL+ACCOUNT_PATH+"/Dmitry")
                    .then().assertThat().statusCode(200);
        }
    }

}