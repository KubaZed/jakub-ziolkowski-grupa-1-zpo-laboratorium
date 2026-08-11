package com.project.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import com.project.model.Projekt;
import com.project.model.Zadanie;
import com.project.service.ProjektService;
import com.project.service.ZadanieService;

@Controller
public class ZadanieController {

    private final ZadanieService zadanieService;
    private final ProjektService projektService;

    public ZadanieController(ZadanieService zadanieService, ProjektService projektService) {
        this.zadanieService = zadanieService;
        this.projektService = projektService;
    }

    @GetMapping("/zadanieList")
    public String zadanieList(Model model, 
                               @RequestParam(name = "page", defaultValue = "0") int page,
                               @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        model.addAttribute("zadania", zadanieService.getZadania(pageable).getContent());
        return "zadanieList";
    }

    @GetMapping("/zadanieEdit")
    public String zadanieEdit(@RequestParam(name="zadanieId", required=false) Integer zadanieId, Model model) {
        if (zadanieId != null) {
            model.addAttribute("zadanie", zadanieService.getZadanie(zadanieId).orElse(new Zadanie()));
        } else {
            Zadanie zadanie = new Zadanie();
            zadanie.setDataczasDodania(LocalDateTime.now());
            model.addAttribute("zadanie", zadanie);
        }
        model.addAttribute("v_projekty", projektService.getProjekty(PageRequest.of(0, 100)).getContent());
        return "zadanieEdit";
    }

    @PostMapping(path = "/zadanieEdit")
    public String zadanieEditSave(@ModelAttribute @Valid Zadanie zadanie,
                                  BindingResult bindingResult,
                                  @RequestParam(name="selectedProjektId", required=false) Integer selectedProjektId,
                                  Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("v_projekty", projektService.getProjekty(PageRequest.of(0, 100)).getContent());
            return "zadanieEdit";
        }

        if (selectedProjektId != null) {
            projektService.getProjekt(selectedProjektId).ifPresent(zadanie::setProjekt);
        }

        try {
            zadanieService.setZadanie(zadanie);
        } catch (HttpStatusCodeException e) {
            bindingResult.rejectValue("", String.valueOf(e.getStatusCode().value()), e.getStatusCode().toString());
            model.addAttribute("v_projekty", projektService.getProjekty(PageRequest.of(0, 100)).getContent());
            return "zadanieEdit";
        }
        return "redirect:/zadanieList";
    }

    @PostMapping(params="cancel", path = "/zadanieEdit")
    public String zadanieEditCancel() {
        return "redirect:/zadanieList";
    }

    @PostMapping(params="delete", path = "/zadanieEdit")
    public String zadanieEditDelete(@ModelAttribute Zadanie zadanie) {
        zadanieService.deleteZadanie(zadanie.getZadanieId());
        return "redirect:/zadanieList";
    }
}