package com.pravallika.student_preparation_tracker.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_files")
public class UserFile {

    // =====================================================
    // ID
    // =====================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // =====================================================
    // FILE NAME
    // =====================================================

    private String fileName;


    // =====================================================
    // FILE TYPE
    // =====================================================

    private String fileType;


    // =====================================================
    // FILE SIZE
    // =====================================================

    private Long fileSize;


    // =====================================================
    // FILE PATH
    // =====================================================

    private String filePath;


    // =====================================================
    // USER EMAIL
    // =====================================================

    private String userEmail;


    // =====================================================
    // UPLOADED DATE AND TIME
    // =====================================================

    private LocalDateTime uploadedAt;


    // =====================================================
    // DEFAULT CONSTRUCTOR
    // =====================================================

    public UserFile() {
    }


    // =====================================================
    // GETTERS AND SETTERS
    // =====================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }


    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }


    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }


    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}