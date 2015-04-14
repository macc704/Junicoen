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
		List<UniNode> list = ToBlockEditorParser.parse(targetXml);

		UniMethodDec fdec = (UniMethodDec) list.get(0);
		assertEquals("start", fdec.methodName);

		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();
		for (UniNode node : list) {
			dec.members.add((UniMethodDec) node);
		}

		UniToBlockParser parser = new UniToBlockParser();
		dec.className = "OperatorBack";
		parser.parse(dec);



	}

}
