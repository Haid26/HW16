package specs;

import io.restassured.RestAssured;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.with;
import static io.restassured.filter.log.LogDetail.ALL;
import static io.restassured.http.ContentType.JSON;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.notNullValue;

public class BaseSpec {

    public static RequestSpecification baseRequestSpec = with()
            .log().all()
            .contentType(JSON)
            .basePath("/api/v1");

    public static RequestSpecification baseRequestNoContentTypeSpec = with()
            .log().all()
            .basePath("/api/v1");

    public static ResponseSpecification error500ResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(500)
            .build();
    public static ResponseSpecification noContentTypeResponseSpec = new ResponseSpecBuilder()
            .log(ALL)
            .expectStatusCode(415)
            .expectBody(matchesJsonSchemaInClasspath("schemas/common/basicErrorWithDetailSchema.json"))
            .expectBody("detail",notNullValue())
            .build();

}
