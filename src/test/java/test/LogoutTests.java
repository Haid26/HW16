package test;

import io.qameta.allure.Step;
import models.common.ErrorDetailResponseModel;
import models.login.LoginRequestModel;
import models.login.LoginResponseModel;
import models.logout.LogoutEmptyRequestModel;
import models.logout.LogoutRequestModel;
import models.logout.LogoutResponseErrorTokenModel;
import models.logout.LogoutResponseWithoutTokenModel;
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
import static specs.logout.LogoutSpec.*;
import static specs.registraion.RegistrationSpec.registrationRequestSpec;
import static specs.registraion.RegistrationSpec.successfulRegistrationResponseSpec;
import static testData.TestData.*;

@DisplayName("Logout tests")
public class LogoutTests extends TestBase {
    String username;
    String password;
    String actualRefresh;
    String actualAccess;

    @Step("Генерация тестовых данных")
    public void generateTestData() {
        Faker faker = new Faker();
        username = faker.internet().username();
        password = faker.internet().password();
    }

    public void registerUser(String username, String password) {
        RegistrationRequestModel registrationData = new RegistrationRequestModel(username, password);
        RegistrationResponseModel registrationResponseModel = step("Регистрация пользователя", () ->
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


    public void loginUser(String username, String password) {
        LoginRequestModel loginData = new LoginRequestModel(username, password);
        LoginResponseModel loginResponse = step("авторизация пользователя", () ->
                given(loginRequestSpec)
                        .body(loginData)
                        .when()
                        .post("/auth/token/")
                        .then()
                        .spec(successfulLoginResponseSpec)
                        .extract()
                        .as(LoginResponseModel.class)
        );

        actualRefresh = loginResponse.refresh();
        actualAccess = loginResponse.access();

        step("проверка ответа метода", () -> {
            assertThat(actualAccess).startsWith(EXPECTED_TOKEN_PATH);
            assertThat(actualRefresh).startsWith(EXPECTED_TOKEN_PATH);
            assertThat(actualAccess).isNotEqualTo(actualRefresh);
        });
    }

    @Test
    @DisplayName("Successful logout test with registration and login")
    public void successfulLogoutTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);
        LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);

        step("успешный логаут", () ->
                given(logoutRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(successfulLogoutResponseSpec)
        );
    }

    @Test
    @DisplayName("Logout without /")
    public void logoutWithWrongUrl() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);
        LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);

        step("отправка запроса без /", () -> {
            given(logoutRequestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout")
                    .then()
                    .spec(error500LogoutResponseSpec);
        });
    }

    @Test
    @DisplayName("Logout without content type header")
    public void logoutNoContentTypeHeaderTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);
        LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);

        ErrorDetailResponseModel responseModel = step("отправка запроса без content type", () ->
                given(logoutNoContentTypeRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(noContentTypeLogoutResponseSpec)
                        .extract()
                        .as(ErrorDetailResponseModel.class)
        );
        step("проверка ответа метода", () ->
                assertEquals(EXPECTED_ERROR_UNSUPPORTED_MEDIA_TYPE, responseModel.detail()));
    }

    @Test
    @DisplayName("Logout null token")
    public void logoutNullTokenTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        LogoutRequestModel logoutData = new LogoutRequestModel(null);
        LogoutResponseWithoutTokenModel responseModel = step("отправка запроса где токен = null", () ->
                given(logoutRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(errorEmptyTokenLogoutResponseSpec)
                        .extract()
                        .as(LogoutResponseWithoutTokenModel.class)

        );

        step("проверка ответа метода", () ->
                assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.refresh().get(0)));
    }

    @Test
    @DisplayName("Logout empty token")
    public void logoutEmptyTokenTest() {
        LogoutEmptyRequestModel logoutData = new LogoutEmptyRequestModel();
        LogoutResponseWithoutTokenModel responseModel = step("отправка запроса без токена", () ->
                given(logoutRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(errorEmptyTokenLogoutResponseSpec)
                        .extract()
                        .as(LogoutResponseWithoutTokenModel.class)
        );

        step("проверка ответа метода", () ->
                assertEquals(EXPECTED_ERROR_REQUIRED_FIELD, responseModel.refresh().get(0)));
    }

    @Test
    @DisplayName("Logout invalid token")
    public void logoutInvalidTokenTest() {
        LogoutRequestModel logoutData = new LogoutRequestModel("123");

        LogoutResponseErrorTokenModel responseModel = step("отправка запроса с невалидным токеном", () ->
                given(logoutRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(errorTokenLogoutResponseSpec)
                        .extract()
                        .as(LogoutResponseErrorTokenModel.class)
        );

        step("проверка ответа метода", () -> {
            assertEquals(EXPECTED_ERROR_INVALID_TOKEN, responseModel.detail());
            assertEquals(EXPECTED_ERROR_CODE_INVALID_TOKEN, responseModel.code());
        });
    }

    @Test
    @DisplayName("Logout after being already logged out")
    public void LogoutTwiceTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);
        step("первый логаут", () ->
                given(logoutRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(successfulLogoutResponseSpec)
        );
        LogoutResponseErrorTokenModel responseModel2 = step("второй логаут", () ->
                given(logoutRequestSpec)
                        .body(logoutData)
                        .when()
                        .post("/auth/logout/")
                        .then()
                        .spec(errorTokenLogoutResponseSpec)
                        .extract()
                        .as(LogoutResponseErrorTokenModel.class)
        );

        step("проверка ответа метода", () -> {
            assertEquals(EXPECTED_ERROR_TOKEN_BLACKLISTED, responseModel2.detail());
            assertEquals(EXPECTED_ERROR_CODE_INVALID_TOKEN, responseModel2.code());
        });
    }

}
