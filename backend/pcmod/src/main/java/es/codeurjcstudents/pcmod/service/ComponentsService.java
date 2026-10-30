package es.codeurjcstudents.pcmod.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import es.codeurjcstudents.pcmod.dto.ComponentDTO;
import es.codeurjcstudents.pcmod.dto.ComponentMapper;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;

@Service
public class ComponentsService {

  private final ComponentsRepository componentRepository;
  private final ComponentMapper componentMapper;
  private final ImageService imagesService;


  public ComponentsService(ComponentsRepository componentRepository, ComponentMapper componentMapper, ImageService imagesService) {
    this.componentRepository = componentRepository;
    this.componentMapper = componentMapper;
    this.imagesService = imagesService;
  }

  public Page<Component> findAll(Pageable pageable) {
    return componentRepository.findAll(pageable);
  }

  public List<Component> findTop3MoreRecent() {
    return componentRepository.findTop3ByOrderByIdDesc();
  }

  public Component getComponent(long id) {
    return componentRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Componente no encontrado."));
  }

  public Resource getImageFile(long id) throws SQLException {

    Optional<Image> imageOptional = componentRepository.findImageById(id);

    if (imageOptional.isPresent() && imageOptional.get().getImageFile() != null) {
      return new InputStreamResource(imageOptional.get().getImageFile().getBinaryStream());
    }
    return null;

  }

  @PreAuthorize ("hasRole('ADMIN')")
  public Component save(Component component) {

    List<String> errors = validateComponent(component);

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException("Validación fallida:\n• " + String.join("\n• ", errors));
    }

    return componentRepository.save(component);
  }

  @PreAuthorize("hasRole('ADMIN')")
  public Image addComponentImage(long id, MultipartFile imageFile) throws IOException {

    imagesService.validate(imageFile);

    Component component = componentRepository.findById(id).orElseThrow();

    Image image = imagesService.createImage(imageFile.getInputStream());

    component.setImage(image);

    return componentRepository.save(component).getImage();
  }

  @PreAuthorize("hasRole('ADMIN')")
  public void delete(long id) {

    Optional<Component> deletedComponent = componentRepository.findById(id);

    if (deletedComponent.isPresent()) {

      componentRepository.deleteById(id);

    }

  }

  public List<String> validateComponent(Component component) {

    List<String> errors = new ArrayList<>();

    if (component.getName() == null || component.getName().isEmpty()) {
      errors.add("El nombre del componente es obligatorio.");
    } else {
      validateName(component, errors);
    }

    validateRequiredField(component.getDescription(), "La descripción es obligatoria.", errors);
    validateRequiredField(component.getBrand(), "La marca es obligatoria.", errors);
    validateRequiredField(component.getType().toString(), "El tipo es obligatoria.", errors);
    validateRequiredField(component.getPrice().toString(), "El precio es obligatorio", errors);
    validateRequiredField(component.getStock().toString(), "El stock es obligatorio", errors);

    return errors;

  }

  private void validateRequiredField(String value, String message, List<String> errors) {

    if (value == null || value.isEmpty()) {
      errors.add(message);
    }

  }

  public void validateName(Component component, List<String> errors) {

    if (componentRepository.existsByName(component.getName())) {
      errors.add("Ya existe un componente con ese nombre.");
    }

  }

  public ComponentDTO toDTO(Component component) {
      return componentMapper.toDTO(component);
  }

  public Component toDomain(ComponentDTO componentDTO) {
      return componentMapper.toDomain(componentDTO);
  }
}
