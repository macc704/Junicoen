package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class UniToBlockOperatorTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = UniToBlockWhileTest.parseClass("Operator");
		
		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		engine.execute(dec);
		String output = baos.toString("UTF8");

		String expect = "Hello World" + System.lineSeparator();
		assertEquals(expect, output);
		
		UniToBlockParser parser = new UniToBlockParser();
		parser.parse(dec);
		
	}

}
