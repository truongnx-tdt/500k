package models;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class OrderDetailId implements Serializable {
    private Integer idProduct;
    private Integer idOrder;

    public OrderDetailId() {}

    public OrderDetailId(Integer idProduct, Integer idOrder) {
        this.idProduct = idProduct;
        this.idOrder = idOrder;
    }

    // getters & setters

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderDetailId)) return false;
        OrderDetailId that = (OrderDetailId) o;
        return Objects.equals(idProduct, that.idProduct) &&
                Objects.equals(idOrder, that.idOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProduct, idOrder);
    }
}
