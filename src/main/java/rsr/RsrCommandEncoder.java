package rsr;

import VASSAL.build.module.BasicCommandEncoder;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;

public class RsrCommandEncoder extends BasicCommandEncoder {

  @Override
	public Decorator createDecorator(String type, GamePiece inner) {
		if (type.startsWith(RsrImmobilized.ID)) {
			return new RsrImmobilized(type, inner);
		} else
		if (type.startsWith(RsrTrait.ID)) {
			return new RsrTrait(type, inner);
		} else
		if (type.startsWith(RsrGlobal.ID)) {
			return new RsrGlobal(type, inner);
		} else {
			return super.createDecorator(type, inner);
		}
	}
}
