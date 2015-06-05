package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class UniToBlockWhileTest {

	@Test
	public void test() throws IOException {

		UniClassDec dec = parseClass("While");

		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);


		engine.execute(dec);

		String output = baos.toString("UTF8");
		String expect = "Bye World" + System.lineSeparator();
		assertEquals(expect, output);

		String fileName = "UniToBlock" + dec.className;
		String filePath = "blockeditor/" + fileName + ".xml";

		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		BlockGenerator parser = new BlockGenerator(out);
		parser.parse(dec);
	}


	public static UniClassDec parseClass(String fileName){
		String file = fileName +  ".xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);

		BlockMapper mapper = new BlockMapper();
		UniClassDec dec = mapper.parse(targetXml);

		dec.className = "UniToBlock" + fileName;

		return dec;
	}


}
