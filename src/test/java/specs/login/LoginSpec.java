package specs.login;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.*;

public class LoginSpec {
    public static RequestSpecification loginRequestSpec = baseRequestSpec;
    public static RequestSpecification loginNoContentTypeRequestSpec = baseRequestNoContentTypeSpec;

    public static ResponseSpecification error500LoginResponseSpec = error500ResponseSpec;
    public static ResponseSpecification noContentTypeLoginResponseSpec = noContentTypeResponseSpec;
    public static ResponseSpecification successfulLoginResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(200)
            .expectBody(matchesJsonSchemaInClasspath("schemas/login/successful_login_response_schema.json"))
            .expectBody("refresh", notNullValue())
            .expectBody("access", notNullValue())
            .build();
    public static ResponseSpecification errorPasswordLoginResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/common/emptydata_response.json"))
            .expectBody("password",notNullValue())
            .build();
    public static ResponseSpecification errorUserNameLoginResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/common/emptydata_response.json"))
            .expectBody("username",notNullValue())
            .build();
    public static ResponseSpecification errorWrongCredentialsLoginResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(401)
            .expectBody(matchesJsonSchemaInClasspath("schemas/common/basicErrorWithDetailSchema.json"))
            .expectBody("detail", notNullValue())
            .build();
}
