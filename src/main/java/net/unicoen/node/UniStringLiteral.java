package net.unicoen.node;

public class UniStringLiteral extends UniExpr implements UniNode {
	public String value;

	@Override
	public String toString() {
		return "LitS(" + value + ")";
	}
}
