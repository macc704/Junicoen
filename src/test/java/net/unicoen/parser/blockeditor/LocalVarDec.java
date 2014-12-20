package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniDecVar;
import net.unicoen.node.UniDecVarWithValue;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniNode;
import net.unicoen.node.UniStringLiteral;

import org.junit.Test;

public class LocalVarDec {

	@Test
	public void test() {
		String file = "LocalVardec.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);
		List<UniNode> list = ToBlockEditorParser.parse(targetXml);
		assertTrue(list != null);
		assertEquals(1, list.size());

		assertTrue(list.get(0) instanceof UniFuncDec);
		
		
		UniFuncDec fdec = (UniFuncDec) list.get(0);
		assertEquals("start", fdec.funcName);
		
		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();
		for (UniNode node : list) {
			dec.members.add((UniFuncDec) node);
		}
		//int
		assertTrue(fdec.block.body.get(0) instanceof UniDecVar);
		if(fdec.block.body.get(0) instanceof UniDecVar){
			assertEquals("i", ((UniDecVar) fdec.block.body.get(0)).name);
			assertEquals("int", ((UniDecVar) fdec.block.body.get(0)).type);
		}else{
			assertTrue(false);
		}
		if(fdec.block.body.get(1) instanceof UniDecVarWithValue){
			UniDecVarWithValue varDec = (UniDecVarWithValue) fdec.block.body.get(1);
			assertEquals("i1", varDec.name);
			assertEquals("int", varDec.type);
			assertEquals(1, ((UniIntLiteral)varDec.value).value);
		}else{
			assertTrue(false);
		}
//		//double
//		assertTrue(fdec.block.body.get(2) instanceof UniDecVar);
//		if(fdec.block.body.get(2) instanceof UniDecVar){
//			assertEquals("d", ((UniDecVar) fdec.block.body.get(2)).name);
//			assertEquals("double", ((UniDecVar) fdec.block.body.get(2)).type);
//		}else{
//			assertTrue(false);
//		}
//		if(fdec.block.body.get(3) instanceof UniDecVarWithValue){
//			UniDecVarWithValue varDec = (UniDecVarWithValue) fdec.block.body.get(3);
//			assertEquals("d1", varDec.name);
//			assertEquals("double", varDec.type);
//			assertEquals("1.0", ((UniStringLiteral)varDec.value).value);
//		}else{
//			assertTrue(false);
//		}
		
		//String
		if(fdec.block.body.get(2) instanceof UniDecVar){
			assertEquals("s", ((UniDecVar) fdec.block.body.get(2)).name);
			assertEquals("String", ((UniDecVar) fdec.block.body.get(2)).type);
		}else{
			assertTrue(false);
		}

		if(fdec.block.body.get(3) instanceof UniDecVarWithValue){
			UniDecVarWithValue varDec = (UniDecVarWithValue) fdec.block.body.get(3);
			assertEquals("s1", varDec.name);
			assertEquals("String", varDec.type);
			assertEquals("あいうえお", ((UniStringLiteral)varDec.value).value);
		}else{
			assertTrue(false);
		}
		
		//boolean
		if(fdec.block.body.get(4) instanceof UniDecVar){
			assertEquals("b", ((UniDecVar) fdec.block.body.get(4)).name);
			assertEquals("boolean", ((UniDecVar) fdec.block.body.get(4)).type);
		}else{
			assertTrue(false);
		}

		if(fdec.block.body.get(5) instanceof UniDecVarWithValue){
			UniDecVarWithValue varDec = (UniDecVarWithValue) fdec.block.body.get(5);
			assertEquals("b1", varDec.name);
			assertEquals("boolean", varDec.type);
			assertEquals(false, ((UniBoolLiteral)varDec.value).value);
		}else{
			assertTrue(false);
		}
		
		
//		// --------------------------
//		Engine engine = new Engine();
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		engine.out = new PrintStream(baos);
//
//		engine.execute(dec);
//		String output = baos.toString("UTF8");
//
//		String expect = "Hello World" + System.lineSeparator();
//		assertEquals(expect, output);
	}

}
