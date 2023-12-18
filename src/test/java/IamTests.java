import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.stream.Collectors;
import org.cloudx.AwsIamClient;
import org.cloudx.pojo.Statement;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.Group;
import software.amazon.awssdk.services.iam.model.Policy;

public class IamTests {
  AwsIamClient iam = new AwsIamClient();

  @ParameterizedTest
  @MethodSource("DataProviders#providePolicyNamesAndExpectedStatements")
  void testIamPolicyStatement(String policyName, Statement expectedStatement)
      throws JsonProcessingException {
    Policy policy = iam.getPolicy(policyName);
    Statement actualStatement = iam.getPolicyDocStatement(policy.arn(), policy.defaultVersionId());
    assertEquals(expectedStatement, actualStatement);
  }

  @ParameterizedTest
  @MethodSource("DataProviders#provideRolesAttachedPolicies")
  void testIamAttachedRolePoliciesExist(String role, String expectedAttachedPolicyName) {
    List<AttachedPolicy> attachedRolePolicies = iam.getAttachedRolePolicies(role);
    List<String> policyNames =
        attachedRolePolicies.stream().map(AttachedPolicy::policyName).collect(Collectors.toList());
    assertTrue(
        policyNames.contains(expectedAttachedPolicyName),
        String.format("%s does not contain %s", policyNames, expectedAttachedPolicyName));
  }

  @ParameterizedTest
  @MethodSource("DataProviders#provideGroupsAttachedPolicies")
  void testIamAttachedGroupPoliciesExist(String group, String expectedAttachedPolicyName) {
    List<String> policyNames =
        iam.getAttachedGroupPolicies(group).stream()
            .map(AttachedPolicy::policyName)
            .collect(Collectors.toList());
    assertTrue(
        policyNames.contains(expectedAttachedPolicyName),
        String.format("%s does not contain %s", policyNames, expectedAttachedPolicyName));
  }

  @ParameterizedTest
  @MethodSource("DataProviders#provideUsersGroups")
  void testIamUserGroupsExist(String user, String expectedGroupName) {
    List<String> groupNames =
        iam.getUserGroups(user).stream().map(Group::groupName).collect(Collectors.toList());
    assertTrue(
        groupNames.contains(expectedGroupName),
        String.format("%s does not contain %s", groupNames, expectedGroupName));
  }
}
