package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class ExCallMethodTest {

	@Test
	public void test() throws IOException {
		String filename = "ExCallMethod.xml";
		String filePath = "blockeditor/" + filename;
		File targetXml = new File(filePath);

		BlockMapper mapper = new BlockMapper();
		UniClassDec dec = mapper.parse(targetXml);

		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		BlockGenerator parser= new BlockGenerator(out);
		dec.className = "ExCallMethodReverse";
		parser.parse(dec);
	}

}
