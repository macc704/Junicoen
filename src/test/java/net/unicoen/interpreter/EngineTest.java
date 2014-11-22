package net.unicoen.interpreter;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.*;
import static org.junit.Assert.*;

import org.junit.Test;

import static net.unicoen.node_helper.Builder.*;

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
}
