package es.codeurjcstudents.pcmod.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.codeurjcstudents.pcmod.enums.ComponentType;

@RestController
@RequestMapping("/api/v1/component-types")
public class ComponentsTypeRestController {

  @GetMapping
  public ComponentType[] getComponentTypes() {
    return ComponentType.values();
  }
  
}
