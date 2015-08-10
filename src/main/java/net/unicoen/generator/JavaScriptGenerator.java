package net.unicoen.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import net.unicoen.node.Traverser;
import net.unicoen.node.UniArg;
import net.unicoen.node.UniArray;
import net.unicoen.node.UniBinOp;
import net.unicoen.node.UniBlock;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniBreak;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniContinue;
import net.unicoen.node.UniDoWhile;
import net.unicoen.node.UniDoubleLiteral;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFieldAccess;
import net.unicoen.node.UniFieldDec;
import net.unicoen.node.UniFor;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniLongLiteral;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniReturn;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniTernaryOp;
import net.unicoen.node.UniUnaryOp;
import net.unicoen.node.UniVariableDec;
import net.unicoen.node.UniWhile;

public class JavaScriptGenerator extends Traverser{

	private final PrintStream out;
	private int indent = 0;
	private boolean indentAtThisLine = false;

	private final IntStack exprPriority = new IntStack();
	private String className = "";

	public JavaScriptGenerator(PrintStream out) {
		this.out = out;
		exprPriority.push(0);
	}

	private void print(String str) {
		if (indentAtThisLine == false) {
			indentAtThisLine = true;
			for (int i = 0; i < indent; i++) {
				out.print("\t");
			}
		}
		out.print(str);
	}

	private int priorityTable(String operator) {
		switch (operator) {
		case "*":
		case "/":
		case "%":
			return 11;
		case "+":
		case "-":
			return 10;
		case ">>":
		case "<<":
			return 9;
		case ">":
		case ">=":
		case "<":
		case "<=":
		case "instanceof":
			return 8;
		case "==":
		case "!=":
			return 7;
		case "&":
			return 6;
		case "^":
			return 5;
		case "|":
			return 4;
		case "&&":
			return 3;
		case "||":
			return 2;
		case "?":
		case ":":
			return 1;
		}
		return 0;
	}

	@Override
	public void traverseBoolLiteral(UniBoolLiteral node) {
		print(node.value ? "true" : "false");
	}

	@Override
	public void traverseIntLiteral(UniIntLiteral node) {
		print(Integer.toString(node.value));
	}

	@Override
	public void traverseLongLiteral(UniLongLiteral node) {
		print(Long.toString(node.value));
		print("L");
	}

	@Override
	public void traverseDoubleLiteral(UniDoubleLiteral node) {
		print(Double.toString(node.value));
	}

	@Override
	public void traverseStringLiteral(UniStringLiteral node) {
		print('"' + node.value.replaceAll("\"", "\\\"") + '"');
	}

	@Override
	public void traverseArray(UniArray node) {
		// TODO Auto-generated method stub
		throw new RuntimeException("not supported yet");
	}

	@Override
	public void traverseFieldAccess(UniFieldAccess node) {
		// TODO Auto-generated method stub
		throw new RuntimeException("not supported yet");
	}

	@Override
	public void traverseMethodCall(UniMethodCall node) {
		parseExpr(node.receiver);
		print(".");
		print(node.methodName);
		print("(");
		boolean isFirst = true;
		for (UniExpr innerExpr : iter(node.args)) {
			if (isFirst) {
				isFirst = false;
			} else {
				print(", ");
			}
			parseExpr(innerExpr);
		}
		print(")");
	}

	private void parseExpr(UniExpr node) {
		parseExpr(node, 0);
	}

	private void parseExpr(UniExpr node, int priority) {
		exprPriority.push(priority);
		traverseExpr(node);
		exprPriority.pop();
	}
	@Override
	public void traverseUnaryOp(UniUnaryOp node) {
		if (node.operator.startsWith("_")) {
			parseExpr(node.expr);
			print(node.operator.substring(1));
		} else if (node.operator.endsWith("_")) {
			print(node.operator.substring(0, node.operator.length() - 1));
			parseExpr(node.expr);
		} else {
			print(node.operator);
			parseExpr(node.expr);
		}
	}

	@Override
	public void traverseBinOp(UniBinOp node) {
		int priority = priorityTable(node.operator) * 10 + 1;
		assert priority > 0;
		boolean requireParen = exprPriority.peek() >= priority;
		if (requireParen) {
			print("(");
		}
		parseExpr(node.left, priority - 1);
		print(" ");
		print(node.operator);
		print(" ");
		parseExpr(node.right, priority);
		if (requireParen) {
			print(")");
		}
	}

	@Override
	public void traverseTernaryOp(UniTernaryOp node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseReturn(UniReturn node) {
		print("return ");
		parseExpr(node.value);
	}

	@Override
	public void traverseBreak(UniBreak node) {
		print("break");
	}

	@Override
	public void traverseContinue(UniContinue node) {
		print("continue");
	}

	@Override
	public void traverseBlock(UniBlock node) {
		printlnIndent("{");
		indent++;
		for(UniExpr expr : node.body){
			parseStatement(expr);
		}
		indent--;
		printlnIndent("}");
	}

	public void parseBlockInnner(UniBlock node){
		indent++;
		parseExpr(node);
		indent--;
	}
	private void withIndent(Runnable runnable) {
		indent++;
		runnable.run();
		indent--;
	}

	private void parseBlockInner(UniBlock block) {
		withIndent(() -> {
			for (UniExpr expr : block.body)
				parseStatement(expr);
		});
	}

	private void parseStatement(UniExpr expr) {
		parseExpr(expr);
		if (expr.isStatement() == false) {
			print(";");
		}
		newline();
	}

	@Override
	public void traverseIf(UniIf node) {
		print("if (");
		parseExpr(node.cond);
		print(")");
		if (node.trueStatement == null) {
			x_println(" {");
			print("}");
		} else if (node.trueStatement instanceof UniBlock) {
			x_println(" {");
			parseBlockInner((UniBlock) node.trueStatement);
			print("}");
		} else {
			newline(); // ifの後ろの改行
			withIndent(() -> {
				parseStatement(node.trueStatement);
			});
		}
		if (node.falseStatement != null) {
			if (indentAtThisLine) {
				print(" "); // 閉じカッコがすでに出力されているので空白を開ける
			}
			print("else");
			if (node.falseStatement instanceof UniBlock) {
				x_println(" {");
				parseBlockInner((UniBlock) node.falseStatement);
				println("}");
			} else {
				newline(); // elseの後ろの改行
				withIndent(() -> {
					parseStatement(node.falseStatement);
				});
			}
		}
	}

	@Override
	public void traverseFor(UniFor node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseWhile(UniWhile node) {
		 genBlock((UniBlock)node.statement, () -> {
		 print("while (");
		 parseExpr(node.cond);
		 print(")");
		 }, null);
	}

	private void genBlock(UniBlock block, Runnable beforeBlock, Runnable afterBlock) {
		if (beforeBlock != null) {
			printIndent();
			beforeBlock.run();
			println(" {");
		} else {
			printlnIndent("{");
		}
		genBlockInner(block);
		if (afterBlock != null) {
			printIndent();
			print("} ");
			afterBlock.run();
			println("");
		} else {
			printlnIndent("}");
		}
	}

	private void genBlockInner(UniBlock block) {
		indent++;
		if (block != null) {
			for (UniExpr expr : iter(block.body)) {
				if (expr.isStatement()) {
					parseExpr(expr);
				} else {
					printIndent();
					parseExpr(expr);
					println(";");
				}
			}
		}
		indent--;
	}

	@Override
	public void traverseDoWhile(UniDoWhile node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseVariableDec(UniVariableDec node) {
		print(String.join(" ", "var", node.name));

		if(node.value != null){
			print(" = ");
			parseExpr(node.value);
		}
	}

	@Override
	public void traverseFieldDec(UniFieldDec node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseMethodDec(UniMethodDec node) {
		ArrayList<String> args = new ArrayList<>();
		out.print(className + ".prototype." + node.methodName + " = function");

		if(node.args != null){
			for(UniArg arg : node.args){
				args.add(arg.name);
			}
		}

		out.print("(" + String.join(", ", args) + ")");
		traverseBlock(node.block);
	}

	@Override
	public void traverseArg(UniArg node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseClassDec(UniClassDec node) {
		out.println("var " + node.className + " = function() {};");
		className = node.className;

		for(UniMemberDec dec : iter(node.members)){
			traverseMemberDec(dec);
		}
	}

	private static <T> Iterable<T> iter(Iterable<T> iter) {
		if (iter == null) {
			return Collections.emptyList();
		}
		return iter;
	}

	private void printlnIndent(String str) {
		printIndent();
		out.println(str);
	}

	private void printIndent() {
		for (int i = 0; i < indent; i++) {
			out.print("\t");
		}
	}

	@Override
	public void traverseIdent(UniIdent node) {
		print(node.name);
	}

	public static String generate(UniClassDec dec) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream(); PrintStream printer = new PrintStream(out)) {
			generate(dec, printer);
			return out.toString();
		} catch (IOException e) {
			return null;
		}
	}

	public static void generate(UniClassDec classDec, PrintStream out) {
		JavaScriptGenerator g = new JavaScriptGenerator(out);
		g.traverseClassDec(classDec);
	}

	private void x_println(String str) {
		print(str);
		newline();
	}
	private void newline() {
		out.print("\n");
	}

	private void println(String str) {
		out.println(str);
	}


}
