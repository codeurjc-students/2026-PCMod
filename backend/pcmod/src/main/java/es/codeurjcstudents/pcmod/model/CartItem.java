package es.codeurjcstudents.pcmod.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private Component component;

    private int quantity;

    public CartItem() {}

    public CartItem(Cart cart, Component component, int quantity) {
        this.cart = cart;
        this.component = component;
        this.quantity = quantity;
    }

    public Long getId() {
      return id; 
    }

    public void setId(Long id) { 
      this.id = id; 
    }

    public Cart getCart() { 
      return cart; 
    }
    
    public void setCart(Cart cart) { 
      this.cart = cart; 
    }

    public Component getComponent() { 
      return component; 
    }

    public void setComponent(Component component) { 
      this.component = component; 
    }

    public int getQuantity() { 
      return quantity; 
    }

    public void setQuantity(int quantity) { 
      this.quantity = quantity; 
    }
}