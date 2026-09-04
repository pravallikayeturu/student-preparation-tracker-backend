package com.pravallika.student_preparation_tracker.repository;

import com.pravallika.student_preparation_tracker.entity.UserFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFileRepository
        extends JpaRepository<UserFile, Long> {

    // =====================================================
    // GET ALL FILES BELONGING TO A USER
    // =====================================================

    List<UserFile> findByUserEmail(String userEmail);


    // =====================================================
    // GET ONE FILE BELONGING TO A USER
    // =====================================================

    Optional<UserFile> findByIdAndUserEmail(
            Long id,
            String userEmail
    );
}