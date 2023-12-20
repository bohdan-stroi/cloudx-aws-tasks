import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import org.cloudx.AwsEc2Client;
import org.cloudx.AwsS3Client;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.model.Bucket;

public class S3Tests {

  private static final AwsS3Client s3 = new AwsS3Client();


  private final Bucket bucket = s3.getBucket("cloudximage-imagestorebucket");
  private final String bucketName = bucket.name();

  @Test
  void testBucket() {
    assertFalse(s3.getBucketPolicyStatus(bucketName), "The bucket " + bucketName + " has public access");
    assertEquals(1,
        s3.getBucketTags(bucketName).stream().filter(t -> t.key().equals("cloudx")).count());
    assertNull(s3.getBucketVersioning(bucketName));
  }

  @Test
  void testApp() throws IOException {
    final AwsEc2Client ec2 = new AwsEc2Client();
    final RequestSpecification reqSpec = RestAssured.given().baseUri(
            "http://" + ec2.getInstance("cloudximage/AppInstance/Instance").publicIpAddress() + "/")
        .basePath("api/image").log().all();
    final ResponseSpecification respSpec = RestAssured.expect().statusCode(200).log().all();
//upload
    Response uploadResponse = reqSpec.multiPart("upfile", new File("./src/test/resources/img.JPG"))
        .post();
    uploadResponse.then().spec(respSpec);
    assertNotNull(uploadResponse.getBody().jsonPath().get("id"));
    int objectId = uploadResponse.getBody().jsonPath().get("id");
//download
    Response downloadResponse = reqSpec.get("file/" + objectId);
    downloadResponse.then().spec(respSpec);
    Files.write(new File("./src/test/resources/downloadedImg" + objectId + ".JPG").toPath(), downloadResponse.getBody().asByteArray());
//list
    Response listResponse = reqSpec.get();
    listResponse.then().spec(respSpec);
    List<LinkedHashMap> objects = listResponse.getBody().jsonPath().get();
    assertTrue(objects.stream().filter(o -> o.get("id").equals(objectId)).count() == 1);
//delete
    Response deleteResponse = reqSpec.delete("/" + objectId);
    deleteResponse.then().spec(respSpec);
  }

}
