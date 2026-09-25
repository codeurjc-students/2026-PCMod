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

    List<String> errors = validateUser(user, rawPassword);

    if (!errors.isEmpty()) {
      throw new IllegalArgumentException("Validation failed: " + errors);
    }

    User userToSave = new User(user.getName(), user.getSurname(), user.getUsername(), user.getAddress(),
        user.getEmail(), passwordEncoder.encode(rawPassword), "REGISTERED_USER");

    return userRepository.save(userToSave);
  }

  public List<String> validateUser(User user, String rawPassword) {

    List<String> errors = new ArrayList<>();

    if (user.getName() == null || user.getName().isEmpty()) {
      errors.add("Name is required");
    }

    if (user.getSurname() == null || user.getSurname().isEmpty()) {
      errors.add("Surname is required");
    }

    if (user.getUsername() == null || user.getUsername().isEmpty()) {
      errors.add("Username is required");
    } else if (userRepository.existsByUsername(user.getUsername())) {
      errors.add("Username already exists");
    }

    if (user.getEmail() == null || user.getEmail().isEmpty()) {
      errors.add("Email is required");
    } else if (userRepository.existsByEmail(user.getEmail())) {
      errors.add("Email already exists");
    } else if (!user.getEmail().matches(EMAIL_REGEX)) {
      errors.add("Email format is invalid. It should be in the format: example@domain.com");
    }

    if (rawPassword == null || rawPassword.isEmpty()) {
      errors.add("Password is required");
    } else if (!rawPassword.matches(PASS_REGEX)) {
      errors.add(
          "Password must contain at least 8 characters including 1 uppercase letter, 1 lowercase letter, 1 digit, and 1 special character (@$!%*?&-_)");
    }

    return errors;
  }

  @PreAuthorize("hasRole('ADMIN') or principal.username == #id.toString()")
  public Image addUserImage(long id, MultipartFile imageFile) throws IOException {

    imagesService.validate(imageFile);

    User user = userRepository.findById(id).orElseThrow();
    Image image = imagesService.createImage(imageFile.getInputStream());

    user.setImage(image);

    return userRepository.save(user).getImage();
  }

  public UserDTO toDTO(User user) {
    return userMapper.toDTO(user);
  }

  public User toDomainFromFullDTO(UserFullDTO userFullDTO) {
    return userMapper.toDomainFromFullDTO(userFullDTO);
  }

}