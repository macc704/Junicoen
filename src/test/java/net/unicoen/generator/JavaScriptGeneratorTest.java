package net.unicoen.generator;

import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBlock;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniVariableDec;
import net.unicoen.node.UniWhile;

import org.junit.Test;

public class JavaScriptGeneratorTest {

	@Test
	public void test() {
		UniClassDec dec = new UniClassDec("Hoge", new ArrayList<String>(), new ArrayList<>());
		UniMethodDec mdec = new UniMethodDec("start", new ArrayList<>(), "void", new ArrayList<>(), new UniBlock());
		UniVariableDec vdec = new UniVariableDec(new ArrayList<>(), "int", "i", new UniIntLiteral(1));
		UniBlock whileBlocks = new UniBlock(new ArrayList<>());
		List<UniExpr> args = new ArrayList<UniExpr>();
		args.add(new UniIntLiteral(50));
		whileBlocks.body.add(new UniMethodCall(new UniIdent("MyLib"), "fd", args));

		UniWhile wS = new UniWhile(new UniBoolLiteral(true), whileBlocks);

		mdec.block.body = new ArrayList<>();
		mdec.block.body.add(vdec);
		mdec.block.body.add(wS);
		dec.members.add(mdec);


		String out = JavaScriptGenerator.generate(dec);

		System.out.println(out);


	}



}
