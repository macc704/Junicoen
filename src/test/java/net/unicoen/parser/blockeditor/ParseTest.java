package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class ParseTest {

	@Test
	public void test() throws UnsupportedEncodingException {
		String file = "hello.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);

		BlockMapper mapper = new BlockMapper();
		UniClassDec dec = mapper.parse(targetXml);

		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		engine.execute(dec);
		String output = baos.toString("UTF8");

		String expect = "Hello world" + System.lineSeparator();
		assertEquals(expect, output);

	}
}
