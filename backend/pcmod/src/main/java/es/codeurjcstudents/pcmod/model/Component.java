package es.codeurjcstudents.pcmod.model;

import es.codeurjcstudents.pcmod.enums.ComponentType;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Component {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id = null;

  @NotBlank(message = "El nombre del componente es obligatorio")
  private String name;

  @Column(columnDefinition = "TEXT")
  @NotBlank(message = "La descripción del componente no puede estar vacía")
  private String description;

  @Enumerated(EnumType.STRING)
  @NotNull(message = "El componente debe tener un tipo")
  private ComponentType type;

  @NotBlank(message = "La marca del componente es obligatoria")
  private String brand;

  @NotNull(message = "El precio del componente es obligatorio")
  private BigDecimal price;

  @NotNull(message = "El stock del componente es obligatorio")
  private Integer stock;

  public Component() {
  }

  public Component(String name, String description, ComponentType type, String brand, BigDecimal price, Integer stock) {
    super();
    this.name = name;
    this.description = description;
    this.type = type;
    this.brand = brand;
    this.price = price;
    this.stock = stock;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ComponentType getType() {
    return type;
  }

  public void setType(ComponentType type) {
    this.type = type;
  }

  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Integer getStock() {
    return stock;
  }

  public void setStock(Integer stock) {
    this.stock = stock;
  }
}
