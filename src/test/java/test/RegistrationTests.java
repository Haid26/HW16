package test;

import io.qameta.allure.Step;
import models.common.ErrorDetailResponseModel;
import models.registration.RegistrationRequestModel;
import models.registration.RegistrationResponseModel;
import models.common.UsernamePasswordValidationErrorResponseModel;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static specs.registraion.RegistrationSpec.*;
import static testData.TestData.*;

@DisplayName("Registration tests")
public class RegistrationTests extends TestBase {
    String username;
    String password;
    String errorMsg;

    @Step("Генерирация тестовых данных")
    public void generateTestData() {
        Faker faker = new Faker();
        username = faker.internet().username();
        password = faker.internet().password();
    }

    @Test
    @DisplayName("Registration successful")
    public void successfulRegistrationTest() {
        generateTestData();
        RegistrationRequestModel registrationData = new RegistrationRequestModel(username, password);

        RegistrationResponseModel responseModel = step("Вызов метода регистрации", () ->
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
                assertEquals(username, responseModel.username()));
    }

    @Test
    @DisplayName("Registration without /")
    public void registrationWithWrongUrl() {
        generateTestData();
        RegistrationRequestModel data = new RegistrationRequestModel(username, password);

        step("запрос регистрации без /", () ->
                given(registrationRequestSpec)
                        .body(data)
                        .when()
                        .post("/users/register")
                        .then()
                        .spec(error500RegistrationResponseSpec)
        );
    }

    @Test
    @DisplayName("Registration without content type header")
    public void registrationNoContentTypeHeaderTest() {
        generateTestData();
        RegistrationRequestModel data = new RegistrationRequestModel(username, password);

        ErrorDetailResponseModel responseModel = step("Запрос регистрации без указания контента", () ->
                given(registrationNoContentTypeRequestSpec)
                        .body(data)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(noContentTypeRegistrationResponseSpec)
                        .extract()
                        .as(ErrorDetailResponseModel.class)
        );

        step("проверка ответа метода", () ->
                assertEquals(EXPECTED_ERROR_UNSUPPORTED_MEDIA_TYPE, responseModel.detail()));
    }

    @Test
    @DisplayName("Registration empty password")
    public void registrationTestEmptyPassword() {
        generateTestData();
        RegistrationRequestModel data = new RegistrationRequestModel(username, null);

        UsernamePasswordValidationErrorResponseModel responseModel = step("Регистрация с пустым паролем", () ->
                given(registrationRequestSpec)
                        .body(data)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(errorPasswordRegistrationResponseSpec)
                        .extract()
                        .as(UsernamePasswordValidationErrorResponseModel.class)
        );

        step("проверка ответа метода", () ->
                assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.password().get(0)));
    }

    @Test
    @DisplayName("Registration empty username")
    public void registrationTestEmptyUsername() {
        generateTestData();

        RegistrationRequestModel data = new RegistrationRequestModel(null, password);

        UsernamePasswordValidationErrorResponseModel responseModel = step("Регистрация с пустым логином", () ->
                given(registrationRequestSpec)
                        .body(data)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(errorUserNameRegistrationResponseSpec)
                        .extract()
                        .as(UsernamePasswordValidationErrorResponseModel.class)
        );
        step("проверка ответа метода", () ->
                assertEquals(EXPECTED_ERROR_NULL_VALUE, responseModel.username().get(0)));
    }

    @Test
    @DisplayName("Registration Existing user")
    public void registrationExistingUserTest() {
        generateTestData();

        RegistrationRequestModel data = new RegistrationRequestModel(username, password);

        RegistrationResponseModel responseModel = step("отправка первого запроса на регистрацию", () ->
                given(registrationRequestSpec)
                        .body(data)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(successfulRegistrationResponseSpec)
                        .extract()
                        .as(RegistrationResponseModel.class)
        );

        step("проверка ответа метода", () ->
                assertEquals(username, responseModel.username()));

        UsernamePasswordValidationErrorResponseModel responseModel2 = step("отправка второго запрсоа на регистрацию", () ->
                given(registrationRequestSpec)
                        .body(data)
                        .when()
                        .post("/users/register/")
                        .then()
                        .spec(errorUserNameRegistrationResponseSpec)
                        .extract()
                        .as(UsernamePasswordValidationErrorResponseModel.class)
        );
        step("проверка ответа метода", () ->
                assertEquals(EXPECTED_ERROR_EXISTING_USER, responseModel2.username().get(0)));
    }
}

