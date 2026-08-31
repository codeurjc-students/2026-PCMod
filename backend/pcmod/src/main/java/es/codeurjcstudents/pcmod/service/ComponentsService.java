package es.codeurjcstudents.pcmod.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import es.codeurjcstudents.pcmod.dto.ComponentDTO;
import es.codeurjcstudents.pcmod.dto.ComponentMapper;
import es.codeurjcstudents.pcmod.model.Component;
import es.codeurjcstudents.pcmod.repository.ComponentsRepository;

@Service
public class ComponentsService {

  private final ComponentsRepository componentRepository;

  @Autowired
  private ComponentMapper componentMapper;

  public ComponentsService(ComponentsRepository componentRepository) {
    this.componentRepository = componentRepository;
  }

  public Page<Component> findAll(Pageable pageable) {
    return componentRepository.findAll(pageable);
  }

  public ComponentDTO toDTO(Component component) {
    return componentMapper.toDTO(component);
  }

  public List<Component> findTop3MoreRecent() {
    return componentRepository.findTop3ByOrderByIdDesc();
  }
}
