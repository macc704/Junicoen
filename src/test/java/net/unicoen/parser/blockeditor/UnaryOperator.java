package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;

import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class UnaryOperator {

	@Test
	public void test() throws IOException {
		String fileName = "UnaryOperator";
		String filePath = "blockeditor/" + fileName + ".xml";
		File targetXml = new File(filePath);

		UniClassDec dec = ToBlockEditorParser.parse(targetXml);

		UniToBlockParser parser = new UniToBlockParser();
		dec.className = fileName + "Back";
		parser.parse(dec);
	}

}
