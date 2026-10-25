package es.codeurjcstudents.pcmod.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;

import es.codeurjcstudents.pcmod.dto.ComponentDTO;
import es.codeurjcstudents.pcmod.model.Component;
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

  @GetMapping("/recent")
  public List<ComponentDTO> findTop3MoreRecent() {
    return componentService.findTop3MoreRecent().stream().map(componentService::toDTO).toList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ComponentDTO> getComponent (@PathVariable long id) {

    Component component = componentService.getComponent(id);
    
    return ResponseEntity.ok(componentService.toDTO(component));
  }

  @GetMapping("/{id}/image")
  public ResponseEntity<Resource> getComponentImage(@PathVariable long id) throws SQLException {

    Resource imageFile = componentService.getImageFile(id);
    MediaType mediaType = MediaTypeFactory.getMediaType(imageFile).orElse(MediaType.IMAGE_JPEG);

    return ResponseEntity.ok().contentType(mediaType).body(imageFile);
  }

}
