package vadcom.money;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.plugin.openapi.annotations.ContentType;
import org.junit.Test;
import io.restassured.RestAssured;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static vadcom.money.MoneyTransfer.ROOT_PATH;

public class MoneyTransferTest {
    private final static String ROOT_API_URL="http://localhost:7000"+ROOT_PATH;

    @Test
    public void addAccountTest() {
        RestAssured.given().body("{" +
                "  \"name\": \"Bank\",\n" +
                "  \"amount\": 1000.0\n" +
                "}")
                .when()
                .post(ROOT_API_URL+"/account")
                .then().assertThat().statusCode(200);

        Account readedAccount=RestAssured
                .given()
                .contentType(ContentType.JSON).accept(ContentType.JSON)
                .when().get(ROOT_API_URL+"/account/Bank")
                .then()
        .extract().body().as(Account.class);

        assertEquals("Bank", readedAccount.getName());
        assertEquals(1000.0, readedAccount.getAmount(),0.1);
    }

/*    @Test
    public void name() throws IOException {
        String json = "{" +
                "  \"name\": \"Bank\",\n" +
                "  \"amount\": 1000.0\n" +
                "}";
        ObjectMapper objectMapper=new ObjectMapper();
        Account car = objectMapper.readValue(json, Account.class);
    }*/
}