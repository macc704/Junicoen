package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniClassDec;

import org.junit.Test;


public class UniToBlockTurtlesTest {

	@Test
	public void test() throws IOException {
		String fileName = "Turtles.xml";
		String outputFileName = "UniToBlockTurtle.xml";
		String filePath = "blockeditor/";

		File targetXml = new File(filePath + fileName);

		BlockMapper mapper = new BlockMapper();

		UniClassDec dec = mapper.parse(targetXml);

		// --------------------------
		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		engine.execute(dec);
		String output = baos.toString("UTF8");

		String expect = "F50B50R90L90";
		assertEquals(expect, output);


		File file = new File(filePath + outputFileName);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		BlockGenerator parser = new BlockGenerator(out);
		parser.parse(dec);
		}

}
