package com.pravallika.student_preparation_tracker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
public class FileStorageService {

    // =====================================================
    // SUPABASE STORAGE CONFIGURATION
    // Values come from application.properties
    // =====================================================

    @Value("${supabase.s3.endpoint}")
    private String s3Endpoint;

    @Value("${supabase.s3.region}")
    private String s3Region;

    @Value("${supabase.s3.access-key}")
    private String accessKey;

    @Value("${supabase.s3.secret-key}")
    private String secretKey;

    @Value("${supabase.bucket}")
    private String bucket;


    private S3Client s3Client;


    // =====================================================
    // CREATE S3 CLIENT
    // =====================================================

    private S3Client getS3Client() {

        if (s3Client == null) {

            AwsBasicCredentials credentials =
                    AwsBasicCredentials.create(
                            accessKey,
                            secretKey
                    );

            s3Client =
                    S3Client.builder()
                            .region(
                                    Region.of(s3Region)
                            )
                            .endpointOverride(
                                    URI.create(s3Endpoint)
                            )
                            .credentialsProvider(
                                    StaticCredentialsProvider.create(
                                            credentials
                                    )
                            )
                            .forcePathStyle(true)
                            .build();
        }

        return s3Client;
    }


    // =====================================================
    // UPLOAD FILE
    // =====================================================

    public String storeFile(MultipartFile file) {

        try {

            // -------------------------------------------------
            // VALIDATE FILE
            // -------------------------------------------------

            if (file == null || file.isEmpty()) {

                throw new RuntimeException(
                        "Cannot store an empty file."
                );
            }

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null ||
                    originalFileName.trim().isEmpty()) {

                throw new RuntimeException(
                        "Invalid file name."
                );
            }


            // -------------------------------------------------
            // CREATE UNIQUE FILE NAME
            // -------------------------------------------------

            String storedFileName =
                    UUID.randomUUID()
                            + "_"
                            + originalFileName;


            System.out.println(
                    "Uploading file: "
                            + storedFileName
            );

            System.out.println(
                    "Supabase bucket: "
                            + bucket
            );

            System.out.println(
                    "Supabase S3 endpoint: "
                            + s3Endpoint
            );

            System.out.println(
                    "Supabase S3 region: "
                            + s3Region
            );


            // -------------------------------------------------
            // CREATE PUT REQUEST
            // -------------------------------------------------

            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(storedFileName)
                            .contentType(
                                    file.getContentType() != null
                                            ? file.getContentType()
                                            : "application/octet-stream"
                            )
                            .build();


            // -------------------------------------------------
            // UPLOAD
            // -------------------------------------------------

            getS3Client().putObject(
                    request,
                    RequestBody.fromBytes(
                            file.getBytes()
                    )
            );


            System.out.println(
                    "File uploaded successfully: "
                            + storedFileName
            );


            // -------------------------------------------------
            // RETURN STORAGE PATH
            // -------------------------------------------------

            return storedFileName;


        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to read uploaded file.",
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to store file in Supabase: "
                            + e.getMessage(),
                    e
            );
        }
    }


    // =====================================================
    // LOAD FILE
    // =====================================================

    public byte[] loadFile(String filePath) {

        try {

            GetObjectRequest request =
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(filePath)
                            .build();


            ResponseBytes<?> response =
                    getS3Client().getObjectAsBytes(
                            request
                    );


            return response.asByteArray();


        } catch (NoSuchKeyException e) {

            throw new RuntimeException(
                    "File not found in Supabase: "
                            + filePath,
                    e
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to load file from Supabase: "
                            + e.getMessage(),
                    e
            );
        }
    }


    // =====================================================
    // DELETE FILE
    // =====================================================

    public void deleteFile(String filePath) {

        try {

            DeleteObjectRequest request =
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(filePath)
                            .build();


            getS3Client().deleteObject(
                    request
            );


            System.out.println(
                    "File deleted successfully: "
                            + filePath
            );


        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to delete file from Supabase: "
                            + e.getMessage(),
                    e
            );
        }
    }
}