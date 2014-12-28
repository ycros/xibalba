package me.dannytatom.xibalba.components.actions;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class MovementComponent extends Component {
  public static final int COST = 100;

  public final Vector2 pos;

  public MovementComponent(Vector2 pos) {
    this.pos = pos;
  }
}
