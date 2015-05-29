package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class UniToBlockNotOperationTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = UniToBlockWhileTest.parseClass("NotOperator");

		String fileName = dec.className;
		String filePath = "blockeditor/" + fileName + ".xml";

		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);


		BlockGenerator parser = new BlockGenerator(out);
		parser.parse(dec);
	}

}
