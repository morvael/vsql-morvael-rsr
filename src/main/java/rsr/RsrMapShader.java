package rsr;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;

import javax.swing.KeyStroke;

import VASSAL.build.module.Map;
import VASSAL.build.module.map.MapShader;
import VASSAL.build.module.map.boardPicker.Board;
import VASSAL.build.module.map.boardPicker.board.ZonedGrid;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import terrain.HexRef;
import terrain.TerrainHexGrid;

/**
 *
 * @author morvael
 */
public class RsrMapShader extends MapShader {

  private Board gridBoard;
  private TerrainHexGrid grid;
  private HashMap<Double, HashMap<HexRef, Area>> zoomMap = new HashMap<Double, HashMap<HexRef, Area>>();
  private Set<HexRef> shadedHexes;

  public RsrMapShader() {
    super();
  }

  @Override
  public String[] getAttributeNames() {
    return new String[]{NAME, ALWAYS_ON, STARTS_ON, BUTTON_TEXT, TOOLTIP,
              ICON, HOT_KEY, BOARDS, BOARD_LIST, TYPE, DRAW_OVER, PATTERN,
              COLOR, IMAGE, OPACITY, BORDER, BORDER_COLOR, BORDER_WIDTH,
              BORDER_OPACITY
            };
  }

  @Override
  public Class<?>[] getAttributeTypes() {
    return new Class<?>[]{String.class, Boolean.class, Boolean.class,
              String.class, String.class, IconConfig.class, KeyStroke.class,
              BoardPrompt.class, String[].class, TypePrompt.class,
              Boolean.class, PatternPrompt.class, Color.class, Image.class,
              Integer.class, Boolean.class, Color.class, Integer.class,
              Integer.class
            };
  }

  @Override
  public String[] getAttributeDescriptions() {
    return new String[]{"Name:  ", "Shading Always On?  ",
              "Shading Starts turned on?  ", "Button text:  ",
              "Tooltip Text:  ", "Button Icon:  ", "Hotkey:  ",
              "All boards in map get Shaded?  ", "Board List:  ", "Type:  ",
              "Draw Shade on top of Counters?  ", "Shade Pattern:  ",
              "Color:  ", "Image:  ", "Opacity(%)", "Border?  ",
              "Border Color:  ", "Border Width:  ", "Border opacity(%)"
            };
  }

  public static String getConfigureTypeName() {
    return "Rsr Map Shading";
  }

  private TerrainHexGrid getGrid() {
    if (grid == null) {
      findGrid();
    }
    return grid;
  }

  private void findGrid() {
    gridBoard = null;
    grid = null;
    for (Board b : map.getBoards()) {
      if (b.getGrid() instanceof TerrainHexGrid) {
        gridBoard = b;
        grid = (TerrainHexGrid) b.getGrid();
        break;
      } else if (b.getGrid() instanceof ZonedGrid) {
        ZonedGrid zg = (ZonedGrid) b.getGrid();
        List<TerrainHexGrid> l = zg.getComponentsOf(TerrainHexGrid.class);
        if (l.size() > 0) {
          gridBoard = b;
          grid = l.get(0);
          break;
        }

      }
    }
  }

  private Area getHexArea(double zoom, HexRef hr) {
    HashMap<HexRef, Area> hexMap = zoomMap.get(zoom);
    Area result = hexMap != null ? hexMap.get(hr) : null;
    if (result == null) {
      getGrid();
      result = grid.getSingleHex(grid.getHexCenter(hr.getColumn(), hr.getRow()));
      if (gridBoard != null) {
        Rectangle r = gridBoard.bounds();
        result = new Area(AffineTransform.getTranslateInstance(r.x, r.y).createTransformedShape(result));
      }
      if (zoom != 1.0) {
        result = new Area(AffineTransform.getScaleInstance(zoom, zoom).createTransformedShape(result));
      }
      if (hexMap == null) {
        hexMap = new HashMap<HexRef, Area>();
        zoomMap.put(zoom, hexMap);
      }
      hexMap.put(hr, result);
    }
    return result;
  }

  public void setShadedHexes(Set<HexRef> shadedHexes) {
    if (this.shadedHexes != shadedHexes) {
      this.shadedHexes = shadedHexes;
      shadingVisible = (shadedHexes != null) && (shadedHexes.size() > 0);
      map.repaint();
    }
  }

  public Set<HexRef> getShadedHexes() {
    return shadedHexes;
  }

  @Override
  public void setLaunchButtonVisibility() {
    launch.setVisible(false);
  }

  @Override
  public void draw(Graphics g, Map map) {
    if ((shadedHexes != null) && (shadedHexes.size() > 0)) {

      double zoom = map.getZoom();
      buildStroke(zoom);

      final Graphics2D g2 = (Graphics2D) g;

      final Composite oldComposite = g2.getComposite();
      final Color oldColor = g2.getColor();
      final Paint oldPaint = g2.getPaint();
      final Stroke oldStroke = g2.getStroke();

      g2.setComposite(getComposite());
      g2.setColor(getColor());
      g2.setPaint(scaleImage && pattern.equals(TYPE_IMAGE) && imageName != null ? getTexture(zoom) : getTexture());
      for (HexRef hr : shadedHexes) {
        Area area = getHexArea(zoom, hr);
        g2.fill(area);
        if (border) {
          g2.setComposite(getBorderComposite());
          g2.setStroke(getStroke(map.getZoom()));
          g2.setColor(getBorderColor());
          g2.draw(area);
        }
      }

      g2.setComposite(oldComposite);
      g2.setColor(oldColor);
      g2.setPaint(oldPaint);
      g2.setStroke(oldStroke);
    }
  }
}
