package net.unicoen.interpreter;

import static net.unicoen.node_helper.Builder.bin;
import static net.unicoen.node_helper.Builder.ident;
import static net.unicoen.node_helper.Builder.list;
import static net.unicoen.node_helper.Builder.lit;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.function.IntUnaryOperator;

import net.unicoen.node.UniArg;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniWhile;

import org.junit.Test;

public class EngineTest {

	private UniClassDec mkClassDec() {
		/*
		 * public class Main {
		 *     public static void main(String[] args) {
		 *         if (true && false) {
		 *             MyLib.printInt(1);
		 *         } else {
		 *             MyLib.printInt(1 + 2);
		 *         }
		 *         while (false) {
		 *             1;
		 *         }
		 *     }
		 * }
		 */
		UniClassDec cDec = new UniClassDec();
		cDec.className = "Main";
		cDec.modifiers = list("public");

		UniFuncDec fDec = new UniFuncDec();
		{
			fDec.funcName = "start";
			fDec.modifiers = list("public", "static");
			fDec.returnType = "void";
			UniArg arg = new UniArg();
			{
				arg.type = "String[]";
				arg.name = "args";
			}
			fDec.args = list(arg);
			UniIf uIf = new UniIf();
			{
				uIf.cond = bin(lit(true), "&&", lit(false));
				UniMethodCall mCallT = new UniMethodCall();
				{
					mCallT.receiver = ident("MyLib");
					mCallT.methodName = "printInt";
					mCallT.args = list(lit(1));
				}
				uIf.trueBlock = list(mCallT);
				UniMethodCall mCallF = new UniMethodCall();
				{
					mCallF.receiver = ident("MyLib");
					mCallF.methodName = "printInt";
					mCallF.args = list(bin(lit(1), "+", lit(2)));
				}
				uIf.falseBlock = list(mCallF);
			}
			UniWhile uWhile = new UniWhile();
			uWhile.cond = lit(false);
			uWhile.body = list(lit(1));
			fDec.body = list(uIf, uWhile);
		}
		cDec.members = list(fDec);
		return cDec;
	}

	@Test
	public void test() throws UnsupportedEncodingException {
		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		engine.execute(mkClassDec());
		String output = baos.toString("UTF8");

		String expect = "3" + System.lineSeparator();
		assertEquals(expect, output);
	}

	@Test
	public void testFunctionObject() {
		// f(100)
		UniExpr expr = new UniMethodCall(null, "f", list(lit(100)));
		IntUnaryOperator twice = n -> n * 2;
		Object result = Engine.executeSimple(expr, "f", twice);
		assertEquals(result, 200);
	}

	@Test
	public void testNativeMethod() {
		String str = "1234567890";
		// str.substring(3,7)
		UniExpr expr = new UniMethodCall(new UniIdent("str"), "substring", list(lit(3), lit(7)));
		Object result = Engine.executeSimple(expr, "str", str);
		assertEquals(result, str.substring(3, 7));
	}
}
