package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniNode;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniVariableDec;
import net.unicoen.node.UniVariableDecWithValue;

import org.junit.Test;

public class LocalVarDec {

	@Test
	public void test() {
		String file = "LocalVardec.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);

		UniClassDec dec = ToBlockEditorParser.parse(targetXml);

		UniMethodDec fdec = (UniMethodDec) dec.members.get(0);
		//int
		assertTrue(fdec.block.body.get(0) instanceof UniVariableDec);
		if(fdec.block.body.get(0) instanceof UniVariableDec){
			assertEquals("i", ((UniVariableDec) fdec.block.body.get(0)).name);
			assertEquals("int", ((UniVariableDec) fdec.block.body.get(0)).type);
		}else{
			assertTrue(false);
		}
		if(fdec.block.body.get(1) instanceof UniVariableDecWithValue){
			UniVariableDecWithValue varDec = (UniVariableDecWithValue) fdec.block.body.get(1);
			assertEquals("i1", varDec.name);
			assertEquals("int", varDec.type);
			assertEquals(1, ((UniIntLiteral)varDec.value).value);
		}else{
			assertTrue(false);
		}
//		//double
//		assertTrue(fdec.block.body.get(2) instanceof UniVarDec);
//		if(fdec.block.body.get(2) instanceof UniVarDec){
//			assertEquals("d", ((UniVarDec) fdec.block.body.get(2)).name);
//			assertEquals("double", ((UniVarDec) fdec.block.body.get(2)).type);
//		}else{
//			assertTrue(false);
//		}
//		if(fdec.block.body.get(3) instanceof UniVarDecWithValue){
//			UniVarDecWithValue varDec = (UniVarDecWithValue) fdec.block.body.get(3);
//			assertEquals("d1", varDec.name);
//			assertEquals("double", varDec.type);
//			assertEquals("1.0", ((UniStringLiteral)varDec.value).value);
//		}else{
//			assertTrue(false);
//		}

		//String
		if(fdec.block.body.get(2) instanceof UniVariableDec){
			assertEquals("s", ((UniVariableDec) fdec.block.body.get(2)).name);
			assertEquals("String", ((UniVariableDec) fdec.block.body.get(2)).type);
		}else{
			assertTrue(false);
		}

		if(fdec.block.body.get(3) instanceof UniVariableDecWithValue){
			UniVariableDecWithValue varDec = (UniVariableDecWithValue) fdec.block.body.get(3);
			assertEquals("s1", varDec.name);
			assertEquals("String", varDec.type);
			assertEquals("あいうえお", ((UniStringLiteral)varDec.value).value);
		}else{
			assertTrue(false);
		}

		//boolean
		if(fdec.block.body.get(4) instanceof UniVariableDec){
			assertEquals("b", ((UniVariableDecWithValue) fdec.block.body.get(4)).name);
			assertEquals("boolean", ((UniVariableDecWithValue) fdec.block.body.get(4)).type);
		}else{
			assertTrue(false);
		}

		if(fdec.block.body.get(5) instanceof UniVariableDecWithValue){
			UniVariableDecWithValue varDec = (UniVariableDecWithValue) fdec.block.body.get(5);
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
