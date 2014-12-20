package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniNode;
import net.unicoen.node.UniWhile;

import org.junit.Test;

public class WhileTest {

	@Test
	public void test() throws UnsupportedEncodingException {
		String file = "While.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);
		List<UniNode> list = ToBlockEditorParser.parse(targetXml);
		assertTrue(list != null);
		assertEquals(1, list.size());

		assertTrue(list.get(0) instanceof UniFuncDec);
		
		
		UniFuncDec fdec = (UniFuncDec) list.get(0);
		assertEquals("start", fdec.funcName);
		
		assertTrue(fdec.block.body.get(0) instanceof UniWhile);
		UniWhile whilestmt = (UniWhile)fdec.block.body.get(0);
		
		assertEquals(2,whilestmt.block.body.size());
		
		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();
		for (UniNode node : list) {
			dec.members.add((UniFuncDec) node);
		}
		// --------------------------
		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		engine.execute(dec);
		String output = baos.toString("UTF8");

		String expect = "Bye World" + System.lineSeparator();
		assertEquals(expect, output);
	}

}
