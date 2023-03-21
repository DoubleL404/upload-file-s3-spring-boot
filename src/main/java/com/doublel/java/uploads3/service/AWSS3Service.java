package com.doublel.java.uploads3.service;

import com.doublel.java.uploads3.enums.ImageTypeSupportedEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketAlreadyExistsException;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.waiters.S3Waiter;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

@Component
@Slf4j
public class AWSS3Service {

    private static final String TAG = "AWSS3Service | ";

    private S3Client s3Client;

    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Value("${aws.s3.key.path.one}")
    private String keyPathOne;

    @Value("${aws.s3.key.path.two}")
    private String keyPathTwo;

    @Value("${aws.s3.key.path.three}")
    private String keyPathThree;

    public AWSS3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void createBucket(String bucketName) {
        if (isBucketExist(bucketName)) {
            return;
        }
        try {
            S3Waiter s3Waiter = s3Client.waiter();

            CreateBucketRequest bucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.createBucket(bucketRequest);

            HeadBucketRequest bucketRequestWait = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            // Wait until the bucket is created and print out the response.
            s3Waiter.waitUntilBucketExists(bucketRequestWait);
            log.info(TAG + "Bucket is ready");
        } catch (S3Exception ex) {
            log.error(TAG + "Create Bucket Error | {}", ex.awsErrorDetails().errorMessage());
        }
    }

    /**
     * Check Bucket existence
     * @param bucketName
     * @return boolean
     */
    public boolean isBucketExist(String bucketName) {
        try {
            HeadBucketRequest request = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.headBucket(request);
            log.info(TAG + "S3 BUCKET | S3 Bucket is existed!");
            return true;
        } catch (BucketAlreadyExistsException e) {
            return false;
        } catch (S3Exception e) {
            log.error(TAG + "S3 BUCKET | Error when checking S3 Bucket existence: {}", e.getMessage());
            return false;
        }

    }

    public String uploadFileToBucket(MultipartFile multipartFile) {
       if (Objects.isNull(multipartFile)) {
           throw new RuntimeException("S3 UPLOAD | Invalid data found in request");
       }
        try {
            log.info(TAG + "S3 UPLOAD | String upload file to S3, original file name: {}", multipartFile.getOriginalFilename());

            String fileName = LocalDateTime.now(ZoneOffset.UTC).toEpochSecond(ZoneOffset.UTC) + "-" + multipartFile.getOriginalFilename();
            boolean validExtension = ImageTypeSupportedEnum.isValidExtensionFile(fileName);
            if (!validExtension) {
                throw new RuntimeException(MessageFormat.format(
                        "S3 UPLOAD | Image file extension is not supported. File name: {0}. Supported image extensions: {1}",
                        multipartFile.getOriginalFilename(),
                        ImageTypeSupportedEnum.extensionsToDelimitedString(",")));
            }

            // Create Bucket
            createBucket(bucketName);

            byte[] bytesFile = multipartFile.getBytes();
            String fileExtension = StringUtils.substring(fileName, StringUtils.lastIndexOf(fileName, '.') + 1);
            String contentType = getContentTypeByExtension(fileExtension);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyPathOne + "/" + fileName)
                    .contentType(contentType)
                    .contentLength((long) bytesFile.length)
                    .build();

            PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(bytesFile));

            return response.eTag();
        } catch (Exception e) {
            log.error(TAG + "S3 UPLOAD | failed to upload file to S3, error: {}", e.getMessage());
        }

        return StringUtils.EMPTY;
    }

    public String getContentTypeByExtension(String extensionFile) {
        Optional<ImageTypeSupportedEnum> imageTypeSupportedEnum = ImageTypeSupportedEnum.findByExtension(extensionFile);

        if (imageTypeSupportedEnum.isPresent()) {
            return imageTypeSupportedEnum.get().getContentType();
        }
        return StringUtils.EMPTY;
    }

}
