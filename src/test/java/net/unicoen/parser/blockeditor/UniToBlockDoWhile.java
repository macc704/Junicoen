package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBlock;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniDoWhile;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniVariableDec;

import org.junit.Test;

public class UniToBlockDoWhile {

	@Test
	public void test() throws IOException {
		String fileName = "UniToBlockDoWhile";
		String filePath = "blockeditor/" + fileName + ".xml";

		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();

		List<UniExpr> blocks = new ArrayList<>();

		UniVariableDec var = new UniVariableDec(new ArrayList<>(), "int", "i", new UniIntLiteral(0));
		blocks.add(var);

		UniBlock block = new UniBlock();

		//{print("hello world")}
		block.body = new ArrayList<>();
		List<UniExpr> args = new ArrayList<>();
		args.add(new UniStringLiteral("Hello World"));
		block.body.add(new UniMethodCall(new UniIdent("MyLib"), "print", args));

		//while(true)
		UniDoWhile dowhile = new UniDoWhile(block, new UniBoolLiteral(true));
		blocks.add(dowhile);


		UniMethodDec main = new UniMethodDec("start", null, "void", new ArrayList<>(), new UniBlock(blocks));
		dec.members.add(main);

		BlockGenerator parser = new BlockGenerator(out);
		parser.parse(dec);

	}

}
