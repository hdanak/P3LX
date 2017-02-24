/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.p3lx.ui.component;

import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.p3lx.ui.UI;
import heronarts.p3lx.ui.UI2dComponent;
import heronarts.p3lx.ui.UIFocus;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

public class UIDropMenu extends UI2dComponent implements UIFocus, LXParameterListener {

  private DiscreteParameter parameter = null;

  private boolean expanded = false;

  public enum Direction {
    DOWN,
    UP
  };

  private Direction direction = Direction.DOWN;
  private float closedY;
  private float closedHeight;

  private int highlight = -1;

  private String[] options;

  public UIDropMenu(float x, float y, float w, float h, DiscreteParameter parameter) {
    super(x, y, w, h);
    this.closedY = y;
    this.closedHeight = h;
    setParameter(parameter);
  }

  public UIDropMenu setParameter(DiscreteParameter parameter) {
    if (this.parameter != null) {
      this.parameter.removeListener(this);
    }
    this.parameter = parameter;
    setOptions(this.parameter.getOptions());
    this.parameter.addListener(this);
    return this;
  }

  public void onParameterChanged(LXParameter p) {
    this.highlight = this.parameter.getValuei();
    redraw();
  }

  /**
   * Sets the direction that this drop menu opens, up or down
   *
   * @param openDirection Up or down
   * @return this
   */
  public UIDropMenu setDirection(Direction direction) {
    this.direction = direction;
    return this;
  }

  /**
   * Sets the list of string options to display in the menu
   *
   * @param options Options array
   * @return this
   */
  public UIDropMenu setOptions(String[] options) {
    this.options = options;
    return this;
  }

  @Override
  protected int getFocusSize() {
    return 4;
  }

  @Override
  public void onDraw(UI ui, PGraphics pg) {
    String text;
    if (this.options != null) {
      text = this.options[this.parameter.getValuei()];
    } else {
      text = Integer.toString(this.parameter.getValuei());
    }
    pg.stroke(ui.theme.getControlBorderColor());
    pg.fill(ui.theme.getControlBackgroundColor());
    pg.rect(0, 0, this.width-1, this.height);

    float textY;
    float lineY;
    float highlightY;
    switch (this.direction) {
    case UP:
      lineY = textY = this.height - this.closedHeight;
      highlightY = this.closedHeight * this.highlight;
      break;
    default:
    case DOWN:
      textY = 0;
      lineY = this.closedHeight;
      highlightY = this.closedHeight * (1 + this.highlight);
      break;
    }


    if (this.expanded) {
      pg.line(1, lineY, this.width-2, lineY);
      pg.noStroke();
      pg.fill(ui.theme.getPrimaryColor());
      pg.rect(1, highlightY, this.width-2, this.closedHeight);
    }

    pg.textFont(hasFont() ? getFont() : ui.theme.getControlFont());
    pg.fill(ui.theme.getControlTextColor());
    pg.textAlign(PConstants.LEFT, PConstants.TOP);
    pg.text(text, 4, textY + 4);
    pg.textAlign(PConstants.RIGHT, PConstants.TOP);
    pg.text("▼", this.width-4, textY + 4);

    if (this.expanded) {
      int range = this.parameter.getRange();
      float yp = (this.direction == Direction.DOWN) ? this.closedHeight : 0;
      for (int i = 0; i < range; ++i) {
        String label = (this.options != null) ? this.options[i] : ("" + i);
        pg.fill(i == this.highlight ? 0xff000000 : ui.theme.getControlTextColor());
        pg.textAlign(PConstants.LEFT, PConstants.TOP);
        pg.text(label, 4, yp + 4);
        yp += this.closedHeight;
      }
    }
  }

  private void toggleExpanded() {
    setExpanded(!this.expanded);
  }

  private void setExpanded(boolean expanded) {
    if (this.expanded != expanded) {
      this.expanded = expanded;
      if (expanded) {
        this.highlight = this.parameter.getValuei();
        if (this.direction == Direction.UP) {
          setPosition(this.x, this.closedY - this.closedHeight * this.parameter.getRange());
        }
        setSize(this.width, this.closedHeight * (this.parameter.getRange() + 1));
      } else {
        setPosition(this.x, this.closedY);
        setSize(this.width, this.closedHeight);
      }
    }
  }

  private int getSelectedIndex(float y) {
    switch (this.direction) {
    case UP:
      if (y >= this.height - this.closedHeight) {
        return -1;
      }
      return (int) (y / this.closedHeight);
    default:
    case DOWN:
      if (y < this.closedHeight) {
        return -1;
      }
      return (int) ((y - this.closedHeight) / this.closedHeight);
    }
  }

  @Override
  public void onMousePressed(MouseEvent mouseEvent, float x, float y) {
    if (!this.expanded) {
      toggleExpanded();
    } else {
      int selected = this.getSelectedIndex(y);
      if (selected >= 0) {
        this.parameter.setValue(highlight);
      }
      toggleExpanded();
    }
  }

  @Override
  public void onMouseMoved(MouseEvent mouseEvent, float x, float y) {
    int selected = this.getSelectedIndex(y);
    if (selected >= 0 && (this.highlight != selected)) {
      this.highlight = selected;
      redraw();
    }
  }

  @Override
  public void onKeyPressed(KeyEvent keyEvent, char keyChar, int keyCode) {
    if (keyCode == java.awt.event.KeyEvent.VK_ENTER ||
        keyCode == java.awt.event.KeyEvent.VK_SPACE) {
      consumeKeyEvent();
      toggleExpanded();
    } else if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
      consumeKeyEvent();
      setExpanded(false);
    } else if (keyCode == java.awt.event.KeyEvent.VK_DOWN) {
      consumeKeyEvent();
      this.parameter.increment();
    } else if (keyCode == java.awt.event.KeyEvent.VK_UP) {
      consumeKeyEvent();
      this.parameter.decrement();
    }
  }



}