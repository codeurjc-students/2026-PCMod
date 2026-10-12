package es.codeurjcstudents.pcmod.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjcstudents.pcmod.dto.UserDTO;
import es.codeurjcstudents.pcmod.dto.UserFullDTO;
import es.codeurjcstudents.pcmod.dto.UserMapper;
import es.codeurjcstudents.pcmod.model.Image;
import es.codeurjcstudents.pcmod.model.User;
import es.codeurjcstudents.pcmod.repository.UsersRepository;

@Service
public class UsersService {

  private final UserMapper userMapper;
  private final ImageService imagesService;
  private final UsersRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
  private static final String PASS_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&\\-_])[A-Za-z\\d@$!%*?&\\-_]{8,}$";

  public UsersService(UsersRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper,
      ImageService imagesService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userMapper = userMapper;
    this.imagesService = imagesService;
  }

  public User createUser(User user) {
    return userRepository.save(user);
  }

  @PreAuthorize("hasRole('ADMIN') or principal.username == #id.toString()")
  public Optional<User> getUser(long id) {
    return userRepository.findById(id);
  }

  @PreAuthorize("hasRole('ADMIN') or principal.username == #id.toString()")
  public Resource getImageFile(long id) throws SQLException {

    Optional<Image> imageOptional = userRepository.findImageById(id);

    if (imageOptional.isPresent() && imageOptional.get().getImageFile() != null) {
      return new InputStreamResource(imageOptional.get().getImageFile().getBinaryStream());
    } else {
      return new ClassPathResource("/sample_images/empty-profile-image.jpg");
    }
  }

  public User register(User user, String rawPassword) {

    List<String> errors = validateUser(user, rawPassword, null);

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException("Validación fallida:\n• " + String.join("\n• ", errors));
    }

    User userToSave = new User(user.getName(), user.getSurname(), user.getUsername(), user.getAddress(),
        user.getEmail(), passwordEncoder.encode(rawPassword), "REGISTERED_USER");

    return userRepository.save(userToSave);
  }

  @PreAuthorize("hasRole('ADMIN') or principal.username == #id.toString()")
  public Image addUserImage(long id, MultipartFile imageFile) throws IOException {

    imagesService.validate(imageFile);

    User user = userRepository.findById(id).orElseThrow();
    Image image;

    if (user.getImage() == null) {
      image = imagesService.createImage(imageFile.getInputStream());
    } else {
      image = imagesService.replaceImageFile(user.getImage().getId(), imageFile.getInputStream());
    }

    user.setImage(image);

    return userRepository.save(user).getImage();
  }

  @PreAuthorize("hasRole('ADMIN') or principal.username == #id.toString()")
  public void deleteUser(long id) {

    Optional<User> user = userRepository.findById(id);

    if (user.isPresent()) {

      userRepository.deleteById(id);

    }

  }

  @PreAuthorize("hasRole('ADMIN') or principal.username == #id.toString()")
  public User updateUser(User newUserData, String rawPassword, long id) {

    List<String> errors = validateUser(newUserData, rawPassword, id);

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException("Validación fallida:\n• " + String.join("\n• ", errors));
    }

    User user = userRepository.findById(id).orElseThrow();
    user.setName(newUserData.getName());
    user.setSurname(newUserData.getSurname());
    user.setUsername(newUserData.getUsername());
    user.setAddress(newUserData.getAddress());
    user.setEmail(newUserData.getEmail());
    user.setEncodedPassword(passwordEncoder.encode(rawPassword));

    return userRepository.save(user);

  }

  public List<String> validateUser(User user, String rawPassword, Long id) {

    List<String> errors = new ArrayList<>();
    User originalUser = null;

    if (id != null) {
      originalUser = userRepository.findById(id).orElseThrow();
    }

    validateRequiredField(user.getName(), "El nombre es obligatorio.", errors);
    validateRequiredField(user.getSurname(), "El apellido es obligatorio.", errors);
    validateRequiredField(user.getAddress(), "La dirección es obligatoria.", errors);

    if (user.getUsername() == null || user.getUsername().isEmpty()) {
      errors.add("El nombre de usuario es obligatorio.");
    } else {
      validateUsername(user, originalUser, errors);
    }

    if (user.getEmail() == null || user.getEmail().isEmpty()) {
      errors.add("El email es obligatorio.");
    } else {
      validateEmail(user, originalUser, errors);
    }

    if (rawPassword == null || rawPassword.isEmpty()) {
      errors.add("La contraseña es obligatoria.");
    } else {
      validatePassword(rawPassword, errors);
    }

    return errors;

  }

  private void validateRequiredField(String value, String message, List<String> errors) {

    if (value == null || value.isEmpty()) {
      errors.add(message);
    }

  }

  public void validateUsername(User user, User originalUser, List<String> errors) {

    boolean isNewUser = (originalUser == null);
    boolean usernameChanged = !isNewUser && !originalUser.getUsername().equals(user.getUsername());

    if ((isNewUser || usernameChanged) && userRepository.existsByUsername(user.getUsername())) {
      errors.add("El nombre de usuario ya existe.");
    }

  }

  public void validateEmail(User user, User originalUser, List<String> errors) {

    boolean isNewUser = (originalUser == null);
    boolean emailChanged = !isNewUser && !originalUser.getEmail().equals(user.getEmail());

    if ((isNewUser || emailChanged) && userRepository.existsByEmail(user.getEmail())) {
      errors.add("El email ya existe.");
      return;
    }

    if (!user.getEmail().matches(EMAIL_REGEX)) {
      errors.add("El formato del email no es válido. Debe seguir el formato: ejemplo@dominio.com.");
    }

  }

  public void validatePassword(String rawPassword, List<String> errors) {

    if (!rawPassword.matches(PASS_REGEX)) {
      errors.add(
          "La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&-_).");
    }

  }

  public UserDTO toDTO(User user) {
    return userMapper.toDTO(user);
  }

  public User toDomainFromFullDTO(UserFullDTO userFullDTO) {
    return userMapper.toDomainFromFullDTO(userFullDTO);
  }

}