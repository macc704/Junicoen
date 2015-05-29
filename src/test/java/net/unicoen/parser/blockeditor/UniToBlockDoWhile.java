package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBlock;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniDoWhile;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniVariableDecWithValue;

import org.junit.Test;

public class UniToBlockDoWhile {

	@Test
	public void test() throws IOException {
		String fileName = "UniToBlockWhile";
		String filePath = "blockeditor/" + fileName + ".xml";

		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();

		List<UniExpr> blocks = new ArrayList<>();

		UniVariableDecWithValue var = new UniVariableDecWithValue(null, "int", "i", new UniIntLiteral(0));
		blocks.add(var);

		UniBlock block = new UniBlock();

		//{print("hello world")}
		block.body = new ArrayList<>();
		List<UniExpr> args = new ArrayList<>();
		args.add(new UniStringLiteral("Hello World"));
		block.body.add(new UniMethodCall(new UniIdent("MyLib"), "print", args));

		//while(i<1)
		UniDoWhile dowhile = new UniDoWhile(block, UniToBlockTestUtil.createBinOpModel("<", new UniIdent(var.name), new UniIntLiteral(1)));
		blocks.add(dowhile);


		UniMethodDec main = new UniMethodDec("start", null, "void", null, new UniBlock(blocks));
		dec.members.add(main);


		dec.className = "UniToBlockDoWhile";

		BlockGenerator parser = new BlockGenerator(out);
		parser.parse(dec);

	}

}
