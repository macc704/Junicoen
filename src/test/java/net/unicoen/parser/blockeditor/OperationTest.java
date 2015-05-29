package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniNode;

import org.junit.Test;

public class OperationTest {

	@Test
	public void test() throws IOException {
		String fileName = "Operator";
		String filePath = "blockeditor/" + fileName + ".xml";
		File targetXml = new File(filePath);

		UniClassDec dec = ToBlockEditorParser.parse(targetXml);

		UniToBlockParser parser = new UniToBlockParser();
		dec.className = "OperatorBack";
		parser.parse(dec);



	}

}
