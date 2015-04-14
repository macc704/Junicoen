package net.unicoen.parser.blockeditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBinOp;
import net.unicoen.node.UniBlock;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniDoubleLiteral;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniVariableDec;

import org.junit.Test;

public class UniToBlockOperatorTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();

		List<UniExpr> blocks = new ArrayList<>();
		//String s;
		UniVariableDec vdec = UniToBlockTestUtil.createVariableDecModel("String", "s");
		blocks.add(vdec);
		//s = "hello + world";
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdec.name), UniToBlockTestUtil.createBinOpModel("+", new UniStringLiteral("hello"), new UniStringLiteral("world")));
		//s="Hello World";
		UniBinOp binOp = UniToBlockTestUtil.createBinOpModel("=", new UniIdent(vdec.name), new UniStringLiteral("Hello World"));
		blocks.add(binOp);
		//int i;
		UniVariableDec vdecInt = UniToBlockTestUtil.createVariableDecModel("int", "i");
		blocks.add(vdecInt);
		//i = 1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecInt.name), new UniIntLiteral(1));
		//i = 1+1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecInt.name), UniToBlockTestUtil.createBinOpModel("+", new UniIntLiteral(1), new UniIntLiteral(1)));
		//i = 1+1+1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecInt.name), UniToBlockTestUtil.createBinOpModel("+", new UniIntLiteral(1), UniToBlockTestUtil.createBinOpModel("+", new UniIntLiteral(1), new UniIntLiteral(1))));
		//i=1-1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecInt.name), UniToBlockTestUtil.createBinOpModel("-", new UniIntLiteral(1), new UniIntLiteral(1)));
		//i=1*1
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecInt.name), UniToBlockTestUtil.createBinOpModel("*", new UniIntLiteral(1), new UniIntLiteral(1)));
		//i=1/1
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecInt.name), UniToBlockTestUtil.createBinOpModel("/", new UniIntLiteral(1), new UniIntLiteral(1)));
		//i=1%1
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecInt.name), UniToBlockTestUtil.createBinOpModel("%", new UniIntLiteral(1), new UniIntLiteral(1)));

		//double d;
		UniVariableDec vdecDouble = UniToBlockTestUtil.createVariableDecModel("double", "d");
		blocks.add(vdecDouble);
		//d = 1.0;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecDouble.name), new UniDoubleLiteral(1.0));
		//d = 1.0 + 1.0;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecDouble.name), UniToBlockTestUtil.createBinOpModel("+", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));
		//d = 1.0 + 1.0 + 1.0;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecDouble.name), UniToBlockTestUtil.createBinOpModel("+", new UniDoubleLiteral(1.0), UniToBlockTestUtil.createBinOpModel("+", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0))));
		//d = 1.0 - 1.0;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecDouble.name), UniToBlockTestUtil.createBinOpModel("-", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));
		//d = 1.0 * 1.0;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecDouble.name), UniToBlockTestUtil.createBinOpModel("*", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));
		//d = 1.0 / 1.0;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecDouble.name), UniToBlockTestUtil.createBinOpModel("/", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));
		//d = 1.0 % 1.0;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecDouble.name), UniToBlockTestUtil.createBinOpModel("%", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));

		//boolean b;
		UniVariableDec vdecBoolean = UniToBlockTestUtil.createVariableDecModel("boolean", "b");
		blocks.add(vdecBoolean);
		//b = true;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), new UniBoolLiteral(true));
		//b = 1>1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel(">", new UniIntLiteral(1), new UniIntLiteral(1)));
		//b = 1>=1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel(">=", new UniIntLiteral(1), new UniIntLiteral(1)));
		//b = 1<1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("<", new UniIntLiteral(1), new UniIntLiteral(1)));
		//b = 1<=1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("<=", new UniIntLiteral(1), new UniIntLiteral(1)));
		//b = 1>1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel(">", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));
		//b = 1>=1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel(">=", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));
		//b = 1<1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("<", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));
		//b = 1<=1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("<=", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));

		//b = 1<1 || 1>1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("||", UniToBlockTestUtil.createBinOpModel("<", new UniIntLiteral(1) , new UniIntLiteral(1)), UniToBlockTestUtil.createBinOpModel(">", new UniIntLiteral(1) , new UniIntLiteral(1))));
		//b = 1<1 && 1>1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("&&", UniToBlockTestUtil.createBinOpModel("<", new UniIntLiteral(1) , new UniIntLiteral(1)), UniToBlockTestUtil.createBinOpModel(">", new UniIntLiteral(1) , new UniIntLiteral(1))));
		//b = 1 == 1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("==", new UniIntLiteral(1), new UniIntLiteral(1)));
		//b = 1 != 1;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("!=", new UniIntLiteral(1), new UniIntLiteral(1)));
		//b = 1.0 == 1.0;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("==", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));
		//b = 1.0 != 1.0;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("!=", new UniDoubleLiteral(1.0), new UniDoubleLiteral(1.0)));
		//b = true == true;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("==", new UniBoolLiteral(true), new UniBoolLiteral(true)));
		//b = true != true;
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("!=", new UniBoolLiteral(true), new UniBoolLiteral(true)));
		//b = "hello" == "hello";
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("==", new UniStringLiteral("hello"), new UniStringLiteral("hello")));
		//b = "hello" != "hello";
		UniToBlockTestUtil.createBinOpModel(blocks, "=", new UniIdent(vdecBoolean.name), UniToBlockTestUtil.createBinOpModel("!=", new UniStringLiteral("hello"), new UniStringLiteral("hello")));

		UniBlock block = new UniBlock(blocks);
		UniMethodDec method = new UniMethodDec("start", null, "void", null, block);

		dec.members.add(method);

		dec.className = "UniToBlockOperator";

		UniToBlockParser parser = new UniToBlockParser();
		parser.parse(dec);


	}

}
