package test.examples;

import models.otherModels.pojo.Registration415ResponsePojoModel;
import models.otherModels.pojo.RegistrationValidationErrorResponsePojoModel;
import models.otherModels.pojo.RegistrationRequestPojoModel;
import models.otherModels.pojo.RegistrationResponsePojoModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test.TestBase;

import static io.restassured.RestAssured.*;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@DisplayName("Pojo tests")
public class RegistrationTestsPojo extends TestBase {
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
    public  void successfulRegistrationTestPojo(){
        generateTestData();

        RegistrationRequestPojoModel data = new RegistrationRequestPojoModel();
        data.setUsername(username);
        data.setPassword(password);

        RegistrationResponsePojoModel responsePojoModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .as(RegistrationResponsePojoModel.class);

        assertEquals(username,responsePojoModel.getUsername());
    }

    @Test
    @DisplayName("Registration 500")
    public  void RegistrationTestPojo500(){
        generateTestData();

        RegistrationRequestPojoModel data = new RegistrationRequestPojoModel();
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
    public  void RegistrationTestPojo415(){
        generateTestData();

        RegistrationRequestPojoModel data = new RegistrationRequestPojoModel();
        data.setUsername(username);
        data.setPassword(password);

        Registration415ResponsePojoModel responsePojoModel = given()
                .log().all()
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(415)
                .extract()
                .as(Registration415ResponsePojoModel.class);
        String detail = "Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.";
        assertEquals(detail,responsePojoModel.getDetail());
    }

    @Test
    @DisplayName("Registration empty password")
    public  void RegistrationTestPojoEmptyPassword(){
        generateTestData();

        RegistrationRequestPojoModel data = new RegistrationRequestPojoModel();
        data.setUsername(username);

        RegistrationValidationErrorResponsePojoModel responsePojoModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .extract()
                .as(RegistrationValidationErrorResponsePojoModel.class);
        errorMsg= "This field may not be null.";
        assertEquals(errorMsg,responsePojoModel.getPassword().get(0));
    }

    @Test
    @DisplayName("Registration empty username")
    public  void RegistrationTestPojoEmptyUsername(){
        generateTestData();

        RegistrationRequestPojoModel data = new RegistrationRequestPojoModel();
        data.setPassword(password);

        RegistrationValidationErrorResponsePojoModel responsePojoModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .extract()
                .as(RegistrationValidationErrorResponsePojoModel.class);
        errorMsg= "This field may not be null.";
        assertEquals(errorMsg,responsePojoModel.getUsername().get(0));
    }

    @Test
    @DisplayName("Registration Existing user")
    public  void RegistrationExistingUserTestPojo(){
        generateTestData();

        RegistrationRequestPojoModel data = new RegistrationRequestPojoModel();
        data.setUsername(username);
        data.setPassword(password);

        RegistrationResponsePojoModel responsePojoModel = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .as(RegistrationResponsePojoModel.class);

        assertEquals(username,responsePojoModel.getUsername());

        RegistrationValidationErrorResponsePojoModel responsePojoModel2 = given()
                .log().all()
                .contentType(JSON)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .log().all()
                .statusCode(400)
                .extract()
                .as(RegistrationValidationErrorResponsePojoModel.class);
        errorMsg = "A user with that username already exists.";
        assertEquals(errorMsg,responsePojoModel2.getUsername().get(0));
    }
}

