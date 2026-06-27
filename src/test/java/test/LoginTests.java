package test;


import io.qameta.allure.Step;
import models.common.ErrorDetailResponseModel;
import models.common.UsernamePasswordValidationErrorResponseModel;
import models.login.LoginRequestModel;
import models.login.LoginResponseModel;
import models.registration.RegistrationRequestModel;
import models.registration.RegistrationResponseModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static specs.login.LoginSpec.*;
import static specs.registraion.RegistrationSpec.*;
import static testData.TestData.*;

@DisplayName("Login tests")
public class LoginTests extends TestBase {
    String username;
    String password;
    String errorMsg;

    public void generateTestData() {
        Faker faker = new Faker();
        username = faker.internet().username();
        password = faker.internet().password();
    }

    public void registerUser(String username, String password) {
        step("регистрация пользователя", () -> {
            RegistrationRequestModel registrationData = new RegistrationRequestModel(username, password);

            RegistrationResponseModel registrationResponseModel = given(registrationRequestSpec)
                    .body(registrationData)
                    .when()
                    .post("/users/register/")
                    .then()
                    .spec(successfulRegistrationResponseSpec)
                    .extract()
                    .as(RegistrationResponseModel.class);

            assertEquals(username, registrationResponseModel.username());
        });
    }

    @Test
    @DisplayName("Successful login test with registration")
    public void successfulLoginTest() {
        generateTestData();

        registerUser(username, password);

        step("успешная авторизация", () -> {
            LoginRequestModel loginData = new LoginRequestModel(username, password);
            LoginResponseModel loginResponse = given(loginRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(successfulLoginResponseSpec)
                    .extract()
                    .as(LoginResponseModel.class);

            String actualRefresh = loginResponse.refresh();
            String actualAccess = loginResponse.access();

            assertThat(actualAccess).startsWith(EXPECTED_TOKEN_PATH);
            assertThat(actualRefresh).startsWith(EXPECTED_TOKEN_PATH);
            assertThat(actualAccess).isNotEqualTo(actualRefresh);
        });
    }

    @Test
    @DisplayName("Login without /")
    public void loginWithWrongUrl() {
        generateTestData();

        step("отправка запроса без /", () -> {
            LoginRequestModel data = new LoginRequestModel(username, password);
            given(loginRequestSpec)
                    .body(data)
                    .when()
                    .post("/auth/token")
                    .then()
                    .spec(error500LoginResponseSpec);
        });
    }

    @Test
    @DisplayName("Login without content type header")
    public void loginNoContentTypeHeaderTest() {
        generateTestData();

        step("отправка запроса без content type", () -> {
            LoginRequestModel data = new LoginRequestModel(username, password);
            ErrorDetailResponseModel responseModel = given(loginNoContentTypeRequestSpec)
                    .body(data)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(noContentTypeLoginResponseSpec)
                    .extract()
                    .as(ErrorDetailResponseModel.class);

            assertEquals(EXPECTED_ERROR_UNSUPPORTED_MEDIA_TYPE, responseModel.detail());
        });
    }

    @Test
    @DisplayName("Login empty password")
    public void loginTestEmptyPassword() {
        generateTestData();

        step("отправка запроса логин без пароля", ()-> {
            LoginRequestModel data = new LoginRequestModel(username, null);
            UsernamePasswordValidationErrorResponseModel responseModel = given(loginRequestSpec)
                    .body(data)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(errorPasswordLoginResponseSpec)
                    .extract()
                    .as(UsernamePasswordValidationErrorResponseModel.class);

            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.password().get(0));
        });
    }

    @Test
    @DisplayName("Login empty username")
    public void loginTestEmptyUsername() {
        generateTestData();

        step("авторизация без логина", ()-> {
            LoginRequestModel data = new LoginRequestModel(null, password);
            UsernamePasswordValidationErrorResponseModel responseModel = given(loginRequestSpec)
                    .body(data)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(errorUserNameLoginResponseSpec)
                    .extract()
                    .as(UsernamePasswordValidationErrorResponseModel.class);

            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.username().get(0));
        });
    }

    @Test
    @DisplayName("Valid user wrong password")
    public void loginWrongPasswordTest() {
        generateTestData();
        registerUser(username, password);
        String actualUsername = username;
        String actualPassword = password;
        while (actualPassword.equals(password)) {
            generateTestData();
        }
        step("попытка авторизации с неверным паролем", ()-> {
            LoginRequestModel loginData = new LoginRequestModel(actualUsername, password);

            ErrorDetailResponseModel loginResponse = given(loginRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(errorWrongCredentialsLoginResponseSpec)
                    .extract()
                    .as(ErrorDetailResponseModel.class);

            assertThat(loginResponse.detail()).isEqualTo(EXPECTED_ERROR_WRONG_CREDENTIALS);
        });
    }

    @Test
    @DisplayName("Valid password wrong user")
    public void loginWrongUserName() {
        generateTestData();
        registerUser(username, password);
        String actualUsername = username;
        String actualPassword = password;
        while (actualUsername.equals(username)) {
            generateTestData();
        }

        step("попытка авторизации с паролем другого пользователя", ()-> {
            LoginRequestModel loginData = new LoginRequestModel(username, actualPassword);

            ErrorDetailResponseModel loginResponse = given(loginRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(errorWrongCredentialsLoginResponseSpec)
                    .extract()
                    .as(ErrorDetailResponseModel.class);

            assertThat(loginResponse.detail()).isEqualTo(EXPECTED_ERROR_WRONG_CREDENTIALS);
        });
    }
}
