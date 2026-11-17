package es.codeurjcstudents.pcmod.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = true)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Component component;

    private String componentName;

    private int quantity;

    private BigDecimal unitPriceAtPurchase;

    public OrderItem() {
    }

    public OrderItem(Order order, Component component, int quantity, BigDecimal unitPriceAtPurchase) {
        this.order = order;
        this.component = component;
        this.quantity = quantity;
        this.unitPriceAtPurchase = unitPriceAtPurchase;
        if (component != null) {
            this.componentName = component.getName();
        }
    }

    public Long getId() { 
      return id; 
    }

    public void setId(Long id) { 
      this.id = id; 
    }

    public Order getOrder() { 
      return order; 
    }

    public void setOrder(Order order) { 
      this.order = order; 
    }

    public Component getComponent() { 
      return component; 
    }

    public void setComponent(Component component) { 
      this.component = component; 
    }

    public String getComponentName() { 
      return componentName; 
    }

    public void setComponentName(String componentName) { 
      this.componentName = componentName; 
    }

    public int getQuantity() { 
      return quantity; 
    }

    public void setQuantity(int quantity) { 
      this.quantity = quantity; 
    }

    public BigDecimal getUnitPriceAtPurchase() { 
      return unitPriceAtPurchase; 
    }

    public void setUnitPriceAtPurchase(BigDecimal unitPriceAtPurchase) { 
      this.unitPriceAtPurchase = unitPriceAtPurchase; 
    }
}