package es.codeurjcstudents.pcmod.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import java.io.IOException;
import java.net.URI;
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
import es.codeurjcstudents.pcmod.dto.ImageDTO;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.Image;
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

  @PostMapping("/")
  public ResponseEntity<ComponentDTO> createComponent(@RequestBody ComponentDTO componentDTO) {

      Component component = componentService.toDomain(componentDTO);
      componentService.save(component);

      URI location = fromCurrentRequest().path("/{id}").buildAndExpand(component.getId()).toUri();

      return ResponseEntity.created(location).body(componentService.toDTO(component));
  }

  @PostMapping("/{id}/image")
  public ResponseEntity<ImageDTO> addComponentImage(@PathVariable long id, @RequestParam MultipartFile imageFile)
      throws IOException {

    Image image = componentService.addComponentImage(id, imageFile);

    URI location = fromCurrentRequest().buildAndExpand(id).toUri();

    return ResponseEntity.created(location).body(new ImageDTO(image.getId()));
  }

  @DeleteMapping("/{id}")
  public void deleteComponent(@PathVariable long id) {

    componentService.delete(id);

  }

}
