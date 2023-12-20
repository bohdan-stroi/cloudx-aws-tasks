package org.cloudx;

import java.util.List;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.BucketVersioningStatus;
import software.amazon.awssdk.services.s3.model.GetBucketEncryptionRequest;
import software.amazon.awssdk.services.s3.model.GetBucketEncryptionResponse;
import software.amazon.awssdk.services.s3.model.GetBucketPolicyStatusRequest;
import software.amazon.awssdk.services.s3.model.GetBucketTaggingRequest;
import software.amazon.awssdk.services.s3.model.GetBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.Tag;

public class AwsS3Client {

  private final S3Client s3 = S3Client.builder().build();

  public Bucket getBucket(String namePrefix) {
    return s3.listBuckets().buckets().stream().filter(b -> b.name().startsWith(namePrefix))
        .findAny().get();
  }

  public String getBucketVersioning(String bucketName) {
    BucketVersioningStatus status;
    try {
      status = s3.getBucketVersioning(GetBucketVersioningRequest.builder().bucket(bucketName).build()).status();
    } catch (NullPointerException e) {
      return null;
    }
    return status.toString();
  }

  public Boolean getBucketPolicyStatus(String bucketName) {
    return s3.getBucketPolicyStatus(
            GetBucketPolicyStatusRequest.builder().bucket(bucketName).build()).policyStatus()
        .isPublic();
  }

  public GetBucketEncryptionResponse getBucketEncryption(String bucketName) {
    return s3.getBucketEncryption(GetBucketEncryptionRequest.builder().bucket(bucketName).build());
  }

  public List<Tag> getBucketTags(String bucketName) {
    return s3.getBucketTagging(GetBucketTaggingRequest.builder().bucket(bucketName).build())
        .tagSet();
  }
}
