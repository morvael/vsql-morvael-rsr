package rsr;

import VASSAL.counters.GamePiece;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import terrain.HexRef;
import terrain.TerrainMapShaderCost;

/**
 *
 * @author morvael
 */
public class RsrAllowedMovement {

  private GamePiece gp;
  private HashMap<HexRef, TerrainMapShaderCost> area;
  private Set<HexRef> area2;
  private boolean substractCost;

  public RsrAllowedMovement(GamePiece gp, HashMap<HexRef, TerrainMapShaderCost> area, boolean substractCost) {
    this.gp = gp;
    this.area = area;
    this.area2 = new HashSet<HexRef>();
    TerrainMapShaderCost cost;
    for (HexRef hr : area.keySet()) {
      cost = area.get(hr);
      if (cost.isTraceOnly() == false) {
        area2.add(hr);
      }
    }
    this.substractCost = substractCost;
  }

  public RsrAllowedMovement(GamePiece gp, Set<HexRef> area) {
    this.gp = gp;
    this.area = null;
    this.area2 = area;
    this.substractCost = false;
  }

  public boolean isAllowed(HexRef hr) {
    return area2.contains(hr);
  }

  public Set<HexRef> getAllowedHexes() {
    return area2;
  }

  public TerrainMapShaderCost getCost(HexRef hr) {
    return (hr != null) && (area != null) ? area.get(hr) : null;
  }

  public void movementDone(HexRef hr) {
    if (substractCost) {
      TerrainMapShaderCost cost = getCost(hr);
      if (cost != null) {
        if (cost.getPointsLeft() > 0.0d) {
          RsrAdvance.getInstance().commandAllowMovement(gp, cost.getPointsLeft(), false);
        } else {
          RsrAdvance.getInstance().commandDisallowMovement(gp);
          RsrAdvance.getInstance().commandMarkAsMoved(gp);
        }
      }
    }
  }

  /**
   * Returns set of hexes that all pieces can access. If cost is provided, the
   * hexes will also be checked against the same path (all pieces must travel
   * through the same hexes).
   * @param pieces
   * @param allowedMovement
   * @return
   */
  public static Set<HexRef> getCommonAllowedHexes(ArrayList<GamePiece> pieces, HashMap<GamePiece, RsrAllowedMovement> allowedMovement) {
    if (pieces.size() == 0) {
      return null;
    } else
    if (pieces.size() == 1) {
      return allowedMovement.get(pieces.get(0)).getAllowedHexes();
    } else {
      HashSet<HexRef> result = new HashSet<HexRef>();
      RsrAllowedMovement am;
      RsrAllowedMovement base = allowedMovement.get(pieces.get(0));
      result.addAll(base.getAllowedHexes());
      if (base.area == null) {
        for (int i=1; i<pieces.size(); i++) {
          result.retainAll(allowedMovement.get(pieces.get(i)).getAllowedHexes());
        }
      } else {
        HexRef hr;
        TerrainMapShaderCost baseCost, cost;
        Iterator<HexRef> it = result.iterator();
        boolean ok;
        while (it.hasNext()) {
          hr = it.next();
          if (RsrAdvance.getInstance().testTargetForStack(hr, pieces, true) == false) {
            it.remove();
            continue;
          }
          ok = true;
          baseCost = base.getCost(hr);
          for (int i=1; i<pieces.size(); i++) {
            am = allowedMovement.get(pieces.get(i));
            cost = am.getCost(hr);
            ok = (ok) && (cost != null) && (baseCost.hasCommonFrom(cost));
            if (ok == false) {
              break;
            }
          }
          if (ok == false) {
            it.remove();
          }
        }
      }
      return result;
    }
  }

}
