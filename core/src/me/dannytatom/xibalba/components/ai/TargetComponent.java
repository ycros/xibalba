package me.dannytatom.xibalba.components.ai;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Vector2;

public class TargetComponent extends Component {
  public Vector2 pos;

  public TargetComponent(Vector2 target) {
    this.pos = target;
  }
}