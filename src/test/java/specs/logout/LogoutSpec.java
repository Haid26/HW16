package specs.logout;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.*;
import static specs.BaseSpec.noContentTypeResponseSpec;

public class LogoutSpec {
    public static RequestSpecification logoutRequestSpec = baseRequestSpec;
    public static RequestSpecification logoutNoContentTypeRequestSpec = baseRequestNoContentTypeSpec;

    public static ResponseSpecification error500LogoutResponseSpec = error500ResponseSpec;
    public static ResponseSpecification noContentTypeLogoutResponseSpec = noContentTypeResponseSpec;
    public static ResponseSpecification successfulLogoutResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .build();
    public static ResponseSpecification errorEmptyTokenLogoutResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/logout/error_logout_empty_token_response.json"))
            .expectBody("refresh",notNullValue())
            .build();
    public static ResponseSpecification errorTokenLogoutResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/logout/not_valid_toker_response.json"))
            .expectBody("detail",notNullValue())
            .expectBody("code",notNullValue())
            .build();
}
