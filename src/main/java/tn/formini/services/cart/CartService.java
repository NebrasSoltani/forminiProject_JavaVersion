package tn.formini.services.cart;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tn.formini.entities.produits.Produit;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class CartService {
    private static final CartService INSTANCE = new CartService();

    private final Map<Integer, CartItem> byProductId = new LinkedHashMap<>();
    private final ObservableList<CartItem> items = FXCollections.observableArrayList();

    private CartService() {}

    public static CartService getInstance() {
        return INSTANCE;
    }

    public ObservableList<CartItem> getItems() {
        return items;
    }

    public CartItem getItemByProductId(int productId) {
        return byProductId.get(productId);
    }

    public int getQuantityForProduct(int productId) {
        CartItem item = byProductId.get(productId);
        return item == null ? 0 : item.getQuantity();
    }

    public int getItemsCountDistinct() {
        return items.size();
    }

    public int getItemsCountTotal() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getGrandTotal() {
        return items.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void add(Produit produit) {
        add(produit, 1);
    }

    public void add(Produit produit, int quantity) {
        Objects.requireNonNull(produit, "produit");
        if (quantity < 1) quantity = 1;

        int productId = produit.getId();
        CartItem existing = byProductId.get(productId);
        if (existing != null) {
            existing.increment(quantity);
            return;
        }

        CartItem item = new CartItem(produit, quantity);
        byProductId.put(productId, item);
        items.add(item);
    }

    public void remove(CartItem item) {
        if (item == null) return;
        byProductId.remove(item.getProduit().getId());
        items.remove(item);
    }

    public void removeByProductId(int productId) {
        CartItem item = byProductId.remove(productId);
        if (item != null) {
            items.remove(item);
        }
    }

    public void clear() {
        byProductId.clear();
        items.clear();
    }
}

