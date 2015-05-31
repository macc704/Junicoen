package net.unicoen.parser.blockeditor;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniNode;

import org.junit.Test;

public class MultistageIfTest {

	@Test
	public void test() throws UnsupportedEncodingException {
		String file = "MultistageIf.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);


		BlockMapper mapper = new BlockMapper();
		UniClassDec dec = mapper.parse(targetXml);

		// --------------------------
		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		engine.execute(dec);
		String output = baos.toString("UTF8");

		String expect = "Hello World" + System.lineSeparator() + "Hello World" + System.lineSeparator();
		assertEquals(expect, output);
	}

}