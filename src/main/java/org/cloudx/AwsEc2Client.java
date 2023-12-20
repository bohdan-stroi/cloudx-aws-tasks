package org.cloudx;

import java.util.List;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.DescribeInternetGatewaysRequest;
import software.amazon.awssdk.services.ec2.model.DescribeNatGatewaysRequest;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsRequest;
import software.amazon.awssdk.services.ec2.model.DescribeVpcsRequest;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.ec2.model.Image;
import software.amazon.awssdk.services.ec2.model.Instance;
import software.amazon.awssdk.services.ec2.model.InstanceStateName;
import software.amazon.awssdk.services.ec2.model.InternetGateway;
import software.amazon.awssdk.services.ec2.model.NatGateway;
import software.amazon.awssdk.services.ec2.model.SecurityGroup;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.ec2.model.Tag;
import software.amazon.awssdk.services.ec2.model.Vpc;

public class AwsEc2Client {

  private final Ec2Client ec2 = Ec2Client.builder().build();

  public Instance getInstance(String nameTag) {
    return ec2.describeInstances().reservations().stream()
        .map(r -> r.instances().get(0))
        .filter(i -> i.tags().contains(Tag.builder().key("Name").value(nameTag).build()) && i.state().name().equals(InstanceStateName.RUNNING))
        .findAny()
        .get();
  }

  public Image getImage(String imageId) {
    return ec2.describeImages().images().stream()
        .filter(i -> i.imageId().equals(imageId))
        .findAny()
        .get();
  }

  public SecurityGroup getSecurityGroup(Instance instance) {
    String securityGroupdId = instance.securityGroups().get(0).groupId();
    return ec2.describeSecurityGroups().securityGroups().stream()
        .filter(sg -> sg.groupId().equals(securityGroupdId))
        .findAny()
        .get();
  }

  public SecurityGroup getSecurityGroup(String secGrpName) {
    return ec2.describeSecurityGroups().securityGroups().stream()
        .filter(sg -> sg.groupName().contains(secGrpName))
        .findAny()
        .get();
  }

  public Vpc getVpc(String vpcId) {
    DescribeVpcsRequest request = DescribeVpcsRequest.builder().vpcIds(vpcId).build();
    return ec2.describeVpcs(request).vpcs().get(0);
  }

  public List<Subnet> getSubnets(String vpcId) {
    DescribeSubnetsRequest request = DescribeSubnetsRequest.builder()
        .filters(Filter.builder().name("vpc-id").values(vpcId).build()).build();
    return ec2.describeSubnets(request).subnets();
  }

  public NatGateway getNatGateway(String vpcId) {
    DescribeNatGatewaysRequest request = DescribeNatGatewaysRequest.builder()
        .filter(Filter.builder().name("vpc-id").values(vpcId).build()).build();
    return ec2.describeNatGateways(request).natGateways().get(0);
  }

  public InternetGateway getInternetGateway(String vpcId) {
    DescribeInternetGatewaysRequest request = DescribeInternetGatewaysRequest.builder()
        .filters(Filter.builder().name("attachment.vpc-id").values(vpcId).build()).build();
    return ec2.describeInternetGateways(request).internetGateways().get(0);
  }
}