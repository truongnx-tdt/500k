package models;

import jakarta.persistence.*;
import models.Order;
import models.OrderDetailId;

@Entity
@Table(name = "OrderDetail")
public class OrderDetail {

    public OrderDetail() {}

    @EmbeddedId
    private OrderDetailId id;

    @Column(name = "quantity")
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idOrder")
    @JoinColumn(name = "IdOrder")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProduct")
    @JoinColumn(name = "IdProduct")
    private Product product;
}
