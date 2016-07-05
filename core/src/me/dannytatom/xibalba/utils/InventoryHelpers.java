package me.dannytatom.xibalba.utils;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Vector2;
import me.dannytatom.xibalba.Main;
import me.dannytatom.xibalba.components.EquipmentComponent;
import me.dannytatom.xibalba.components.InventoryComponent;
import me.dannytatom.xibalba.components.ItemComponent;
import me.dannytatom.xibalba.components.PositionComponent;

import java.util.ArrayList;
import java.util.Objects;

public class InventoryHelpers {
  private final Main main;

  public InventoryHelpers(Main main) {
    this.main = main;
  }

  /**
   * Add an item to an entity's inventory.
   *
   * @param entity Entity we wanna to give shit to
   * @param item   The item itself
   */
  public void addItem(Entity entity, Entity item) {
    InventoryComponent inventory = ComponentMappers.inventory.get(entity);

    if (inventory != null) {
      if (inventory.items.size() < 10) {
        item.remove(PositionComponent.class);
        inventory.items.add(item);

        EquipmentComponent equipment = ComponentMappers.equipment.get(entity);
        ItemComponent itemDetails = ComponentMappers.item.get(item);

        main.log.add("You picked up a " + itemDetails.name);

        if (Objects.equals(itemDetails.type, "weapon")
            && equipment.slots.get("right hand") == null) {
          main.equipmentHelpers.holdItem(entity, item);
          main.log.add("You are now holding a " + itemDetails.name);
        }
      }
    }
  }

  public void eatItem(Entity entity, Entity item) {
    if (item != null) {
      ItemComponent itemDetails = ComponentMappers.item.get(item);

      if (itemDetails.attributes.get("raiseHealth") != null) {
        main.entityHelpers.raiseHealth(entity, itemDetails.attributes.get("raiseHealth"));
      }

      if (itemDetails.attributes.get("raiseStrength") != null) {
        main.entityHelpers.raiseStrength(entity, itemDetails.attributes.get("raiseStrength"));
      }

      removeItem(entity, item);
    }
  }

  /**
   * It's like drop except we remove it 100%.
   *
   * @param entity Entity we want to take shit from
   * @param item   The shit we gonna take
   */
  public void removeItem(Entity entity, Entity item) {
    InventoryComponent inventory = ComponentMappers.inventory.get(entity);

    if (inventory != null && item != null) {
      inventory.items.remove(item);
      main.engine.removeEntity(item);
    }
  }

  /**
   * Drop an item to the ground.
   *
   * @param entity   Whose shit we're dropping
   * @param item     What we're dropping
   * @param position Where we gonna drop it
   */
  public void dropItem(Entity entity, Entity item, Vector2 position) {
    InventoryComponent inventory = ComponentMappers.inventory.get(entity);

    if (inventory != null) {
      if (main.equipmentHelpers.isEquipped(entity, item)) {
        main.equipmentHelpers.removeItem(entity, item);
      }

      item.add(new PositionComponent(main.world.currentMapIndex, position));
      inventory.items.remove(item);

      if (ComponentMappers.player.has(entity)) {
        main.log.add("You dropped a " + ComponentMappers.item.get(item).name);
      }
    }
  }

  /**
   * This seems mostly unnecessary. Drop it at their feet.
   *
   * @param entity Whose feet we're dropping shit on
   * @param item   What we're dropping
   */
  public void dropItem(Entity entity, Entity item) {
    if (ComponentMappers.inventory.has(entity)) {
      dropItem(entity, item, ComponentMappers.position.get(entity).pos);
    }
  }

  /**
   * Iterate through inventory, return whatever item has the `throwing` flag set to true.
   *
   * @param entity Entity whose throwing things
   *
   * @return The item being thrown
   */
  public Entity getThrowingItem(Entity entity) {
    ArrayList<Entity> items = ComponentMappers.inventory.get(entity).items;

    for (Entity item : items) {
      ItemComponent itemDetails = ComponentMappers.item.get(item);

      if (itemDetails.throwing) {
        return item;
      }
    }

    return null;
  }

  /**
   * Find out if an entity has the ammunition it needs.
   *
   * @param entity Who needs to know
   * @param type   Type of ammunition they're looking for
   *
   * @return Whether or not they got it
   */
  public boolean hasAmmunitionOfType(Entity entity, String type) {
    ArrayList<Entity> items = ComponentMappers.inventory.get(entity).items;

    for (Entity item : items) {
      ItemComponent itemDetails = ComponentMappers.item.get(item);

      if (Objects.equals(itemDetails.type, type)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Return the first item of ammunition type given.
   *
   * @param entity Entity whose inventory we're looking at
   * @param type   Type of ammunition we're looking for
   *
   * @return Some (or, well, 1) ammunition
   */
  public Entity getAmmunitionOfType(Entity entity, String type) {
    ArrayList<Entity> items = ComponentMappers.inventory.get(entity).items;

    for (Entity item : items) {
      ItemComponent itemDetails = ComponentMappers.item.get(item);

      if (Objects.equals(itemDetails.type, type)) {
        return item;
      }
    }

    return null;
  }
}
