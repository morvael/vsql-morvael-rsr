package filter;

import VASSAL.build.module.Map;
import VASSAL.counters.Decorator;
import java.util.ArrayList;
import VASSAL.counters.GamePiece;
import VASSAL.counters.Stack;
import java.util.HashMap;
import terrain.HexRef;
import terrain.TerrainHexGrid;

/**
 *
 * @author morvael
 */
public abstract class Filter {

  protected abstract FilterCheck getFilter(String filter);

  public ArrayList<GamePiece> filterPieces(ArrayList<GamePiece> pieces, String filter) {
    return filterPieces(pieces, getFilter(filter));
  }

  public ArrayList<GamePiece> filterPieces(ArrayList<GamePiece> pieces, FilterCheck check) {
    ArrayList<GamePiece> result = new ArrayList<GamePiece>();
    if (pieces != null) {
      for (GamePiece gp : pieces) {
        if ((check == null) || (check.accept(gp))) {
        	result.add(gp);
        }
      }
    }
    return result;
  }

  public ArrayList<GamePiece> filterPieces(Map m) {
    return filterPieces(m, (FilterCheck)null);
  }

  public ArrayList<GamePiece> filterPieces(Map m, String filter) {
    return filterPieces(m, getFilter(filter));
  }

  public ArrayList<GamePiece> filterPieces(Map m, FilterCheck check) {
    ArrayList<GamePiece> result = new ArrayList<GamePiece>();
		GamePiece[] p = m.getPieces();
		Stack st;
		GamePiece gp;
		for (int i = 0; i < p.length; ++i) {
			if (p[i] instanceof Stack) {
				st = (Stack)p[i];
				for (int j=0; j<st.getPieceCount(); j++) {
					gp = Decorator.getOutermost(st.getPieceAt(j));
          if ((check == null) || (check.accept(gp))) {
          	result.add(gp);
          }
				}
			} else {
        gp = Decorator.getOutermost(p[i]);
        if ((check == null) || (check.accept(gp))) {
        	result.add(gp);
        }
			}
		}
		return result;
  }

  public boolean check(GamePiece gp, String filter) {
    return check(gp, getFilter(filter));
  }

  public boolean check(GamePiece gp, FilterCheck check) {
    return (check == null) || (check.accept(Decorator.getOutermost(gp)));
  }

	protected void addPiece(TerrainHexGrid g, HashMap<HexRef, ArrayList<GamePiece>> pieces, GamePiece gp) {
		HexRef hr = g.getHexPos(gp.getPosition());
		ArrayList<GamePiece> list;
		if (pieces.containsKey(hr)) {
			list = pieces.get(hr);
		} else {
			list = new ArrayList<GamePiece>();
			pieces.put(hr, list);
		}
		list.add(gp);
	}

	public HashMap<HexRef, ArrayList<GamePiece>> filterPieceGroups(Map m, TerrainHexGrid g) {
		return filterPieceGroups(m, g, (FilterCheck)null);
	}

	public HashMap<HexRef, ArrayList<GamePiece>> filterPieceGroups(Map m, TerrainHexGrid g, String filter) {
    return filterPieceGroups(m, g, getFilter(filter));
  }

	public HashMap<HexRef, ArrayList<GamePiece>> filterPieceGroups(Map m, TerrainHexGrid g, FilterCheck check) {
    HashMap<HexRef, ArrayList<GamePiece>> result = new HashMap<HexRef, ArrayList<GamePiece>>();
		for (GamePiece gp : filterPieces(m, check)) {
			addPiece(g, result, gp);
		}
    return result;
	}

	public static boolean isAny(String c, String ... elements) {
		for (int i=0; i<elements.length; i++) {
			if (c.equals(elements[i])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAny(int c, int ... elements) {
		for (int i=0; i<elements.length; i++) {
			if (c == elements[i]) {
				return true;
			}
		}
		return false;
	}

}
