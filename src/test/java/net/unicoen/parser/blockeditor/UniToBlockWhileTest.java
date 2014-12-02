package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniNode;

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
		
		
		UniToBlockParser parser = new UniToBlockParser();
		parser.parse(dec);
	}
	
	
	public static UniClassDec parseClass(String fileName){
		String file = fileName +  ".xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);
		List<UniNode> list = ToBlockEditorParser.parse(targetXml);
		
		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();
		for (UniNode node : list) {
			dec.members.add((UniFuncDec) node);
		}
		
		dec.className = "UniToBlock" + fileName;
		
		return dec;
	}
	

}
