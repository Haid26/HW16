package test;

import io.qameta.allure.Step;
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

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static specs.login.LoginSpec.loginRequestSpec;
import static specs.login.LoginSpec.successfulLoginResponseSpec;
import static specs.registraion.RegistrationSpec.registrationRequestSpec;
import static specs.registraion.RegistrationSpec.successfulRegistrationResponseSpec;
import static specs.updateUser.UpdateUserSpec.*;
import static testData.TestData.*;

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

    public void generateTestData() {
        Faker faker = new Faker();
        username = faker.internet().username();
        password = faker.internet().password();
        firstName = faker.name().firstName();
        lastName = faker.name().lastName();
        email = faker.internet().emailAddress();

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
        id = registrationResponseModel.id();
    }

    @Step("Авторизация пользователя")
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


        actualRefresh = loginResponse.refresh();
        actualAccess = loginResponse.access();

        assertThat(actualAccess).startsWith(EXPECTED_TOKEN_PATH);
        assertThat(actualRefresh).startsWith(EXPECTED_TOKEN_PATH);
        assertThat(actualAccess).isNotEqualTo(actualRefresh);
    }

    @Test
    @DisplayName("Successful Patch full info update test")
    public void successfulPatchUpdateUserInfoTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        actualUserName = username;
        actualPassword = password;
        generateTestData();
        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);
        step("успешное обновление всех данных", () -> {
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
        });
    }

    @Test
    @DisplayName("Patch Update user info without /")
    public void patchUpdateUserInfoWithWrongUrlTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);
        step("отправка запроса без /", () -> {
            given(updateUserRequestSpec)
                    .body(updateUserRequest)
                    .headers("Authorization",
                            "Bearer " + actualAccess)
                    .when()
                    .patch("/users/me")
                    .then()
                    .spec(error500UpdateUserResponseSpec);
        });
    }

    @Test
    @DisplayName("Patch update user info without content type test")
    public void patchUpdateUserInfoNoContentTypeTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);
        step("отправка запроса без content type", () -> {
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

            assertEquals(EXPECTED_ERROR_UNSUPPORTED_MEDIA_TYPE, responseModel.detail());
        });
    }

    @Test
    @DisplayName("Patch update user without Token")
    public void patchUpdateUserInfoWithoutTokenTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);
        step("отправка запроса без токена", () -> {
            ErrorDetailResponseModel responseModel = given(updateUserRequestSpec)
                    .body(updateUserRequest)
                    .when()
                    .patch("/users/me/")
                    .then()
                    .spec(noTokenUpdateUserResponseSpec)
                    .extract()
                    .as(ErrorDetailResponseModel.class);

            assertEquals(EXPECTED_ERROR_NO_TOKEN, responseModel.detail());
        });
    }

    @Test
    @DisplayName("successful Patch Partial info update test")
    public void successfulPatchPartialUpdateUserInfoTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserPartialRequestModel updateUserRequest = new UpdateUserPartialRequestModel(firstName, lastName);
        step("частичное обновление данных", () -> {
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
        });
    }

    @Test
    @DisplayName("Test with null username")
    public void updateUserNullUsernameTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(null, firstName, lastName, email);
        step("отправка запроса username=null", () -> {
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

            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.username().get(0));
        });

    }

    @Test
    @DisplayName("Test with null first name")
    public void updateUserNullFirstNameTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, null, lastName, email);
        step("отправка запроса firstName=null", () -> {
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

            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.firstName().get(0));
        });
    }

    @Test
    @DisplayName("Test with null last name")
    public void updateUserNullLastNameTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, null, email);
        step("отправка запроса lastName =null", () -> {
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

            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.lastName().get(0));
        });
    }

    @Test
    @DisplayName("Test with null email")
    public void updateUserNullEmailTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, lastName, lastName, null);
        step("отправка запроса email=null", () -> {
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

            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.email().get(0));
        });
    }

    @Test
    @DisplayName("Patch Test with all null parameters")
    public void patchUpdateUserNullAllParametersTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(null, null, null, null);
        step("отправка запроса c null параметрами", () -> {
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

            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.username().get(0));
            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.firstName().get(0));
            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.lastName().get(0));
            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.email().get(0));
        });
    }

    @Test
    @DisplayName("Successful Put full info update test")
    public void successfulPutUpdateUserInfoTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        actualUserName = username;
        actualPassword = password;
        generateTestData();
        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);
        step("update info через put", () -> {
            UpdateUserSuccessfulResponseModel responseModel = given(updateUserRequestSpec)
                    .body(updateUserRequest)
                    .headers("Authorization",
                            "Bearer " + actualAccess)
                    .when()
                    .put("/users/me/")
                    .then()
                    .spec(successfulUpdateUserResponseSpec)
                    .extract()
                    .as(UpdateUserSuccessfulResponseModel.class);

            assertEquals(id, responseModel.id());
            assertEquals(username, responseModel.username());
            assertEquals(firstName, responseModel.firstName());
            assertEquals(lastName, responseModel.lastName());
            assertEquals(email, responseModel.email());
        });
    }

    @Test
    @DisplayName("Put Update user info without /")
    public void putUpdateUserInfoWithWrongUrlTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);
        step("update info через put", () -> {
            given(updateUserRequestSpec)
                    .body(updateUserRequest)
                    .headers("Authorization",
                            "Bearer " + actualAccess)
                    .when()
                    .put("/users/me")
                    .then()
                    .spec(error500UpdateUserResponseSpec);
        });
    }

    @Test
    @DisplayName("Put update user info without content type test")
    public void putUpdateUserInfoNoContentTypeTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);
        step("update info через put", () -> {
            ErrorDetailResponseModel responseModel = given(updateUserNoContentTypeRequestSpec)
                    .body(updateUserRequest)
                    .headers("Authorization",
                            "Bearer " + actualAccess)
                    .when()
                    .put("/users/me/")
                    .then()
                    .spec(noContentTypeUpdateUserResponseSpec)
                    .extract()
                    .as(ErrorDetailResponseModel.class);

            assertEquals(EXPECTED_ERROR_UNSUPPORTED_MEDIA_TYPE, responseModel.detail());
        });
    }

    @Test
    @DisplayName("Put update user without Token")
    public void putUpdateUserInfoWithoutTokenTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username, firstName, lastName, email);
        step("update info через put", () -> {
            ErrorDetailResponseModel responseModel = given(updateUserRequestSpec)
                    .body(updateUserRequest)
                    .when()
                    .patch("/users/me/")
                    .then()
                    .spec(noTokenUpdateUserResponseSpec)
                    .extract()
                    .as(ErrorDetailResponseModel.class);

            assertEquals(EXPECTED_ERROR_NO_TOKEN, responseModel.detail());
        });
    }

    @Test
    @DisplayName("Put Partial info update test")
    public void putPartialUpdateUserInfoTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserPartialRequestModel updateUserRequest = new UpdateUserPartialRequestModel(firstName, lastName);
        step("update info через put", () -> {
            UpdateUserNullErorResponseModel responseModel = given(updateUserRequestSpec)
                    .body(updateUserRequest)
                    .headers("Authorization",
                            "Bearer " + actualAccess)
                    .when()
                    .put("/users/me/")
                    .then()
                    .spec(nullDataUpdateUserResponseSpec)
                    .extract()
                    .as(UpdateUserNullErorResponseModel.class);

            assertEquals(EXPECTED_ERROR_REQUIRED_FIELD, responseModel.username().get(0));
            assertEquals(EXPECTED_ERROR_REQUIRED_FIELD, responseModel.email().get(0));
        });
    }

    @Test
    @DisplayName("Put Test with all null parameters")
    public void putUpdateUserNullAllParametersTest() {
        generateTestData();
        registerUser(username, password);
        loginUser(username, password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(null, null, null, null);
        step("update info через put", () -> {
            UpdateUserNullErorResponseModel responseModel = given(updateUserRequestSpec)
                    .body(updateUserRequest)
                    .headers("Authorization",
                            "Bearer " + actualAccess)
                    .when()
                    .put("/users/me/")
                    .then()
                    .spec(nullDataUpdateUserResponseSpec)
                    .extract()
                    .as(UpdateUserNullErorResponseModel.class);

            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.username().get(0));
            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.firstName().get(0));
            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.lastName().get(0));
            assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.email().get(0));
        });
    }
}
