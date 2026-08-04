package es.codeurjcstudents.pcmod.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.codeurjcstudents.pcmod.dto.ComponentDTO;
import es.codeurjcstudents.pcmod.service.ComponentsService;

@RestController
@RequestMapping("/api/v1/components")
public class ComponentsRestController {

  @Autowired
  private ComponentsService componentService;

  @GetMapping("/")
  public Page<ComponentDTO> findAll(Pageable pageable) {
    return componentService.findAll(pageable).map(component -> componentService.toDTO(component));
  }

}
