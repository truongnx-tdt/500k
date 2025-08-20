package models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "TypeProduct")
public class TypeProduct {

    public TypeProduct() {}

    @Id
    @Column(name = "IdTypeProduct")
    private Integer idTypeProduct;

    @Column(name = "NameType", length = 50)
    private String nameType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "IdEmployee")
    private Employee employee;

    @OneToMany(mappedBy = "typeProduct", fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();
}
