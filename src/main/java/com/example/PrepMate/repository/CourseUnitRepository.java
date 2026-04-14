package com.example.PrepMate.repository;

import com.example.PrepMate.model.CourseUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseUnitRepository extends JpaRepository<CourseUnit, Long> {
    // Believe it or not, leaving this empty gives you full access to 
    // save(), findAll(), deleteById(), and more automatically!
}