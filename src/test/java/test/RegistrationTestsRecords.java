package test;

import models.records.Registration415ResponseRecordsModel;
import models.records.RegistrationRequestRecordsModel;
import models.records.RegistrationResponseRecordsModel;
import models.records.RegistrationValidationErrorResponseRecordsModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Records tests")
public class RegistrationTestsRecords extends TestBase{
    String username;
    String password;
    String errorMsg;

    @BeforeEach
    public void generateTestData(){
        Faker faker = new Faker();
        username= faker.internet().username();
        password = faker.internet().password();
    }

    @Test
    @DisplayName("Registration 201")
    public  void successfulRegistrationTestRecords(){
        generateTestData();

        RegistrationRequestRecordsModel data = new RegistrationRequestRecordsModel(username,password);


        RegistrationResponseRecordsModel responseRecordsModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .as(RegistrationResponseRecordsModel.class);

        assertEquals(username,responseRecordsModel.username());
    }

    @Test
    @DisplayName("Registration 500")
    public  void RegistrationTestRecords500(){
        generateTestData();

        RegistrationRequestRecordsModel data = new RegistrationRequestRecordsModel(username,password);

        given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register")
                .then()
                .log().all()
                .statusCode(500);


    }

    @Test
    @DisplayName("Registration 415")
    public  void RegistrationTestRecords415(){
        generateTestData();

        RegistrationRequestRecordsModel data = new RegistrationRequestRecordsModel(username,password);

        Registration415ResponseRecordsModel responseRecordsModel = given()
                .log().all()
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(415)
                .extract()
                .as(Registration415ResponseRecordsModel.class);
        String detail = "Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.";
        assertEquals(detail,responseRecordsModel.detail());
    }

    @Test
    @DisplayName("Registration empty password")
    public  void RegistrationTestRecordsEmptyPassword(){
        generateTestData();

        RegistrationRequestRecordsModel data = new RegistrationRequestRecordsModel(username,null);

        RegistrationValidationErrorResponseRecordsModel responseRecordsModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .extract()
                .as(RegistrationValidationErrorResponseRecordsModel.class);
        errorMsg="This field may not be null.";
        assertEquals(errorMsg,responseRecordsModel.password().get(0));
    }

    @Test
    @DisplayName("Registration empty username")
    public  void RegistrationTestRecordsEmptyUsername(){
        generateTestData();

        RegistrationRequestRecordsModel data = new RegistrationRequestRecordsModel(null,password);

        RegistrationValidationErrorResponseRecordsModel responseRecordsModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .extract()
                .as(RegistrationValidationErrorResponseRecordsModel.class);
        errorMsg = "This field may not be null.";
        assertEquals(errorMsg,responseRecordsModel.username().get(0));
    }

    @Test
    @DisplayName("Registration Existing user")
    public  void RegistrationExistingUserTestRecords(){
        generateTestData();

        RegistrationRequestRecordsModel data = new RegistrationRequestRecordsModel(username,password);

        RegistrationResponseRecordsModel responseRecordsModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .as(RegistrationResponseRecordsModel.class);

        assertEquals(username,responseRecordsModel.username());

        RegistrationValidationErrorResponseRecordsModel responseRecordsModel2 = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .extract()
                .as(RegistrationValidationErrorResponseRecordsModel.class);
        errorMsg = "A user with that username already exists.";
        assertEquals(errorMsg,responseRecordsModel2.username().get(0));
    }
}

