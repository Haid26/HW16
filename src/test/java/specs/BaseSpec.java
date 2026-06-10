package specs;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.with;
import static io.restassured.http.ContentType.JSON;

public class BaseSpec {

    public static RequestSpecification baseRequestSpec = with()
            .log().all()
            .contentType(JSON)
            .basePath("/api/v1");

    public static RequestSpecification baseRequestNoContentTypeSpec = with()
            .log().all()
            .basePath("/api/v1");



}
