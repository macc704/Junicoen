package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class OperationTest {

	@Test
	public void test() throws IOException {
		String fileName = "Operator";
		String filePath = "blockeditor/" + fileName + ".xml";
		File targetXml = new File(filePath);

		BlockMapper mapper = new BlockMapper();
		UniClassDec dec = mapper.parse(targetXml);

		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		BlockGenerator parser = new BlockGenerator(out);
		dec.className = "OperatorBack";
		parser.parse(dec);



	}

}
