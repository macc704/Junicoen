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


		UniClassDec dec = ToBlockEditorParser.parse(targetXml);;

		UniToBlockParser parser= new UniToBlockParser();
		dec.className = "DoWhileBack";
		parser.parse(dec);
	}

}
