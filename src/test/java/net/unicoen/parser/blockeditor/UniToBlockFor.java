package net.unicoen.parser.blockeditor;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniBlock;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFor;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniUnaryOp;
import net.unicoen.node.UniVariableDecWithValue;

import org.junit.Test;

public class UniToBlockFor {

	@Test
	public void test() throws IOException {
		UniClassDec dec = new UniClassDec();
		dec.className = "UniToBlockFor";
		dec.members = new ArrayList<>();

		List<UniExpr> blocks = new ArrayList<>();
		UniVariableDecWithValue var = new UniVariableDecWithValue(null, "int", "i", new UniIntLiteral(0));

		UniFor uniFor = new UniFor();
		//初期化子
		uniFor.init = var;
		uniFor.cond = UniToBlockTestUtil.createBinOpModel("<", new UniIdent(var.name), new UniIntLiteral(1));
		uniFor.step = new UniUnaryOp("_++", new UniIdent(var.name));

		List<UniExpr> bodyBlocks = new ArrayList<>();
		List<UniExpr> args = new ArrayList<>();
		args.add(new UniStringLiteral("Hello World"));

		bodyBlocks.add(new UniMethodCall(new UniIdent("MyLib"), "print", args));
		uniFor.block = new UniBlock(bodyBlocks);

		blocks.add(uniFor);

		UniMethodDec main = new UniMethodDec("start", null, "void", null, new UniBlock(blocks));
		dec.members.add(main);

		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		engine.execute(dec);
		String output = baos.toString("UTF8");
		String expect = "Hello World" + System.lineSeparator();

		assertEquals(expect, output);

		UniToBlockParser parser = new UniToBlockParser();
		parser.parse(dec);


	}

}
