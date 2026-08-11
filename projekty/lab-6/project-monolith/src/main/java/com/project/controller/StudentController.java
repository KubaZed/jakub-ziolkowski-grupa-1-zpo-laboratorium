package com.project.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.project.model.Student;
import com.project.service.StudentService;

@Controller
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/studentList")
    public String studentList(Model model, Pageable pageable,
                              @RequestParam(name="nazwisko", required=false) String nazwisko) {
        Page<Student> pageStudents;
        if (nazwisko != null && !nazwisko.trim().isEmpty()) {
            pageStudents = studentService.findByNazwiskoStartsWithIgnoreCase(nazwisko.trim(), pageable);
            model.addAttribute("nazwisko", nazwisko);
        } else {
            pageStudents = studentService.getStudenci(pageable);
        }
        
        model.addAttribute("studenci", pageStudents.getContent());
        model.addAttribute("page", pageStudents);
        return "studentList";
    }

    @GetMapping("/studentEdit")
    public String studentEdit(@RequestParam(name="studentId", required=false) Integer studentId, Model model) {
        if (studentId != null) {
            model.addAttribute("student", studentService.getStudent(studentId).orElse(new Student()));
        } else {
            model.addAttribute("student", new Student());
        }
        return "studentEdit";
    }

    @PostMapping(path = "/studentEdit")
    public String studentEditSave(@ModelAttribute("student") @Valid Student student, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "studentEdit";
        }
        studentService.setStudent(student);
        return "redirect:/studentList";
    }

    @PostMapping(params="cancel", path = "/studentEdit")
    public String studentEditCancel() {
        return "redirect:/studentList";
    }

    @PostMapping(params="delete", path = "/studentEdit")
    public String studentEditDelete(@ModelAttribute("student") Student student) {
        studentService.deleteStudent(student.getStudentId());
        return "redirect:/studentList";
    }
}