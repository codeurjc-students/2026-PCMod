package es.codeurjcstudents.pcmod.controller;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjcstudents.pcmod.dto.ImageDTO;
import es.codeurjcstudents.pcmod.dto.UserDTO;
import es.codeurjcstudents.pcmod.dto.UserMapper;
import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.service.ImageService;
import es.codeurjcstudents.pcmod.service.UsersService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/users")
public class UsersRestController {

  @Autowired
  UsersService usersService;

  @Autowired
  UserMapper userMapper;

  @Autowired
  ImageService imagesService;

  @GetMapping("/me")
  public UserDTO me(HttpServletRequest request) {

    Principal principal = request.getUserPrincipal();

    if (principal != null) {

      User user = usersService.getUser(Long.parseLong(principal.getName())).orElseThrow();
      return userMapper.toDTO(user);

    } else {

      throw new NoSuchElementException();

    }
  }

  @GetMapping("/{id}/image")
  public ResponseEntity<Resource> getUserImage(@PathVariable long id) throws SQLException {

    Resource imageFile = usersService.getImageFile(id);
    MediaType mediaType = MediaTypeFactory.getMediaType(imageFile).orElse(MediaType.IMAGE_JPEG);

    return ResponseEntity.ok().contentType(mediaType).body(imageFile);
  }

  @PostMapping("/{id}/image")
  public ResponseEntity<ImageDTO> addUserImage(@PathVariable long id, @RequestParam MultipartFile imageFile)
      throws IOException {

    Image image = usersService.addUserImage(id, imageFile);

    URI location = fromCurrentRequest().buildAndExpand(id).toUri();

    return ResponseEntity.created(location).body(new ImageDTO(image.getId()));
  }

}