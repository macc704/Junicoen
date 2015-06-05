package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBinOp;
import net.unicoen.node.UniBlock;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniVariableDecWithValue;

import org.junit.Test;

public class UniToBlockIfElseTestTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = new UniClassDec();
		dec.className = "UniToBlockIfElse";
		dec.members = new ArrayList<>();

		List<UniExpr> blocks = new ArrayList<>();
		UniVariableDecWithValue var = new UniVariableDecWithValue(null, "int", "i", new UniIntLiteral(0));
		blocks.add(var);
		UniIf uniIf = new UniIf();

		List<UniExpr> bodyBlocks = new ArrayList<>();
		List<UniExpr> args = new ArrayList<>();
		args.add(new UniIntLiteral(100));
		uniIf.cond = new UniBinOp("<", new UniIdent("i"), new UniIntLiteral(4));

		bodyBlocks.add(new UniMethodCall(new UniIdent("MyLib"), "fd", args));
		uniIf.trueBlock = new UniBlock(bodyBlocks);

		blocks.add(uniIf);

		UniMethodDec main = new UniMethodDec("start", null, "void", null, new UniBlock(blocks));
		dec.members.add(main);

		String fileName = dec.className;
		String filePath = "blockeditor/" + fileName + ".xml";

		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		BlockGenerator parser = new BlockGenerator(out);
		parser.parse(dec);
	}

}
