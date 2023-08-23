package rsr;

import VASSAL.counters.GamePiece;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author Dominik
 */
public class StackInfo {

  public double stackSize;
  public HashSet<String> nationalities = new HashSet<String>();
  public int guardsCavalryCorps = 0;

  public StackInfo(RsrAdvanceBase core, ArrayList<GamePiece> pieces) {
    if (pieces != null) {
      for (GamePiece gp : pieces) {
        if (RsrTrait.getType(gp).equals("Marker") == false) {
          stackSize += RsrTrait.getStackPoints(gp);
          nationalities.add(RsrTrait.getNationality(gp));
          if (RsrTrait.getUnitType(gp).equals("SovietGuardsCavalryCorps")) {
            guardsCavalryCorps++;
          }
        }
      }
    }
  }

}
