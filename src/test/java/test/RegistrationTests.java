package test;

import models.common.ErrorDetailResponseModel;
import models.registration.RegistrationRequestModel;
import models.registration.RegistrationResponseModel;
import models.registration.RegistrationValidationErrorResponseModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static specs.registraion.RegistrationSpec.*;

@DisplayName("Registration tests")
public class RegistrationTests extends TestBase{
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
    public  void successfulRegistrationTest(){
        generateTestData();

        RegistrationRequestModel registrationData = new RegistrationRequestModel(username,password);


        RegistrationResponseModel responseModel = given(registrationRequestSpec)
                .body(registrationData)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(RegistrationResponseModel.class);

        assertEquals(username,responseModel.username());
    }

    @Test
    @DisplayName("Registration without /")
    public  void registrationWithWrongUrl(){
        generateTestData();

        RegistrationRequestModel data = new RegistrationRequestModel(username,password);

        given(registrationRequestSpec)
                .body(data)
                .when()
                .post("/users/register")
                .then()
                .spec(error500RegistrationResponseSpec);
    }

    @Test
    @DisplayName("Registration without content type header")
    public  void registrationNoContentTypeHeaderTest(){
        generateTestData();

        RegistrationRequestModel data = new RegistrationRequestModel(username,password);

        ErrorDetailResponseModel responseModel = given(registrationNoContentTypeRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(noContentTypeRegistrationResponseSpec)
                .extract()
                .as(ErrorDetailResponseModel.class);

        String detail = "Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.";
        assertEquals(detail,responseModel.detail());
    }

    @Test
    @DisplayName("Registration empty password")
    public  void RegistrationTestEmptyPassword(){
        generateTestData();

        RegistrationRequestModel data = new RegistrationRequestModel(username,null);

        RegistrationValidationErrorResponseModel responseModel = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(errorPasswordRegistrationResponseSpec)
                .extract()
                .as(RegistrationValidationErrorResponseModel.class);

        errorMsg="This field may not be null.";
        assertEquals(errorMsg,responseModel.password().get(0));
    }

    @Test
    @DisplayName("Registration empty username")
    public  void RegistrationTestEmptyUsername(){
        generateTestData();

        RegistrationRequestModel data = new RegistrationRequestModel(null,password);

        RegistrationValidationErrorResponseModel responseModel = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(errorUserNameRegistrationResponseSpec)
                .extract()
                .as(RegistrationValidationErrorResponseModel.class);

        errorMsg = "This field may not be null.";
        assertEquals(errorMsg,responseModel.username().get(0));
    }

    @Test
    @DisplayName("Registration Existing user")
    public  void RegistrationExistingUserTest(){
        generateTestData();

        RegistrationRequestModel data = new RegistrationRequestModel(username,password);

        RegistrationResponseModel responseModel = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(successfulRegistrationResponseSpec)
                .extract()
                .as(RegistrationResponseModel.class);

        assertEquals(username,responseModel.username());

        RegistrationValidationErrorResponseModel responseModel2 = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(errorUserNameRegistrationResponseSpec)
                .extract()
                .as(RegistrationValidationErrorResponseModel.class);

        errorMsg = "A user with that username already exists.";
        assertEquals(errorMsg,responseModel2.username().get(0));
    }
}

