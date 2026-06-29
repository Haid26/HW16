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

    @Step("Генерация тестовых данных")
    public void generateTestData() {
        Faker faker = new Faker();
        username = faker.internet().username();
        password = faker.internet().password();
    }

    public void registerUser(String username, String password) {

        RegistrationRequestModel registrationData = new RegistrationRequestModel(username, password);
        RegistrationResponseModel registrationResponseModel = step("регистрация пользователя", () ->
                given(registrationRequestSpec)
                        .body(registrationData)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(successfulRegistrationResponseSpec)
                        .extract()
                        .as(RegistrationResponseModel.class)
        );

        step("проверка ответа метода", () ->
                assertEquals(username, registrationResponseModel.username()));
    }

    @Test
    @DisplayName("Successful login test with registration")
    public void successfulLoginTest() {
        generateTestData();

        registerUser(username, password);

        LoginRequestModel loginData = new LoginRequestModel(username, password);
        LoginResponseModel loginResponse = step("успешная авторизация", () ->
                given(loginRequestSpec)
                        .body(loginData)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(successfulLoginResponseSpec)
                        .extract()
                        .as(LoginResponseModel.class)
        );
        String actualRefresh = loginResponse.refresh();
        String actualAccess = loginResponse.access();
        step("проверка ответа метода", () -> {
            assertThat(actualAccess).startsWith(EXPECTED_TOKEN_PATH);
            assertThat(actualRefresh).startsWith(EXPECTED_TOKEN_PATH);
            assertThat(actualAccess).isNotEqualTo(actualRefresh);
        });

    }

    @Test
    @DisplayName("Login without /")
    public void loginWithWrongUrl() {
        generateTestData();
        LoginRequestModel data = new LoginRequestModel(username, password);

        step("отправка запроса без /", () ->
                given(loginRequestSpec)
                        .body(data)
                        .when()
                        .post("/auth/token")
                        .then()
                        .spec(error500LoginResponseSpec)
        );
    }

    @Test
    @DisplayName("Login without content type header")
    public void loginNoContentTypeHeaderTest() {
        generateTestData();

        LoginRequestModel data = new LoginRequestModel(username, password);
        ErrorDetailResponseModel responseModel = step("отправка запроса без content type", () ->
                given(loginNoContentTypeRequestSpec)
                        .body(data)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(noContentTypeLoginResponseSpec)
                        .extract()
                        .as(ErrorDetailResponseModel.class)
        );
        step("проверка ответа метода", () ->
                assertEquals(EXPECTED_ERROR_UNSUPPORTED_MEDIA_TYPE, responseModel.detail()));
    }

    @Test
    @DisplayName("Login empty password")
    public void loginTestEmptyPassword() {
        generateTestData();
        LoginRequestModel data = new LoginRequestModel(username, null);

        UsernamePasswordValidationErrorResponseModel responseModel = step("отправка запроса логин без пароля", () ->
                given(loginRequestSpec)
                        .body(data)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(errorPasswordLoginResponseSpec)
                        .extract()
                        .as(UsernamePasswordValidationErrorResponseModel.class)
        );
        step("проверка ответа метода", () ->
                assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.password().get(0)));
    }

    @Test
    @DisplayName("Login empty username")
    public void loginTestEmptyUsername() {
        generateTestData();
        LoginRequestModel data = new LoginRequestModel(null, password);

        UsernamePasswordValidationErrorResponseModel responseModel = step("авторизация без логина", () ->
                given(loginRequestSpec)
                        .body(data)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(errorUserNameLoginResponseSpec)
                        .extract()
                        .as(UsernamePasswordValidationErrorResponseModel.class)
        );

        step("проверка ответа метода", () ->
                assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.username().get(0)));
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
        LoginRequestModel loginData = new LoginRequestModel(actualUsername, password);

        ErrorDetailResponseModel loginResponse = step("попытка авторизации с неверным паролем", () ->
             given(loginRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(errorWrongCredentialsLoginResponseSpec)
                    .extract()
                    .as(ErrorDetailResponseModel.class)
        );
        step("проверка ответа метода", () ->
            assertThat(loginResponse.detail()).isEqualTo(EXPECTED_ERROR_WRONG_CREDENTIALS));
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
        LoginRequestModel loginData = new LoginRequestModel(username, actualPassword);

        ErrorDetailResponseModel loginResponse = step("попытка авторизации с паролем другого пользователя", () ->
            given(loginRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(errorWrongCredentialsLoginResponseSpec)
                    .extract()
                    .as(ErrorDetailResponseModel.class)
        );

        step("проверка ответа метода", () ->
            assertThat(loginResponse.detail()).isEqualTo(EXPECTED_ERROR_WRONG_CREDENTIALS));
    }
}
