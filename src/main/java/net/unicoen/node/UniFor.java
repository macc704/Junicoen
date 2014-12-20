package net.unicoen.node;

import java.util.List;

public class UniFor extends UniExpr {
	public UniExpr init;
	public UniExpr cond;
	public UniExpr step;
	public List<UniExpr> body;

	public UniFor() {
	}

	public UniFor(UniExpr init, UniExpr cond, UniExpr step, List<UniExpr> body) {
		this.init = init;
		this.cond = cond;
		this.step = step;
		this.body = body;
	}

	@Override
	public String toString() {
		return "For";
	}
}
