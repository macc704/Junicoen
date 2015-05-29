package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBlock;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniReturn;

import org.junit.Test;

public class UniToBlockReturnTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();

		UniReturn returnModel = new UniReturn(new UniIntLiteral(1));
		List<UniExpr> blocks = new ArrayList<>();
		blocks.add(returnModel);
		UniBlock block = new UniBlock(blocks);
		UniMethodDec method = new UniMethodDec("start", null, "int", null, block);

		dec.members.add(method);

		dec.className = "UniToBlockReturn";

		String fileName = dec.className;
		String filePath = "blockeditor/" + fileName + ".xml";

		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		BlockGenerator parser = new BlockGenerator(out);

		parser.parse(dec);
	}

}
