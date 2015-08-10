package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBlock;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniVariableDec;

import org.junit.Test;

public class UniToBlockExCallTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = new UniClassDec();
		dec.className = "UniToBlockExCall";
		dec.members = new ArrayList<>();

		String fileName = dec.className;
		String filePath = "blockeditor/" + fileName + ".xml";

		File file = new File(filePath);
		file.createNewFile();
		PrintStream out = new PrintStream(file);

		UniMethodDec method = new UniMethodDec("start", new ArrayList<>(), "void", new ArrayList<>(), null);
		dec.members.add(method);

		List<UniExpr> blocks = new ArrayList<>();
		UniBlock block = new UniBlock(blocks);
		method.block = block;

		UniVariableDec var = new UniVariableDec(null, "Turtle", "t", null);
		UniVariableDec numVar = new UniVariableDec(null, "int", "i", new UniMethodCall(new UniIdent("MyLib"), "input", new ArrayList<>()));
		List<UniExpr> args = new ArrayList<>();
		args.add(new UniIntLiteral(50));
		UniMethodCall fd = new UniMethodCall(new UniIdent("t"), "fd", args);

		blocks.add(var);
		blocks.add(numVar);
		blocks.add(fd);

		BlockGenerator parser = new BlockGenerator(out);
		parser.parse(dec);
	}

}
