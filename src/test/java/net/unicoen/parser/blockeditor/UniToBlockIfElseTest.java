package net.unicoen.parser.blockeditor;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniNode;

import org.junit.Test;

public class UniToBlockIfElseTest {

	@Test
	public void test() throws IOException {

		String file = "IfElse.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);

		UniClassDec dec = ToBlockEditorParser.parse(targetXml);

		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		dec.className = "UniToBlockIfElse";

		engine.execute(dec);
		String output = baos.toString("UTF8");

		String expect = "Hello World" + System.lineSeparator() + "Bye World" + System.lineSeparator();
		assertEquals(expect, output);

		UniToBlockParser parser = new UniToBlockParser();
		parser.parse(dec);

	}

}
