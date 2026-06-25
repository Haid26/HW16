package test;

import models.login.LoginRequestModel;
import models.login.LoginResponseModel;
import models.registration.RegistrationRequestModel;
import models.registration.RegistrationResponseModel;
import models.updateUser.UpdateUserFullRequestModel;
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
import static specs.updateUser.UpdateUserSpec.successfulUpdateUserResponseSpec;
import static specs.updateUser.UpdateUserSpec.updateUserRequestSpec;

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
    public void successfulUpdateUserInfoTest(){
        generateTestData();
        registerUser(username,password);
        loginUser(username,password);

        UpdateUserFullRequestModel updateUserRequest = new UpdateUserFullRequestModel(username,firstName,lastName,email);

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

        assertEquals(id,responseModel.id());
        assertEquals(username,responseModel.username());
        assertEquals(firstName,responseModel.firstName());
        assertEquals(lastName,responseModel.lastName());
        assertEquals(email,responseModel.email());
    }
}
