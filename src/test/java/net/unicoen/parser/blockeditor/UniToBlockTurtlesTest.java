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


public class UniToBlockTurtlesTest {

	@Test
	public void test() throws IOException {
		String fileName = "Turtles.xml";
		String filePath = "blockeditor/" + fileName;
		File targetXml = new File(filePath);

		UniClassDec dec = new UniClassDec();

		// --------------------------
		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		dec.className = "UniToBlockTurtle";

		engine.execute(dec);
		String output = baos.toString("UTF8");

		String expect = "F50B50R90L90";
		assertEquals(expect, output);


		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		BlockGenerator parser = new BlockGenerator(out);
		parser.parse(dec);
		}

}
