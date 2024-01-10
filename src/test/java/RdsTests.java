import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.io.File;
import org.cloudx.AwsEc2Client;
import org.junit.jupiter.api.Test;

public class RdsTests {

  @Test
  void testDeleteImage() {
    final AwsEc2Client ec2 = new AwsEc2Client();
    final RequestSpecification reqSpec = RestAssured.given().baseUri(
            "http://" + ec2.getInstance("cloudxserverless/AppInstance/Instance").publicIpAddress() + "/")
        .basePath("api/image").log().all();
    final ResponseSpecification respSpec = RestAssured.expect().statusCode(200).log().all();
//upload
    Response uploadResponse = reqSpec.multiPart("upfile", new File("./src/test/resources/img.JPG"))
        .post();
    uploadResponse.then().spec(respSpec);
    assertNotNull(uploadResponse.getBody().jsonPath().get("id"));
    String objectId = uploadResponse.getBody().jsonPath().get("id");
//delete
    Response deleteResponse = reqSpec.delete("/" + objectId);
    deleteResponse.then().spec(respSpec);
//get
    Response getMetadataResponse = reqSpec.get("/" + objectId);
    assertEquals(404, getMetadataResponse.getStatusCode(), "Expected image metadata to be deleted, but was: " + getMetadataResponse.getBody().jsonPath().get());
    getMetadataResponse.then().statusCode(404);
  }
}
