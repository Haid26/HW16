package test;

import models.common.ErrorDetailResponseModel;
import models.common.UsernamePasswordValidationErrorResponseModel;
import models.login.LoginRequestModel;
import models.login.LoginResponseModel;
import models.logout.LogoutEmptyRequestModel;
import models.logout.LogoutRequestModel;
import models.logout.LogoutResponseErrorTokenModel;
import models.logout.LogoutResponseWithoutTokenModel;
import models.registration.RegistrationRequestModel;
import models.registration.RegistrationResponseModel;
import net.datafaker.Faker;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static specs.login.LoginSpec.*;
import static specs.login.LoginSpec.errorPasswordLoginResponseSpec;
import static specs.logout.LogoutSpec.*;
import static specs.registraion.RegistrationSpec.registrationRequestSpec;
import static specs.registraion.RegistrationSpec.successfulRegistrationResponseSpec;

@DisplayName("Logout tests")
public class LogoutTests extends TestBase {
    String username;
    String password;
    String errorMsg;
    String actualRefresh;
    String actualAccess;

    @BeforeEach
    public void generateTestData() {
        Faker faker = new Faker();
        username = faker.internet().username();
        password = faker.internet().password();
    }

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
        LoginRequestModel loginData = new LoginRequestModel(username, password);

        LoginResponseModel loginResponse = given(loginRequestSpec)
                .body(loginData)
                .when()
                .post("/auth/token/")
                .then()
                .spec(successfulLoginResponseSpec)
                .extract()
                .as(LoginResponseModel.class);

        String expectedTokenPath = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        actualRefresh = loginResponse.refresh();
        actualAccess = loginResponse.access();

        assertThat(actualAccess).startsWith(expectedTokenPath);
        assertThat(actualRefresh).startsWith(expectedTokenPath);
        assertThat(actualAccess).isNotEqualTo(actualRefresh);
    }

    @Test
    @DisplayName("Successful logout test with registration and login")
    public void successfulLogoutTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);

        given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(successfulLogoutResponseSpec);

    }

    @Test
    @DisplayName("Logout without /")
    public void logoutWithWrongUrl() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);

        given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout")
                .then()
                .spec(error500LogoutResponseSpec);

    }

    @Test
    @DisplayName("Logout without content type header")
    public void logoutNoContentTypeHeaderTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);

        ErrorDetailResponseModel responseModel = given(logoutNoContentTypeRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(noContentTypeLogoutResponseSpec)
                .extract()
                .as(ErrorDetailResponseModel.class);

        String detail = "Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.";
        assertEquals(detail, responseModel.detail());
    }

    @Test
    @DisplayName("Logout null token")
    public void logoutNullTokenTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        LogoutRequestModel logoutData = new LogoutRequestModel(null);

        LogoutResponseWithoutTokenModel responseModel = given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(errorEmptyTokenLogoutResponseSpec)
                .extract()
                .as(LogoutResponseWithoutTokenModel.class);

        String errorMsg = "This field may not be null.";

        assertEquals(errorMsg, responseModel.refresh().get(0));
    }

    @Test
    @DisplayName("Logout empty token")
    public void logoutEmptyTokenTest() {
        LogoutEmptyRequestModel logoutData = new LogoutEmptyRequestModel();

        LogoutResponseWithoutTokenModel responseModel = given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(errorEmptyTokenLogoutResponseSpec)
                .extract()
                .as(LogoutResponseWithoutTokenModel.class);

        String errorMsg = "This field is required.";

        assertEquals(errorMsg, responseModel.refresh().get(0));
    }

    @Test
    @DisplayName("Logout invalid token")
    public void logoutInvalidTokenTest() {
        LogoutRequestModel logoutData = new LogoutRequestModel("123");

        LogoutResponseErrorTokenModel responseModel = given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(errorTokenLogoutResponseSpec)
                .extract()
                .as(LogoutResponseErrorTokenModel.class);

        String detail = "Token is invalid";
        String code = "token_not_valid";
        assertEquals(detail, responseModel.detail());
        assertEquals(code,responseModel.code());
    }

    @Test
    @DisplayName("Logout after being already logged out")
    public void LogoutTwiceTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        LogoutRequestModel logoutData = new LogoutRequestModel(actualRefresh);
        given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(successfulLogoutResponseSpec);

        LogoutResponseErrorTokenModel responseModel2 =  given(logoutRequestSpec)
                .body(logoutData)
                .when()
                .post("/auth/logout/")
                .then()
                .spec(errorTokenLogoutResponseSpec)
                .extract()
                .as(LogoutResponseErrorTokenModel.class);

        String detail = "Token is blacklisted";
        String code = "token_not_valid";
        assertEquals(detail, responseModel2.detail());
        assertEquals(code,responseModel2.code());
    }

}
