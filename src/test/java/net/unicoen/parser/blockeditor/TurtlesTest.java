package net.unicoen.parser.blockeditor;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniNode;

import org.junit.Test;

public class TurtlesTest {

	@Test
	public void test() throws UnsupportedEncodingException {
		String file = "Turtles.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);
		List<UniNode> list = ToBlockEditorParser.parse(targetXml);

		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();
		for (UniNode node : list) {
			dec.members.add((UniMethodDec) node);
		}
		// --------------------------
		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		engine.execute(dec);
		String output = baos.toString("UTF8");

		String expect = "F50B50R90L90";
		assertEquals(expect, output);
	}
}
