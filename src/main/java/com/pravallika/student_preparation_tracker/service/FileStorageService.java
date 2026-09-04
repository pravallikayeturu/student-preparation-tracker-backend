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
    // =====================================================

    private static final String S3_ENDPOINT =
            "https://yipzkzilvrfpsucjnjhe.storage.supabase.co/storage/v1/s3";

    private static final String S3_REGION =
            "ap-southeast-2";

    private static final String ACCESS_KEY =
            "68c5237b997e7f822f5fc7cf0adbd7c7";

    private static final String SECRET_KEY =
            "cf3253e73c4aff2a2b88bfc122f444d4303887f7dd2effe1d07216fb496e05b2";

    private static final String BUCKET =
            "study_files";


    private S3Client s3Client;


    // =====================================================
    // CREATE S3 CLIENT
    // =====================================================

    private S3Client getS3Client() {

        if (s3Client == null) {

            AwsBasicCredentials credentials =
                    AwsBasicCredentials.create(
                            ACCESS_KEY,
                            SECRET_KEY
                    );

            s3Client =
                    S3Client.builder()
                            .region(
                                    Region.of(S3_REGION)
                            )
                            .endpointOverride(
                                    URI.create(S3_ENDPOINT)
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
                            + BUCKET
            );

            System.out.println(
                    "Supabase S3 endpoint: "
                            + S3_ENDPOINT
            );


            // -------------------------------------------------
            // CREATE PUT REQUEST
            // -------------------------------------------------

            PutObjectRequest request =
                    PutObjectRequest.builder()
                            .bucket(BUCKET)
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
                            .bucket(BUCKET)
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
                            .bucket(BUCKET)
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