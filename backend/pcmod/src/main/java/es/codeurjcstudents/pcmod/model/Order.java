package es.codeurjcstudents.pcmod.model;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import es.codeurjcstudents.pcmod.enums.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity(name = "OrderTable")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    private String userUsername;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal totalPrice;

    private String shippingAddress;

    private LocalDateTime createdAt = LocalDateTime.now(ZoneId.of("Europe/Madrid"));

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(User user, OrderStatus status, String shippingAddress, LocalDateTime createdAt) {
        this.user = user;
        if (user != null) {
            this.userUsername = user.getUsername();
        }
        this.status = status;
        this.shippingAddress = shippingAddress;
        this.createdAt = createdAt;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public Long getId() { 
      return id; 
    }

    public void setId(Long id) { 
      this.id = id; 
    }

    public User getUser() { 
      return user; 
    }

    public void setUser(User user) { 
      this.user = user; 
      if (user != null) {
          this.userUsername = user.getUsername();
      }
    }

    public String getUserUsername() { 
      return userUsername; 
    }

    public void setUserUsername(String userUsername) { 
      this.userUsername = userUsername; 
    }

    public OrderStatus getStatus() { 
      return status; 
    }

    public void setStatus(OrderStatus status) { 
      this.status = status; 
    }

    public BigDecimal getTotalPrice() { 
      return totalPrice; 
    }

    public void setTotalPrice(BigDecimal totalPrice) { 
      this.totalPrice = totalPrice; 
    }

    public String getShippingAddress() { 
      return shippingAddress; 
    }

    public void setShippingAddress(String shippingAddress) { 
      this.shippingAddress = shippingAddress; 
    }

    public LocalDateTime getCreatedAt() { 
      return createdAt; 
    }

    public void setCreatedAt(LocalDateTime createdAt) { 
      this.createdAt = createdAt; 
    }

    public List<OrderItem> getItems() { 
      return items; 
    }

    public void setItems(List<OrderItem> items) { 
      this.items = items; 
    }
}