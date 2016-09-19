package me.dannytatom.xibalba.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import me.dannytatom.xibalba.components.AttributesComponent;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.components.statuses.DrowningComponent;
import me.dannytatom.xibalba.utils.ComponentMappers;
import me.dannytatom.xibalba.world.WorldManager;

public class SwimmingSystem extends UsesEnergySystem {
  public SwimmingSystem() {
    super(Family.all(PositionComponent.class, AttributesComponent.class).get());
  }

  @Override
  protected void processEntity(Entity entity, float deltaTime) {
    PositionComponent position = ComponentMappers.position.get(entity);
    AttributesComponent attributes = ComponentMappers.attributes.get(entity);

    if (WorldManager.mapHelpers.getCell(position.pos.x, position.pos.y).isDeepWater()) {
      if (attributes.oxygen >= 2) {
        attributes.oxygen -= 2;
      }

      if (attributes.oxygen == 0 && !ComponentMappers.drowning.has(entity)) {
        entity.add(new DrowningComponent());
      }
    }
  }
}
