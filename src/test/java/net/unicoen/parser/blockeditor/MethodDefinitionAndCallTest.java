package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.UnsupportedEncodingException;

import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class MethodDefinitionAndCallTest {

	@Test
	public void test() throws UnsupportedEncodingException {
		String file = "MethodDefinitionAndCall.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);

		BlockMapper mapper = new BlockMapper();
		UniClassDec dec = mapper.parse(targetXml);


	}

}
