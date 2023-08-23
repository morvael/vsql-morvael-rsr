package rsr;

import VASSAL.build.module.map.PieceMover;
import VASSAL.counters.Deck;
import VASSAL.counters.DeckVisitor;
import VASSAL.counters.DeckVisitorDispatcher;
import VASSAL.counters.GamePiece;
import VASSAL.counters.PieceIterator;
import VASSAL.counters.PieceVisitorDispatcher;
import VASSAL.counters.Properties;
import VASSAL.counters.Stack;
import VASSAL.counters.DragBuffer;
import VASSAL.counters.KeyBuffer;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author derwido
 * @since 2009-05-08
 */
public class RsrPieceMover extends PieceMover {

  @Override
  protected PieceVisitorDispatcher createSelectionProcessor() {
    return new DeckVisitorDispatcher(new DeckVisitor() {

      public Object visitDeck(Deck d) {
        DragBuffer.getBuffer().clear();
        /*
        ArrayList<GamePiece> pieces = new ArrayList<GamePiece>();
        PieceIterator it = d.drawCards();
        while (it.hasMoreElements()) {
          pieces.add(it.nextPiece());
        }
        RsrAdvance.getInstance().selectPiecesToMove(map, pieces);
        for (GamePiece gp : pieces) {
          DragBuffer.getBuffer().add(gp);
        }
        */
        return null;
      }

      public Object visitStack(Stack s) {
        DragBuffer.getBuffer().clear();
        ArrayList<GamePiece> pieces = new ArrayList<GamePiece>();
        for (int i = 0; i < s.getPieceCount(); i++) {
          if ((Boolean.TRUE.equals(s.getPieceAt(i).getProperty(Properties.SELECTED))) && (Boolean.TRUE.equals(s.getPieceAt(i).getProperty(Properties.NON_MOVABLE)) == false)) {
            pieces.add(s.getPieceAt(i));
          }
        }
        Iterator<GamePiece> i = KeyBuffer.getBuffer().getPiecesIterator();
        while (i.hasNext()) {
          if (i.next().getParent() != s) {
            i.remove();
          }
        }
        RsrAdvance.getInstance().selectPiecesToMove(map, pieces);
        for (GamePiece gp : pieces) {
          DragBuffer.getBuffer().add(gp);
        }
        return null;
      }

      public Object visitDefault(GamePiece selected) {
        if (selected.getParent() != null) {
          return visitStack(selected.getParent());
        }
        DragBuffer.getBuffer().clear();
        ArrayList<GamePiece> pieces = new ArrayList<GamePiece>();
        if (Boolean.TRUE.equals(selected.getProperty(Properties.NON_MOVABLE)) == false) {
          pieces.add(selected);
        }
        Iterator<GamePiece> i = KeyBuffer.getBuffer().getPiecesIterator();
        while (i.hasNext()) {
          if (i.next() != selected) {
            i.remove();
          }
        }
        RsrAdvance.getInstance().selectPiecesToMove(map, pieces);
        for (GamePiece gp : pieces) {
          DragBuffer.getBuffer().add(gp);
        }
        return null;
      }
    });
  }

  @Override
  public void mouseReleased(MouseEvent e) {
    if (canHandleEvent(e)) {
      Point p = e.getPoint();
      if ((isClick(p) == false) && (DragBuffer.getBuffer().isEmpty() == false)) {
        ArrayList<GamePiece> pieces = getMovingPieces();
        Point sp = map.snapTo(p);
        if (RsrAdvance.getInstance().isValidTarget(map, sp, pieces)) {
          performDrop(sp);
          RsrAdvance.getInstance().movementDone(map, sp, pieces);
        }
        RsrAdvance.getInstance().sendAndLog();
      }
    }
    RsrAdvance.getInstance().clearMovementHighlight();
    dragBegin = null;
    map.getView().setCursor(null);
  }

  @Override
  protected void performDrop(Point p) {
    PieceIterator it = DragBuffer.getBuffer().getIterator();
    while (it.hasMoreElements()) {
      RsrAdvance.getInstance().commandBaseMove(map, it.nextPiece(), p);
    }
    RsrAdvance.getInstance().commandBaseReportMoves();
    DragBuffer.getBuffer().clear();
  }

  protected ArrayList<GamePiece> getMovingPieces() {
    ArrayList<GamePiece> pieces = new ArrayList<GamePiece>();
    PieceIterator it = DragBuffer.getBuffer().getIterator();
    GamePiece gp;
    while (it.hasMoreElements()) {
      gp = it.nextPiece();
      if (gp instanceof Stack) {
        Stack s = (Stack) gp;
        for (int i = 0; i < s.getPieceCount(); i++) {
          pieces.add(s.getPieceAt(i));
        }
      } else {
        pieces.add(gp);
      }
    }
    return pieces;
  }

}
