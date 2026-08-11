package com.project.controller;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.Set;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import com.project.model.Projekt;
import com.project.model.Student;
import com.project.service.ProjektService;
import com.project.service.StudentService;

@Controller
public class ProjectController {

    private final ProjektService projektService;
    private final StudentService studentService;

    public ProjectController(ProjektService projektService, StudentService studentService) {
        this.projektService = projektService;
        this.studentService = studentService;
    }

    @GetMapping("/projektList")
    public String projektList(Model model, Pageable pageable) {
        model.addAttribute("projekty", projektService.getProjekty(pageable).getContent());
        return "projektList";
    }

    @GetMapping("/projektEdit")
    public String projektEdit(@RequestParam(name="projektId", required=false) Integer projektId, Model model, Pageable pageable) {
        if (projektId != null) {
            model.addAttribute("projekt", projektService.getProjekt(projektId).orElse(new Projekt()));
        } else {
            model.addAttribute("projekt", new Projekt());
        }
        model.addAttribute("v_studenci", studentService.getStudenci(pageable).getContent());
        return "projektEdit";
    }

    @PostMapping(path = "/projektEdit")
    public String projektEditSave(@ModelAttribute @Valid Projekt projekt, 
                                  BindingResult bindingResult,
                                  @RequestParam(name="selectedStudentIds", required=false) Set<Integer> selectedStudentIds,
                                  Model model, Pageable pageable) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("v_studenci", studentService.getStudenci(pageable).getContent());
            return "projektEdit";
        }
        
        if (selectedStudentIds != null && !selectedStudentIds.isEmpty()) {
            Set<Student> studenci = new HashSet<>();
            for (Integer studentId : selectedStudentIds) {
                studentService.getStudent(studentId).ifPresent(studenci::add);
            }
            projekt.setStudenci(studenci);
        }

        try {
            projektService.setProjekt(projekt);
        } catch (HttpStatusCodeException e) {
            bindingResult.rejectValue("", String.valueOf(e.getStatusCode().value()), e.getStatusCode().toString());
            model.addAttribute("v_studenci", studentService.getStudenci(pageable).getContent());
            return "projektEdit";
        }
        return "redirect:/projektList";
    }

    @PostMapping(params="cancel", path = "/projektEdit")
    public String projektEditCancel() {
        return "redirect:/projektList";
    }

    @PostMapping(params="delete", path = "/projektEdit")
    public String projektEditDelete(@ModelAttribute Projekt projekt) {
        projektService.deleteProjekt(projekt.getProjektId());
        return "redirect:/projektList";
    }
}