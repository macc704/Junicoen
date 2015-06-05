package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBlock;
import net.unicoen.node.UniBreak;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniContinue;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniMethodDec;

import org.junit.Test;

public class UniToBlockContinueBreakTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = new UniClassDec("UniToBlockContinueBreak", new ArrayList<String>(), new ArrayList<>());

		UniMethodDec method = new UniMethodDec("start", new ArrayList<>(), "void", new ArrayList<>(), new UniBlock());

		dec.members.add(method);

		List<UniExpr> bodyBlocks = new ArrayList<>();
		bodyBlocks.add(new UniContinue());
		bodyBlocks.add(new UniBreak());
		method.block.body = bodyBlocks;

		File file = new File("blockeditor/" + dec.className + ".xml");
		file.createNewFile();
		PrintStream out = new PrintStream(file);

		BlockGenerator generator = new BlockGenerator(out);
		generator.parse(dec);


	}

}
