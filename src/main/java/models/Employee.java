package models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Employee")
public class Employee {

    public Employee() {}

    @Id
    @Column(name = "IdEmployee")
    private Integer idEmployee;

    @Column(name = "NameEmployee", length = 250)
    private String nameEmployee;

    // C# là datetime2; nếu chỉ cần ngày: LocalDate. Nếu cần cả giờ, đổi sang LocalDateTime
    @Column(name = "BirthDay")
    private LocalDate birthDay;

    @Column(name = "Gender")
    private Boolean gender;

    @Column(name = "Address", length = 250)
    private String address;

    @Column(name = "Email", length = 200)
    private String email;

    @Column(name = "Password", length = 100)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdRole")
    private RoleEmployee roleEmployee;

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private Set<Order> orders = new HashSet<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private Set<TypeProduct> typeProducts = new HashSet<>();

    // === Getter & Setter ===
    public Integer getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(Integer idEmployee) {
        this.idEmployee = idEmployee;
    }

    public String getNameEmployee() {
        return nameEmployee;
    }

    public void setNameEmployee(String nameEmployee) {
        this.nameEmployee = nameEmployee;
    }

    public LocalDate getBirthDay() {
        return birthDay;
    }

    public void setBirthDay(LocalDate birthDay) {
        this.birthDay = birthDay;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public RoleEmployee getRoleEmployee() {
        return roleEmployee;
    }

    public void setRoleEmployee(RoleEmployee roleEmployee) {
        this.roleEmployee = roleEmployee;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public Set<Product> getProducts() {
        return products;
    }

    public void setProducts(Set<Product> products) {
        this.products = products;
    }

    public Set<TypeProduct> getTypeProducts() {
        return typeProducts;
    }

    public void setTypeProducts(Set<TypeProduct> typeProducts) {
        this.typeProducts = typeProducts;
    }
}
