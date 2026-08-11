package com.project.controller;

import java.net.URI;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.project.model.Student;
import com.project.service.StudentService;
import org.springdoc.core.annotations.ParameterObject;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api")
@Tag(name = "Student")
public class StudentRestController {

    private static final Logger logger = LoggerFactory.getLogger(StudentRestController.class);

    private final StudentService studentService;

    public StudentRestController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/studenti/{studentId}")
    public ResponseEntity<Student> getStudent(@PathVariable("studentId") Integer studentId) {
        return ResponseEntity.of(studentService.getStudent(studentId));
    }

    @PostMapping(path = "/studenti")
    public ResponseEntity<Void> createStudent(@Valid @RequestBody Student student) {
        Student createdStudent = studentService.setStudent(student);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{studentId}").buildAndExpand(createdStudent.getStudentId()).toUri();
                
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/studenti/{studentId}")
    public ResponseEntity<Void> updateStudent(@Valid @RequestBody Student student, @PathVariable("studentId") Integer studentId) {
        return studentService.getStudent(studentId)
                .map(s -> {
                    studentService.setStudent(student);
                    return new ResponseEntity<Void>(HttpStatus.OK);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/studenti/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable("studentId") Integer studentId) {
        return studentService.getStudent(studentId).map(s -> {
            studentService.deleteStudent(studentId);
            return new ResponseEntity<Void>(HttpStatus.OK);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/studenti")
    public Page<Student> getStudenti(@ParameterObject Pageable pageable) {
        return studentService.getStudenci(pageable);
    }

    @GetMapping(value = "/studenti", params = "nrIndeksu")
    public ResponseEntity<Student> getStudentByNrIndeksu(@RequestParam(name = "nrIndeksu") String nrIndeksu) {
        return ResponseEntity.of(studentService.findByNrIndeksu(nrIndeksu));
    }

    @GetMapping(value = "/studenti", params = "nazwisko")
    public Page<Student> getStudentiByNazwisko(@RequestParam(name = "nazwisko") String nazwisko, @ParameterObject Pageable pageable) {
        return studentService.findByNazwiskoStartsWithIgnoreCase(nazwisko, pageable);
    }
}