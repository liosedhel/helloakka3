package helloakka.api;

import akka.Done;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.eventsourcedentity.EventSourcedEntity;
import akka.javasdk.eventsourcedentity.EventSourcedEntityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

//TODO KB cannot be renamed by accident
@ComponentId("shopping-cart")
public class ShoppingCartEntity extends EventSourcedEntity<ShoppingCart, ShoppingCartEvent> {

    private final String entityId;

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartEntity.class);

    public ShoppingCartEntity(EventSourcedEntityContext context) {
        this.entityId = context.entityId();
    }

    @Override
    public ShoppingCart emptyState() {
        return new ShoppingCart(entityId, Collections.emptyList(), false);
    }

    public Effect<Done> addItem(ShoppingCart.LineItem lineItem) {
        if (currentState().checkedOut()) {
            logger.info("Shopping cart has already been checked-out");
            return effects().error("Shopping cart has already been checked-out");
        } else {
            return effects().persist(new ShoppingCartEvent.ItemAdded(lineItem)).thenReply(cart -> Done.done());
        }
    }

    // Can be executed on any node, might not be consistent
    public ReadOnlyEffect<ShoppingCart> getCart() {
        return effects().reply(currentState());
    }

    @Override
    public ShoppingCart applyEvent(ShoppingCartEvent shoppingCartEvent) {
        return switch (shoppingCartEvent) {
            case ShoppingCartEvent.ItemAdded a -> currentState().onItemAdded(a);
            case ShoppingCartEvent.CheckedOut checkedOut -> currentState();
            case ShoppingCartEvent.ItemRemoved itemRemoved -> currentState();
        };
    }
}
