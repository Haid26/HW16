package test;

import models.common.ErrorDetailResponseModel;
import models.registration.RegistrationRequestModel;
import models.registration.RegistrationResponseModel;
import models.common.UsernamePasswordValidationErrorResponseModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static specs.registraion.RegistrationSpec.*;
import static testData.TestData.*;

@DisplayName("Registration tests")
public class RegistrationTests extends TestBase{
    String username;
    String password;
    String errorMsg;


    public void generateTestData(){
        Faker faker = new Faker();
        username= faker.internet().username();
        password = faker.internet().password();
    }

    @Test
    @DisplayName("Registration successful")
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

        assertEquals(EXPECTED_ERROR_UNSUPPORTED_MEDIA_TYPE,responseModel.detail());
    }

    @Test
    @DisplayName("Registration empty password")
    public  void registrationTestEmptyPassword(){
        generateTestData();

        RegistrationRequestModel data = new RegistrationRequestModel(username,null);

        UsernamePasswordValidationErrorResponseModel responseModel = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(errorPasswordRegistrationResponseSpec)
                .extract()
                .as(UsernamePasswordValidationErrorResponseModel.class);

        assertEquals(EXPECTED_ERROR_NULL_VALUE,responseModel.password().get(0));
    }

    @Test
    @DisplayName("Registration empty username")
    public  void registrationTestEmptyUsername(){
        generateTestData();

        RegistrationRequestModel data = new RegistrationRequestModel(null,password);

        UsernamePasswordValidationErrorResponseModel responseModel = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(errorUserNameRegistrationResponseSpec)
                .extract()
                .as(UsernamePasswordValidationErrorResponseModel.class);

        assertEquals(EXPECTED_ERROR_NULL_VALUE,responseModel.username().get(0));
    }

    @Test
    @DisplayName("Registration Existing user")
    public  void registrationExistingUserTest(){
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

        UsernamePasswordValidationErrorResponseModel responseModel2 = given(registrationRequestSpec)
                .body(data)
                .when()
                .post("/users/register/")
                .then()
                .spec(errorUserNameRegistrationResponseSpec)
                .extract()
                .as(UsernamePasswordValidationErrorResponseModel.class);

        assertEquals(EXPECTED_ERROR_EXISTING_USER,responseModel2.username().get(0));
    }
}

