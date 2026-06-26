package test;

import models.common.ErrorDetailResponseModel;
import models.login.LoginRequestModel;
import models.login.LoginResponseModel;
import models.registration.RegistrationRequestModel;
import models.registration.RegistrationResponseModel;
import models.updateUser.UpdateUserFullRequestModel;
import models.updateUser.UpdateUserNullErorResponseModel;
import models.updateUser.UpdateUserPartialRequestModel;
import models.updateUser.UpdateUserSuccessfulResponseModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import specs.updateUser.UpdateUserSpec;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.registraion.RegistrationSpec.registrationRequestSpec;
import static specs.registraion.RegistrationSpec.successfulRegistrationResponseSpec;
import static specs.updateUser.UpdateUserSpec.*;

@DisplayName("Tests for update user info method")
public class UpdateUserTests extends TestBase {
    String username;
    String password;
    String actualRefresh;
    String actualAccess;
    String firstName;
    String lastName;
    String email;
    Integer id;
    String detail;
    String actualUserName;
    String actualPassword;
    final String ERRMSG = "This field may not be null.";

    @BeforeEach
    public void generateTestData() {
        Faker faker = new Faker();
        username = faker.internet().username();
        password = faker.internet().password();
        firstName = faker.name().firstName();
        lastName = faker.name().lastName();
        email = faker.internet().emailAddress();

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
        id = registrationResponseModel.id();
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
    @DisplayName("Successful full info update test")
    public void successfulUpdateUserInfoTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        actualUserName = username;
        actualPassword = password;
        generateTestData();
        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);

        UpdateUserSuccessfulResponseModel responseModel = given(updateUserRequestSpec)
                .body(updateUserRequest)
                .headers("Authorization",
                        "Bearer " + actualAccess)
                .when()
                .patch("/users/me/")
                .then()
                .spec(successfulUpdateUserResponseSpec)
                .extract()
                .as(UpdateUserSuccessfulResponseModel.class);

        assertEquals(id, responseModel.id());
        assertEquals(username, responseModel.username());
        assertEquals(firstName, responseModel.firstName());
        assertEquals(lastName, responseModel.lastName());
        assertEquals(email, responseModel.email());
    }

    @Test
    @DisplayName("Update user info without /")
    public void updateUserInfoWithWrongUrlTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);

        given(updateUserRequestSpec)
                .body(updateUserRequest)
                .headers("Authorization",
                        "Bearer " + actualAccess)
                .when()
                .patch("/users/me")
                .then()
                .spec(error500UpdateUserResponseSpec);
    }

    @Test
    @DisplayName("update user info without content type test")
    public void updateUserInfoNoContentTypeTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);

        ErrorDetailResponseModel responseModel = given(updateUserNoContentTypeRequestSpec)
                .body(updateUserRequest)
                .headers("Authorization",
                        "Bearer " + actualAccess)
                .when()
                .patch("/users/me/")
                .then()
                .spec(noContentTypeUpdateUserResponseSpec)
                .extract()
                .as(ErrorDetailResponseModel.class);

        detail = "Unsupported media type \"text/plain; charset=ISO-8859-1\" in request.";
        assertEquals(detail, responseModel.detail());
    }

    @Test
    @DisplayName("Test without Token")
    public void updateUserInfoWithoutTokenTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);

        ErrorDetailResponseModel responseModel = given(updateUserRequestSpec)
                .body(updateUserRequest)
                .when()
                .patch("/users/me/")
                .then()
                .spec(noTokenUpdateUserResponseSpec)
                .extract()
                .as(ErrorDetailResponseModel.class);

        detail = "Authentication credentials were not provided.";
        assertEquals(detail, responseModel.detail());
    }

    @Test
    @DisplayName("successful Partial info update test")
    public void successfulPartialUpdateUserInfoTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserPartialRequestModel updateUserRequest = new UpdateUserPartialRequestModel(firstName, lastName);

        UpdateUserSuccessfulResponseModel responseModel = given(updateUserRequestSpec)
                .body(updateUserRequest)
                .headers("Authorization",
                        "Bearer " + actualAccess)
                .when()
                .patch("/users/me/")
                .then()
                .spec(successfulUpdateUserResponseSpec)
                .extract()
                .as(UpdateUserSuccessfulResponseModel.class);

        assertEquals(id, responseModel.id());
        assertEquals(username, responseModel.username());
        assertEquals(firstName, responseModel.firstName());
        assertEquals(lastName, responseModel.lastName());
    }

    @Test
    @DisplayName("Test with null username")
    public void updateUserNullUsernameTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(null, firstName, lastName, email);

        UpdateUserNullErorResponseModel responseModel = given(updateUserRequestSpec)
                .body(updateUserRequest)
                .headers("Authorization",
                        "Bearer " + actualAccess)
                .when()
                .patch("/users/me/")
                .then()
                .spec(nullDataUpdateUserResponseSpec)
                .extract()
                .as(UpdateUserNullErorResponseModel.class);

        assertEquals(ERRMSG, responseModel.username().get(0));

    }

    @Test
    @DisplayName("Test with null first name")
    public void updateUserNullFirstNameTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, null, lastName, email);

        UpdateUserNullErorResponseModel responseModel = given(updateUserRequestSpec)
                .body(updateUserRequest)
                .headers("Authorization",
                        "Bearer " + actualAccess)
                .when()
                .patch("/users/me/")
                .then()
                .spec(nullDataUpdateUserResponseSpec)
                .extract()
                .as(UpdateUserNullErorResponseModel.class);

        assertEquals(ERRMSG, responseModel.firstName().get(0));

    }

    @Test
    @DisplayName("Test with null last name")
    public void updateUserNullLastNameTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, null, email);

        UpdateUserNullErorResponseModel responseModel = given(updateUserRequestSpec)
                .body(updateUserRequest)
                .headers("Authorization",
                        "Bearer " + actualAccess)
                .when()
                .patch("/users/me/")
                .then()
                .spec(nullDataUpdateUserResponseSpec)
                .extract()
                .as(UpdateUserNullErorResponseModel.class);

        assertEquals(ERRMSG, responseModel.lastName().get(0));

    }

    @Test
    @DisplayName("Test with null email")
    public void updateUserNullEmailTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, lastName, lastName, null);

        UpdateUserNullErorResponseModel responseModel = given(updateUserRequestSpec)
                .body(updateUserRequest)
                .headers("Authorization",
                        "Bearer " + actualAccess)
                .when()
                .patch("/users/me/")
                .then()
                .spec(nullDataUpdateUserResponseSpec)
                .extract()
                .as(UpdateUserNullErorResponseModel.class);

        assertEquals(ERRMSG, responseModel.email().get(0));

    }

    @Test
    @DisplayName("Test with all null parameters")
    public void updateUserNullAllParametersTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(null, null, null, null);

        UpdateUserNullErorResponseModel responseModel = given(updateUserRequestSpec)
                .body(updateUserRequest)
                .headers("Authorization",
                        "Bearer " + actualAccess)
                .when()
                .patch("/users/me/")
                .then()
                .spec(nullDataUpdateUserResponseSpec)
                .extract()
                .as(UpdateUserNullErorResponseModel.class);

        assertEquals(ERRMSG, responseModel.username().get(0));
        assertEquals(ERRMSG, responseModel.firstName().get(0));
        assertEquals(ERRMSG, responseModel.lastName().get(0));
        assertEquals(ERRMSG, responseModel.email().get(0));

    }
}
