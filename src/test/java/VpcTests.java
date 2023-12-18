import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.cloudx.AwsEc2Client;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InternetGateway;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.Vpc;

public class VpcTests {

  private final AwsEc2Client ec2 = new AwsEc2Client();
  private final SoftAssertions softAssert = new SoftAssertions();
  private final Instance publicInstance = ec2.getInstance("cloudxinfo/PublicInstance/Instance");
  private final String publicInstanceVpcId = publicInstance.vpcId();

  @Test
  void testVpc() {
    Vpc vpc = ec2.getVpc(publicInstanceVpcId);
    List<Subnet> subnets = ec2.getSubnets(publicInstanceVpcId);

    softAssert.assertThat(vpc.isDefault()).isFalse();
    softAssert.assertThat(vpc.cidrBlock()).isEqualTo("10.0.0.0/16");
    softAssert.assertThat(
            vpc.tags().stream().filter(t -> t.key().equals("Name")).findAny().get().value())
        .isEqualTo("cloudxinfo/Network/Vpc");
    softAssert.assertThat(subnets.stream()
        .filter(s -> s.tags().contains(Tag.builder().key("aws-cdk:subnet-type").value("Private").build()) || s.tags()
            .contains(Tag.builder().key("aws-cdk:subnet-type").value("Public").build())).count()).isEqualTo(2);
    softAssert.assertAll();
  }

  @Test
  void testNetworkConfig() {
    Instance privateInstance = ec2.getInstance("cloudxinfo/PrivateInstance/Instance");
    InternetGateway internetGateway = ec2.getInternetGateway(publicInstanceVpcId);
    NatGateway natGateway = ec2.getNatGateway(publicInstanceVpcId);

    softAssert.assertThat(privateInstance.hasNetworkInterfaces()).isTrue();
    softAssert.assertThat(publicInstanceVpcId).isEqualTo(privateInstance.vpcId());
    softAssert.assertThat(internetGateway.attachments().get(0).stateAsString()).isEqualTo("available");
    softAssert.assertThat(natGateway.natGatewayAddresses().get(0).publicIp()).isNotNull();
    softAssert.assertAll();
    assertNull(privateInstance.publicIpAddress(), "Private instance has a public ip address");
  }
}
