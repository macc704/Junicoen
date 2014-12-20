package net.unicoen.node;

public class UniCondOp extends UniExpr {
	public UniExpr cond;
	public UniExpr trueExpr;
	public UniExpr falseExpr;

	public UniCondOp() {
	}

	public UniCondOp(UniExpr cond, UniExpr trueExpr, UniExpr falseExpr) {
		this.cond = cond;
		this.trueExpr = trueExpr;
		this.falseExpr = falseExpr;
	}

	@Override
	public String toString() {
		return "CondOp";
	}
}
