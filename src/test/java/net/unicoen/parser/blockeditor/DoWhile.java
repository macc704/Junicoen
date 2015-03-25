package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniNode;

import org.junit.Test;

public class DoWhile {

	@Test
	public void test() throws IOException {
		String file = "DoWhile.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);
		List<UniNode> list = ToBlockEditorParser.parse(targetXml);

		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();
		for (UniNode node : list) {
			dec.members.add((UniMethodDec) node);
		}

		UniToBlockParser parser= new UniToBlockParser();
		dec.className = "DoWhileBack";
		parser.parse(dec);
	}

}
