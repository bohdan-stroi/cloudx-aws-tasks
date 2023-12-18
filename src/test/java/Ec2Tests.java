import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.cloudx.AwsEc2Client;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.services.ec2.model.GroupIdentifier;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceType;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;

public class Ec2Tests {
  private final AwsEc2Client ec2 = new AwsEc2Client();
  private final SoftAssertions softAssert = new SoftAssertions();

  @ParameterizedTest
  @MethodSource({"DataProviders#provideInstanceNameTags"})
  void testInstanceConfig(String instanceNameTag, String securityGroupMark) {
    Instance instance = ec2.getInstance(instanceNameTag);
    Image image = ec2.getImage(instance.imageId());
    List<GroupIdentifier> instanceSecGrps = instance.securityGroups();

    softAssert.assertThat(instance.instanceType()).isEqualTo(InstanceType.fromValue("t2.micro"));
    softAssert.assertThat(instanceSecGrps).isNotEmpty();
    if (instanceSecGrps.size() > 0) {
      softAssert
          .assertThat(instance.securityGroups().get(0).groupName())
          .contains(securityGroupMark);
    }
    if (instanceNameTag.contains("Public"))
      softAssert.assertThat(instance.publicIpAddress()).isNotNull();
    if (instanceNameTag.contains("Private"))
      softAssert.assertThat(instance.publicIpAddress()).isNull();
    softAssert
        .assertThat(
            instance.tags().stream()
                .filter(t -> t.key().equals("Name") || t.key().equals("cloudx"))
                .count())
        .isEqualTo(2);
    softAssert.assertThat(image.description()).contains("Amazon Linux 2");
    softAssert.assertThat(image.blockDeviceMappings().get(0).ebs().volumeSize()).isEqualTo(8);
    softAssert.assertAll();
  }

  @Test
  void testPublicSecurityGroupConfig() {
    SecurityGroup publicSecGrp = ec2.getSecurityGroup("Public");

    softAssert.assertThat(publicSecGrp.ipPermissions()).hasSize(2);
    softAssert.assertThat(publicSecGrp.ipPermissions().get(0).fromPort()).isEqualTo(80);
    softAssert
        .assertThat(publicSecGrp.ipPermissions().get(0).ipRanges().get(0).cidrIp())
        .isEqualTo("0.0.0.0/0");
    softAssert.assertThat(publicSecGrp.ipPermissions().get(1).fromPort()).isEqualTo(22);
    softAssert
        .assertThat(publicSecGrp.ipPermissions().get(1).ipRanges().get(0).cidrIp())
        .isEqualTo("0.0.0.0/0");
    softAssert
        .assertThat(publicSecGrp.ipPermissionsEgress().get(0).ipRanges().get(0).cidrIp())
        .isEqualTo("0.0.0.0/0");
    softAssert.assertAll();
  }

  @Test
  void testPrivateSecurityGroupConfig() {
    SecurityGroup privateSecGrp = ec2.getSecurityGroup("Private");
    String publicSecGrpId = ec2.getSecurityGroup("Public").groupId();

    softAssert.assertThat(privateSecGrp.ipPermissions()).hasSize(2);
    softAssert.assertThat(privateSecGrp.ipPermissions().get(0).fromPort()).isEqualTo(80);
    softAssert.assertThat(privateSecGrp.ipPermissions().get(0).ipRanges()).isEmpty();
    softAssert.assertThat(privateSecGrp.ipPermissions().get(0).ipv6Ranges()).isEmpty();
    softAssert
        .assertThat(privateSecGrp.ipPermissions().get(0).userIdGroupPairs().get(0).groupId())
        .isEqualTo(publicSecGrpId);
    softAssert.assertThat(privateSecGrp.ipPermissions().get(1).fromPort()).isEqualTo(22);
    softAssert.assertThat(privateSecGrp.ipPermissions().get(1).ipRanges()).isEmpty();
    softAssert.assertThat(privateSecGrp.ipPermissions().get(1).ipv6Ranges()).isEmpty();
    softAssert
        .assertThat(privateSecGrp.ipPermissions().get(1).userIdGroupPairs().get(0).groupId())
        .isEqualTo(publicSecGrpId);
    softAssert
        .assertThat(privateSecGrp.ipPermissionsEgress().get(0).ipRanges().get(0).cidrIp())
        .isEqualTo("0.0.0.0/0");
    softAssert.assertAll();
  }

  @Test
  void testApplication() {
    Instance publicInstance = ec2.getInstance("cloudxinfo/PublicInstance/Instance");

    assertNotNull(publicInstance.publicIpAddress(), "Public IP address is not assigned");
    JsonPath actualResponse =
        RestAssured.given()
            .get("http://" + publicInstance.publicIpAddress() + "/")
            .getBody()
            .jsonPath();
    softAssert.assertThat((String) actualResponse.get("region")).isEqualTo("eu-central-1");
    softAssert
        .assertThat((String) actualResponse.get("availability_zone"))
        .isEqualTo(publicInstance.placement().availabilityZone());
    softAssert
        .assertThat((String) actualResponse.get("private_ipv4"))
        .isEqualTo(publicInstance.privateIpAddress());
    softAssert.assertAll();
  }
}
