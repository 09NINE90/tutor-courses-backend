package ru.razumoff.courses.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestApi {

    @GetMapping
    public ResponseEntity<String> getAllCourses() {
        return ResponseEntity.ok("Courses Service is working!");
    }
}
