package test;

import models.lombok.Registration415ResponseLombokModel;
import models.lombok.RegistrationValidationErrorResponseLombokModel;
import models.lombok.RegistrationRequestLombokModel;
import models.lombok.RegistrationResponseLombokModel;

import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Lombok tests")
public class RegistrationTestsLombok extends TestBase{
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
    public  void successfulRegistrationTestLombok(){
        generateTestData();

        RegistrationRequestLombokModel data = new RegistrationRequestLombokModel();
        data.setUsername(username);
        data.setPassword(password);

        RegistrationResponseLombokModel responseLombokModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .as(RegistrationResponseLombokModel.class);

        assertEquals(username,responseLombokModel.getUsername());
    }

    @Test
    @DisplayName("Registration 500")
    public  void RegistrationTestLombok500(){
        generateTestData();

        RegistrationRequestLombokModel data = new RegistrationRequestLombokModel();
        data.setUsername(username);
        data.setPassword(password);

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
    public  void RegistrationTestLombok415(){
        generateTestData();

        RegistrationRequestLombokModel data = new RegistrationRequestLombokModel();
        data.setUsername(username);
        data.setPassword(password);

        Registration415ResponseLombokModel responseLombokModel = given()
                .log().all()
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(415)
                .extract()
                .as(Registration415ResponseLombokModel.class);
        String detail = "Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.";
        assertEquals(detail,responseLombokModel.getDetail());
    }

    @Test
    @DisplayName("Registration empty password")
    public  void RegistrationTestLombokEmptyPassword(){
        generateTestData();

        RegistrationRequestLombokModel data = new RegistrationRequestLombokModel();
        data.setUsername(username);

        RegistrationValidationErrorResponseLombokModel responseLombokModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .extract()
                .as(RegistrationValidationErrorResponseLombokModel.class);
        errorMsg="This field may not be null.";
        assertEquals(errorMsg,responseLombokModel.getPassword().get(0));
    }

    @Test
    @DisplayName("Registration empty username")
    public  void RegistrationTestLombokEmptyUsername(){
        generateTestData();

        RegistrationRequestLombokModel data = new RegistrationRequestLombokModel();
        data.setPassword(password);

        RegistrationValidationErrorResponseLombokModel responseLombokModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .extract()
                .as(RegistrationValidationErrorResponseLombokModel.class);
        errorMsg = "This field may not be null.";
        assertEquals(errorMsg,responseLombokModel.getUsername().get(0));
    }

    @Test
    @DisplayName("Registration Existing user")
    public  void RegistrationExistingUserTestLombok(){
        generateTestData();

        RegistrationRequestLombokModel data = new RegistrationRequestLombokModel();
        data.setUsername(username);
        data.setPassword(password);

        RegistrationResponseLombokModel responseLombokModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .as(RegistrationResponseLombokModel.class);

        assertEquals(username,responseLombokModel.getUsername());

        RegistrationValidationErrorResponseLombokModel responseLombokModel2 = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .extract()
                .as(RegistrationValidationErrorResponseLombokModel.class);
        errorMsg = "A user with that username already exists.";
        assertEquals(errorMsg,responseLombokModel2.getUsername().get(0));
    }
}

