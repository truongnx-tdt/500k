package models;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "RoleEmployee")
public class RoleEmployee {

    public RoleEmployee() {}

    @Id
    @Column(name = "IdRole")
    private Integer idRole;

    @Column(name = "NameRole", length = 100)
    private String nameRole;

    @OneToMany(mappedBy = "roleEmployee", fetch = FetchType.LAZY)
    private Set<Employee> employees = new HashSet<>();

    // === Getter & Setter ===
    public Integer getIdRole() {
        return idRole;
    }

    public void setIdRole(Integer idRole) {
        this.idRole = idRole;
    }

    public String getNameRole() {
        return nameRole;
    }

    public void setNameRole(String nameRole) {
        this.nameRole = nameRole;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
}
