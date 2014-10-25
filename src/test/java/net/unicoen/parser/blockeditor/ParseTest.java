package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import net.unicoen.node.UniNode;

import org.junit.Test;

public class ParseTest {

	@Test
	public void test() {
		String file = "hello.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);
		List<UniNode> list = ToBlockEditorParser.parse(targetXml);
		assertTrue(list != null);
	}
}
