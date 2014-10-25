package net.unicoen.node;

import java.util.ArrayList;
import java.util.List;

public class Builder {

	@SafeVarargs
	public static <T> List<T> list(T... args) {
		ArrayList<T> list = new ArrayList<>();
		for (T item : args) {
			list.add(item);
		}
		return list;
	}

	public static UniIntLiteral lit(int num) {
		UniIntLiteral lit = new UniIntLiteral();
		lit.value = num;
		return lit;
	}

	public static UniBoolLiteral lit(boolean flag) {
		UniBoolLiteral lit = new UniBoolLiteral();
		lit.value = flag;
		return lit;
	}

	public static UniIdent ident(String identName) {
		UniIdent ident = new UniIdent();
		ident.name = identName;
		return ident;
	}

	public static UniBinOp bin(UniExpr leftExpr, String op, UniExpr rightExpr) {
		UniBinOp bin = new UniBinOp();
		bin.operator = op;
		bin.left = leftExpr;
		bin.right = rightExpr;
		return bin;
	}
}
