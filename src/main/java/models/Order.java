package models;

import jakarta.persistence.*;
import models.Employee;
import models.OrderDetail;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Order")
public class Order {

    public Order() {}

    @Id
    @Column(name = "IdOrder")
    private Integer idOrder;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "Total", precision = 19, scale = 4)
    private BigDecimal total;

    @Column(name = "CreateDate")
    private LocalDateTime createDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdEmployee")
    private Employee employee;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    private Set<OrderDetail> orderDetails = new HashSet<>();

}
