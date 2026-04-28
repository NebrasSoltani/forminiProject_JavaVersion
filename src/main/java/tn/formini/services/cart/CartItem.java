package tn.formini.services.cart;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import tn.formini.entities.produits.Produit;

import java.math.BigDecimal;
import java.util.Objects;

public class CartItem {
    private final Produit produit;
    private final IntegerProperty quantity = new SimpleIntegerProperty(1);
    private final ReadOnlyObjectWrapper<BigDecimal> lineTotal = new ReadOnlyObjectWrapper<>(BigDecimal.ZERO);

    public CartItem(Produit produit, int quantity) {
        this.produit = Objects.requireNonNull(produit, "produit");
        setQuantity(quantity);

        this.quantity.addListener((obs, oldV, newV) -> recomputeLineTotal());
        recomputeLineTotal();
    }

    public Produit getProduit() {
        return produit;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        if (quantity < 1) quantity = 1;
        this.quantity.set(quantity);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal.get();
    }

    public ReadOnlyObjectProperty<BigDecimal> lineTotalProperty() {
        return lineTotal.getReadOnlyProperty();
    }

    public void increment(int delta) {
        if (delta <= 0) return;
        setQuantity(getQuantity() + delta);
    }

    private void recomputeLineTotal() {
        BigDecimal prix = produit.getPrix() == null ? BigDecimal.ZERO : produit.getPrix();
        lineTotal.set(prix.multiply(BigDecimal.valueOf(getQuantity())));
    }
}

