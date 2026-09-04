
package com.pravallika.student_preparation_tracker.controller;

import com.pravallika.student_preparation_tracker.entity.UserFile;
import com.pravallika.student_preparation_tracker.repository.UserFileRepository;
import com.pravallika.student_preparation_tracker.service.FileStorageService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:5173")
public class FileController {

    private final UserFileRepository userFileRepository;

    private final FileStorageService fileStorageService;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FileController(
            UserFileRepository userFileRepository,
            FileStorageService fileStorageService) {

        this.userFileRepository = userFileRepository;

        this.fileStorageService = fileStorageService;
    }


    // =====================================================
    // UPLOAD FILE
    // =====================================================

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {

            // =================================================
            // CHECK LOGIN
            // =================================================

            if (authentication == null) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("User is not authenticated.");
            }


            // =================================================
            // CHECK FILE
            // =================================================

            if (file == null || file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Please select a file.");
            }


            // =================================================
            // GET LOGGED-IN USER
            // =================================================

            String userEmail =
                    authentication.getName();


            // =================================================
            // GET ORIGINAL FILE NAME
            // =================================================

            String originalFileName =
                    file.getOriginalFilename();

            if (originalFileName == null ||
                    originalFileName.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Invalid file name.");
            }


            // =================================================
            // STORE FILE IN SUPABASE
            // =================================================

            String filePath =
                    fileStorageService.storeFile(file);


            // =================================================
            // SAVE FILE INFORMATION IN DATABASE
            // =================================================

            UserFile userFile =
                    new UserFile();

            userFile.setFileName(
                    originalFileName
            );

            userFile.setFileType(
                    file.getContentType()
            );

            userFile.setFileSize(
                    file.getSize()
            );

            userFile.setFilePath(
                    filePath
            );

            userFile.setUserEmail(
                    userEmail
            );

            userFile.setUploadedAt(
                    java.time.LocalDateTime.now()
            );


            UserFile savedFile =
                    userFileRepository.save(userFile);


            // =================================================
            // RESPONSE
            // =================================================

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(savedFile);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Failed to upload file: "
                                    + e.getMessage()
                    );
        }
    }


    // =====================================================
    // GET MY FILES
    // =====================================================

    @GetMapping
    public ResponseEntity<?> getMyFiles(
            Authentication authentication) {

        if (authentication == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("User is not authenticated.");
        }


        String userEmail =
                authentication.getName();


        List<UserFile> files =
                userFileRepository.findByUserEmail(
                        userEmail
                );


        return ResponseEntity.ok(files);
    }


    // =====================================================
    // VIEW FILE
    // =====================================================

    @GetMapping("/view/{id}")
    public ResponseEntity<?> viewFile(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            // =================================================
            // CHECK LOGIN
            // =================================================

            if (authentication == null) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("User is not authenticated.");
            }


            String userEmail =
                    authentication.getName();


            // =================================================
            // SECURITY CHECK
            // =================================================

            UserFile userFile =
                    userFileRepository
                            .findByIdAndUserEmail(
                                    id,
                                    userEmail
                            )
                            .orElse(null);


            if (userFile == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("File not found.");
            }


            // =================================================
            // LOAD FILE FROM SUPABASE
            // =================================================

            byte[] fileBytes =
                    fileStorageService.loadFile(
                            userFile.getFilePath()
                    );


            // =================================================
            // CONTENT TYPE
            // =================================================

            String contentType =
                    userFile.getFileType();

            if (contentType == null ||
                    contentType.isBlank()) {

                contentType =
                        "application/octet-stream";
            }


            // =================================================
            // RETURN FILE FOR BROWSER VIEW
            // =================================================

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    contentType
                            )
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\""
                                    + userFile.getFileName()
                                    + "\""
                    )
                    .body(fileBytes);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Failed to view file."
                    );
        }
    }


    // =====================================================
    // DOWNLOAD FILE
    // =====================================================

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadFile(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            // =================================================
            // CHECK LOGIN
            // =================================================

            if (authentication == null) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("User is not authenticated.");
            }


            String userEmail =
                    authentication.getName();


            // =================================================
            // SECURITY CHECK
            // =================================================

            UserFile userFile =
                    userFileRepository
                            .findByIdAndUserEmail(
                                    id,
                                    userEmail
                            )
                            .orElse(null);


            if (userFile == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("File not found.");
            }


            // =================================================
            // LOAD FILE FROM SUPABASE
            // =================================================

            byte[] fileBytes =
                    fileStorageService.loadFile(
                            userFile.getFilePath()
                    );


            // =================================================
            // CONTENT TYPE
            // =================================================

            String contentType =
                    userFile.getFileType();

            if (contentType == null ||
                    contentType.isBlank()) {

                contentType =
                        "application/octet-stream";
            }


            // =================================================
            // RETURN FILE FOR DOWNLOAD
            // =================================================

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    contentType
                            )
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + userFile.getFileName()
                                    + "\""
                    )
                    .body(fileBytes);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Failed to download file."
                    );
        }
    }


    // =====================================================
    // DELETE FILE
    // =====================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long id,
            Authentication authentication) {

        try {

            // =================================================
            // CHECK LOGIN
            // =================================================

            if (authentication == null) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("User is not authenticated.");
            }


            String userEmail =
                    authentication.getName();


            // =================================================
            // SECURITY CHECK
            // =================================================

            UserFile userFile =
                    userFileRepository
                            .findByIdAndUserEmail(
                                    id,
                                    userEmail
                            )
                            .orElse(null);


            if (userFile == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("File not found.");
            }


            // =================================================
            // DELETE FILE FROM SUPABASE
            // =================================================

            fileStorageService.deleteFile(
                    userFile.getFilePath()
            );


            // =================================================
            // DELETE DATABASE RECORD
            // =================================================

            userFileRepository.delete(
                    userFile
            );


            return ResponseEntity.ok(
                    "File deleted successfully."
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Failed to delete file."
                    );
        }
    }
}
