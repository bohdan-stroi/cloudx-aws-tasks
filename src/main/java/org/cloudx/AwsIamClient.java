package org.cloudx;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.cloudx.pojo.Statement;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AttachedPolicy;
import software.amazon.awssdk.services.iam.model.GetPolicyVersionRequest;
import software.amazon.awssdk.services.iam.model.Group;
import software.amazon.awssdk.services.iam.model.ListAttachedGroupPoliciesRequest;
import software.amazon.awssdk.services.iam.model.ListAttachedGroupPoliciesResponse;
import software.amazon.awssdk.services.iam.model.ListAttachedRolePoliciesRequest;
import software.amazon.awssdk.services.iam.model.ListAttachedRolePoliciesResponse;
import software.amazon.awssdk.services.iam.model.ListGroupsForUserRequest;
import software.amazon.awssdk.services.iam.model.Policy;

public class AwsIamClient {
  public static final IamClient iam = IamClient.builder().build();
  private final ObjectMapper objectMapper = new ObjectMapper();

  public Policy getPolicy(String expectedPolicyName) {
    return iam.listPolicies().policies().stream()
        .filter(p -> p.policyName().equals(expectedPolicyName))
        .findAny()
        .get();
  }

  public Statement getPolicyDocStatement(String arn) throws JsonProcessingException {
    GetPolicyVersionRequest request =
        GetPolicyVersionRequest.builder().policyArn(arn).versionId("v1").build();
    String responseDocument = iam.getPolicyVersion(request).policyVersion().document();
    return extractDocumentStatement(responseDocument);
  }

  public Statement getPolicyDocStatement(String arn, String versionId)
      throws JsonProcessingException {
    GetPolicyVersionRequest request =
        GetPolicyVersionRequest.builder().policyArn(arn).versionId(versionId).build();
    String responseDocument = iam.getPolicyVersion(request).policyVersion().document();
    return extractDocumentStatement(responseDocument);
  }

  public List<AttachedPolicy> getAttachedRolePolicies(String role) {
    ListAttachedRolePoliciesRequest request =
        ListAttachedRolePoliciesRequest.builder().roleName(role).build();
    ListAttachedRolePoliciesResponse response = iam.listAttachedRolePolicies(request);
    return response.attachedPolicies();
  }

  public List<AttachedPolicy> getAttachedGroupPolicies(String role) {
    ListAttachedGroupPoliciesRequest request =
        ListAttachedGroupPoliciesRequest.builder().groupName(role).build();
    ListAttachedGroupPoliciesResponse response = iam.listAttachedGroupPolicies(request);
    return response.attachedPolicies();
  }

  public List<Group> getUserGroups(String user) {
    ListGroupsForUserRequest request = ListGroupsForUserRequest.builder().userName(user).build();
    return iam.listGroupsForUser(request).groups();
  }

  private Statement extractDocumentStatement(String document) throws JsonProcessingException {
    ObjectNode documentJson =
        objectMapper.readValue(
            URLDecoder.decode(document, StandardCharsets.UTF_8), ObjectNode.class);
    String statementStr = documentJson.get("Statement").get(0).toString();
    return objectMapper.readValue(statementStr, Statement.class);
  }
}
