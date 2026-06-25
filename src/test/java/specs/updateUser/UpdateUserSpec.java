package specs.updateUser;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.*;
import static specs.BaseSpec.noContentTypeResponseSpec;

public record UpdateUserSpec() {
    public static RequestSpecification updateUserRequestSpec = baseRequestSpec;
    public static RequestSpecification updateUserNoContentTypeRequestSpec = baseRequestNoContentTypeSpec;

    public static ResponseSpecification error500UpdateUserResponseSpec = error500ResponseSpec;
    public static ResponseSpecification noContentTypeUpdateUserResponseSpec = noContentTypeResponseSpec;
    public static ResponseSpecification successfulUpdateUserResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/updateUser/successful_user_update_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("username", notNullValue())
            .expectBody("firstName", notNullValue())
            .expectBody("lastName", notNullValue())
            .expectBody("email", notNullValue())
            .expectBody("remoteAddr", notNullValue())
            .build();

}
