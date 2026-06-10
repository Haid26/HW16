package specs.registraion;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;
import static specs.BaseSpec.baseRequestNoContentTypeSpec;
import static specs.BaseSpec.baseRequestSpec;

public class RegistrationSpec {

    public static RequestSpecification registrationRequestSpec = baseRequestSpec;
    public static RequestSpecification registrationNoContentTypeRequestSpec = baseRequestNoContentTypeSpec;

    public static ResponseSpecification successfulRegistrationResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(201)
            .expectBody(matchesJsonSchemaInClasspath("schemas/registration/successful_registration_response_schema.json"))
            .expectBody("id", notNullValue())
            .expectBody("username", notNullValue())
            .expectBody("remoteAddr", notNullValue())
            .build();
    public static ResponseSpecification error500RegistrationResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(500)
            .build();
    public static ResponseSpecification noContentTypeRegistrationResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(415)
            .expectBody(matchesJsonSchemaInClasspath("basicErrorWithDetailSchema.json"))
            .expectBody("detail",notNullValue())
            .build();
    public static ResponseSpecification errorPasswordRegistrationResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/registration/emptydata_registration_response.json"))
            .expectBody("password",notNullValue())
            .build();
    public static ResponseSpecification errorUserNameRegistrationResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(400)
            .expectBody(matchesJsonSchemaInClasspath("schemas/registration/emptydata_registration_response.json"))
            .expectBody("username",notNullValue())
            .build();
}
