package me.dannytatom.xibalba.map;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import me.dannytatom.xibalba.components.PlayerComponent;
import me.dannytatom.xibalba.components.PositionComponent;
import me.dannytatom.xibalba.utils.ComponentMappers;
import org.xguzm.pathfinding.grid.GridCell;

public class Map {
  public final int width;
  public final int height;
  private final Engine engine;
  private final Cell[][] map;

  /**
   * Holds logic for dealing with maps.
   *
   * @param engine The Ashley engine
   * @param map    The actual map
   */
  public Map(Engine engine, Cell[][] map) {
    this.engine = engine;
    this.map = map;

    this.width = this.map.length;
    this.height = this.map[0].length;
  }

  /**
   * Get pathfinding cells.
   *
   * @return 2d array of GridCells
   */
  public GridCell[][] createGridCells() {
    GridCell[][] cells = new GridCell[width][height];

    for (int x = 0; x < map.length; x++) {
      for (int y = 0; y < map[x].length; y++) {
        cells[x][y] = new GridCell(x, y, isWalkable(new Vector2(x, y)));
      }
    }

    return cells;
  }

  /**
   * Get starting light map.
   * <p>
   * 1 is blocked, 0 is not
   *
   * @return Resistance map
   */
  public float[][] createResistanceMap() {
    float[][] resistanceMap = new float[width][height];

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        resistanceMap[x][y] = getCell(x, y).isWall ? 1 : 0;
      }
    }

    return resistanceMap;
  }

  /**
   * Get the cell for this position.
   *
   * @param x x pos of cell
   * @param y y pos of cell
   * @return The Cell instance at this pos
   */
  public Cell getCell(int x, int y) {
    return map[x][y];
  }

  public Cell getCell(Vector2 position) {
    return getCell((int) position.x, (int) position.y);
  }

  /**
   * Find player pos.
   *
   * @return Vector2 of player pos
   */
  public Vector2 getPlayerPosition() {
    Entity player = engine.getEntitiesFor(Family.one(PlayerComponent.class).get()).first();
    PositionComponent position = ComponentMappers.position.get(player);

    return position.pos;
  }

  /**
   * Attempt to get the entity at the given position,
   * returns null if nobody is there.
   *
   * @param position The entity's position
   * @return The entity
   */
  public Entity getEntityAt(Vector2 position) {
    ImmutableArray<Entity> entities =
        engine.getEntitiesFor(Family.all(PositionComponent.class).get());

    for (Entity entity : entities) {
      if (entity.getComponent(PositionComponent.class).pos.epsilonEquals(position, 0.00001f)) {
        return entity;
      }
    }

    return null;
  }

  /**
   * Returns an open position near the player.
   * TODO: Make this less retarded.
   *
   * @return Player position
   */
  public Vector2 getNearPlayer() {
    Vector2 playerPosition = getPlayerPosition();
    Vector2 position;

    if (isWalkable(new Vector2(playerPosition.x + 1, playerPosition.y))) {
      position = new Vector2(playerPosition.x + 1, playerPosition.y);
    } else if (isWalkable(new Vector2(playerPosition.x - 1, playerPosition.y))) {
      position = new Vector2(playerPosition.x - 1, playerPosition.y);
    } else if (isWalkable(new Vector2(playerPosition.x, playerPosition.y + 1))) {
      position = new Vector2(playerPosition.x, playerPosition.y + 1);
    } else if (isWalkable(new Vector2(playerPosition.x, playerPosition.y - 1))) {
      position = new Vector2(playerPosition.x, playerPosition.y - 1);
    } else {
      position = null;
    }

    return position;
  }

  /**
   * Returns whether or not the given position is blocked.
   *
   * @param position Position to check
   * @return Is it blocked?
   */
  boolean isBlocked(Vector2 position) {
    boolean blocked = map[(int) position.x][(int) position.y].isWall;

    if (!blocked) {
      ImmutableArray<Entity> entities =
          engine.getEntitiesFor(Family.all(PositionComponent.class).get());

      for (Entity entity : entities) {
        PositionComponent ep = ComponentMappers.position.get(entity);

        if (ep.pos.epsilonEquals(position, 0.00001f)) {
          blocked = true;
          break;
        }
      }
    }

    return blocked;
  }

  public boolean isWalkable(Vector2 position) {
    return !isBlocked(position);
  }

  /**
   * Check if something is near the player.
   *
   * @param position Starting position
   * @return Whether we're near the player or not
   */
  public boolean isNearPlayer(Vector2 position) {
    Vector2 playerPosition = getPlayerPosition();

    return position.x <= playerPosition.x + 1
        && position.x >= playerPosition.x - 1
        && position.y <= playerPosition.y + 1
        && position.y >= playerPosition.y - 1;
  }

  /**
   * Uses light map to determine if they can see the player.
   *
   * @param position Entity's position
   * @param distance Radius to use
   * @return Can they see the player?
   */
  public boolean canSeePlayer(Vector2 position, int distance) {
    ShadowCaster caster = new ShadowCaster();
    float[][] lightMap = caster.calculateFOV(createResistanceMap(),
        (int) position.x, (int) position.y, distance);
    Vector2 playerPosition = getPlayerPosition();

    return lightMap[(int) playerPosition.x][(int) playerPosition.y] > 0;
  }

  /**
   * Find a random open cell.
   *
   * @return Random open cell
   */
  public Vector2 getRandomOpenPosition() {
    int x;
    int y;

    do {
      x = MathUtils.random(0, map.length - 1);
      y = MathUtils.random(0, map[x].length - 1);
    } while (isBlocked(new Vector2(x, y)));

    return new Vector2(x, y);
  }

  /**
   * Find player start.
   *
   * @return First open cell
   */
  public Vector2 findPlayerStart() {
    Vector2 space = new Vector2();

    search:
    for (int x = 0; x < map.length; x++) {
      for (int y = 0; y < map[x].length; y++) {
        if (isWalkable(new Vector2(x, y))) {
          space.add(x, y);

          break search;
        }
      }
    }

    return space;
  }
}
