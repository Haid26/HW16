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

    public void generateTestData() {
        Faker faker = new Faker();
        username = faker.internet().username();
        password = faker.internet().password();
    }

    @Step("Регистрация пользователя")
    public void registerUser(String username, String password) {
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
    }


    public void loginUser(String username, String password) {
        step("авторизация пользователя", () -> {
            LoginRequestModel loginData = new LoginRequestModel(username, password);
            LoginResponseModel loginResponse = given(loginRequestSpec)
                    .body(loginData)
                    .when()
                    .post("/auth/token/")
                    .then()
                    .spec(successfulLoginResponseSpec)
                    .extract()
                    .as(LoginResponseModel.class);

            actualRefresh = loginResponse.refresh();
            actualAccess = loginResponse.access();

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

        step("успешный логаут", () -> {
            LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);

            given(logoutRequestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(successfulLogoutResponseSpec);
        });
    }

    @Test
    @DisplayName("Logout without /")
    public void logoutWithWrongUrl() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        step("отправка запроса без /", () -> {
            LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);
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

        step("отправка запроса без content type", () -> {
            LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);
            ErrorDetailResponseModel responseModel = given(logoutNoContentTypeRequestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(noContentTypeLogoutResponseSpec)
                    .extract()
                    .as(ErrorDetailResponseModel.class);

            assertEquals(EXPECTED_ERROR_UNSUPPORTED_MEDIA_TYPE, responseModel.detail());
        });
    }

    @Test
    @DisplayName("Logout null token")
    public void logoutNullTokenTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        step("отправка запроса где токен = null", () -> {
            LogoutRequestModel logoutData = new LogoutRequestModel(null);
            LogoutResponseWithoutTokenModel responseModel = given(logoutRequestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(errorEmptyTokenLogoutResponseSpec)
                    .extract()
                    .as(LogoutResponseWithoutTokenModel.class);

            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.refresh().get(0));
        });
    }

    @Test
    @DisplayName("Logout empty token")
    public void logoutEmptyTokenTest() {
        step("отправка запроса без токена", () -> {
            LogoutEmptyRequestModel logoutData = new LogoutEmptyRequestModel();
            LogoutResponseWithoutTokenModel responseModel = given(logoutRequestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(errorEmptyTokenLogoutResponseSpec)
                    .extract()
                    .as(LogoutResponseWithoutTokenModel.class);

            assertEquals(EXPECTED_ERROR_REQUIRED_FIELD, responseModel.refresh().get(0));
        });
    }

    @Test
    @DisplayName("Logout invalid token")
    public void logoutInvalidTokenTest() {
        step("отправка запроса с невалидным токеном", () -> {
            LogoutRequestModel logoutData = new LogoutRequestModel("123");
            LogoutResponseErrorTokenModel responseModel = given(logoutRequestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(errorTokenLogoutResponseSpec)
                    .extract()
                    .as(LogoutResponseErrorTokenModel.class);


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
        step("первый логаут", () -> {
                    given(logoutRequestSpec)
                            .body(logoutData)
                            .when()
                            .post("/auth/logout/")
                            .then()
                            .spec(successfulLogoutResponseSpec);
                });
        step("второй логаут", () -> {
            LogoutResponseErrorTokenModel responseModel2 = given(logoutRequestSpec)
                    .body(logoutData)
                    .when()
                    .post("/auth/logout/")
                    .then()
                    .spec(errorTokenLogoutResponseSpec)
                    .extract()
                    .as(LogoutResponseErrorTokenModel.class);


            assertEquals(EXPECTED_ERROR_TOKEN_BLACKLISTED, responseModel2.detail());
            assertEquals(EXPECTED_ERROR_CODE_INVALID_TOKEN, responseModel2.code());
        });
    }

}
