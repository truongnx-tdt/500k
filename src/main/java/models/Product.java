package models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Product")
public class Product {

    public Product() {}

    @Id
    @Column(name = "IdProduct")
    private Integer idProduct;

    @Column(name = "NameProduct", length = 100)
    private String nameProduct;

    @Column(name = "PriceProduct", precision = 19, scale = 4)
    private BigDecimal priceProduct;

    @Column(name = "Decriptions", length = 150)
    private String decriptions;

    @Lob
    @Column(name = "Images")
    private byte[] images;

    @Column(name = "IsActive")
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdTypeProduct")
    private TypeProduct typeProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdEmployee")
    private Employee employee;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<OrderDetail> orderDetails = new HashSet<>();


    // --- getters/setters ---
    public Integer getIdProduct() { return idProduct; }
    public void setIdProduct(Integer idProduct) { this.idProduct = idProduct; }

    public String getNameProduct() { return nameProduct; }
    public void setNameProduct(String nameProduct) { this.nameProduct = nameProduct; }

    public BigDecimal getPriceProduct() { return priceProduct; }
    public void setPriceProduct(BigDecimal priceProduct) { this.priceProduct = priceProduct; }

    public String getDecriptions() { return decriptions; }
    public void setDecriptions(String decriptions) { this.decriptions = decriptions; }

    public byte[] getImages() { return images; }
    public void setImages(byte[] images) { this.images = images; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public TypeProduct getTypeProduct() { return typeProduct; }
    public void setTypeProduct(TypeProduct typeProduct) { this.typeProduct = typeProduct; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public Set<OrderDetail> getOrderDetails() { return orderDetails; }
    public void setOrderDetails(Set<OrderDetail> orderDetails) { this.orderDetails = orderDetails; }
}
