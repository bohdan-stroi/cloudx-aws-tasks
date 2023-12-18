import java.util.List;
import java.util.stream.Stream;
import org.cloudx.pojo.Statement;
import org.junit.jupiter.params.provider.Arguments;

public class DataProviders {

  public static Stream<Arguments> providePolicyNamesAndExpectedStatements() {
    return Stream.of(
        Arguments.of("FullAccessPolicyEC2", new Statement("ec2:*", "*", "Allow")),
        Arguments.of("FullAccessPolicyS3", new Statement("s3:*", "*", "Allow")),
        Arguments.of(
            "ReadAccessPolicyS3",
            new Statement(List.of("s3:Describe*", "s3:Get*", "s3:List*"), "*", "Allow")));
  }

  public static Stream<Arguments> provideRolesAttachedPolicies() {
    return Stream.of(
        Arguments.of("FullAccessRoleEC2", "FullAccessPolicyEC2"),
        Arguments.of("FullAccessRoleS3", "FullAccessPolicyS3"),
        Arguments.of("ReadAccessRoleS3", "ReadAccessPolicyS3"));
  }

  public static Stream<Arguments> provideGroupsAttachedPolicies() {
    return Stream.of(
        Arguments.of("FullAccessGroupEC2", "FullAccessPolicyEC2"),
        Arguments.of("FullAccessGroupS3", "FullAccessPolicyS3"),
        Arguments.of("ReadAccessGroupS3", "ReadAccessPolicyS3"));
  }

  public static Stream<Arguments> provideUsersGroups() {
    return Stream.of(
        Arguments.of("FullAccessUserEC2", "FullAccessGroupEC2"),
        Arguments.of("FullAccessUserS3", "FullAccessGroupS3"),
        Arguments.of("ReadAccessUserS3", "ReadAccessGroupS3"));
  }
  public static Stream<Arguments> provideInstanceNameTags() {
    return Stream.of(
        Arguments.of("cloudxinfo/PublicInstance/Instance", "PublicInstanceSecurityGroup"),
        Arguments.of("cloudxinfo/PrivateInstance/Instance", "PrivateInstanceSecurityGroup"));
  }
}
