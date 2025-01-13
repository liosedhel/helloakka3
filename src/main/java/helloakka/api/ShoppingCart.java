package helloakka.api;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public record ShoppingCart(String cartId, List<LineItem> items, boolean checkedOut) {

    public record LineItem(String productId, String name, int quantity) {
        public LineItem withQuantity(int quantity) {
            return new LineItem(productId, name, quantity);
        }
    }

    public ShoppingCart onItemAdded(ShoppingCartEvent.ItemAdded itemAdded) {
        var item = itemAdded.item();
        var lineItem = updateItem(item);
        List<LineItem> lineItems = removeItemByProductId(item.productId());
        lineItems.add(lineItem);
        lineItems.sort(Comparator.comparing(LineItem::productId));
        return new ShoppingCart(cartId, lineItems, checkedOut);
    }

    private LineItem updateItem(LineItem item) {
        return findItemByProductId(item.productId())
                .map(li -> li.withQuantity(li.quantity() + item.quantity()))
                .orElse(item);
    }

    private List<LineItem> removeItemByProductId(String productId) {
        return items().stream()
                .filter(lineItem -> !lineItem.productId().equals(productId))
                .collect(Collectors.toList());
    }

    public Optional<LineItem> findItemByProductId(String productId) {
        return items.stream().filter(lineItem -> lineItem.productId().equals(productId)).findFirst();
    }
}
