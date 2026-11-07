package es.codeurjcstudents.pcmod.model;

import java.time.LocalDate;
import java.time.ZoneId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id = null;

  @NotBlank(message = "El título de la reseña es obligatorio")
  private String title;
  
  @Column(columnDefinition = "TEXT")
  @NotBlank(message = "El contenido de la reseña es obligatorio")
  private String message;

  @Min(1)
  @Max(5)
  @NotNull(message = "La valoración del producto es obligatoria")
  private Integer rating;

  private LocalDate date;

  @ManyToOne 
  private User user;

  @ManyToOne 
  private Component component;

  public Review() {
  }

  public Review(String title, String message, Integer rating) {
    super();
    this.title = title;
    this.message = message;
    this.rating = rating;
    this.date = LocalDate.now(ZoneId.of("Europe/Madrid"));
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }

  public LocalDate getDate(){
    return date;
  }

  public void setDate(LocalDate date){
    this.date = date;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Component getComponent() {
    return component;
  }

  public void setComponent(Component component) {
    this.component = component;
  }
}
