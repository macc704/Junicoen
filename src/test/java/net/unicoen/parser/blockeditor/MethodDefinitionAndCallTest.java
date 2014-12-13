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
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniNode;

import org.junit.Test;

public class MethodDefinitionAndCallTest {

	@Test
	public void test() throws UnsupportedEncodingException {
		String file = "MethodDefinitionAndCall.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);
		List<UniNode> list = ToBlockEditorParser.parse(targetXml);
		
		assertTrue(list != null);
		assertEquals(2, list.size());
		
		for(UniNode node : list){
			UniFuncDec func = (UniFuncDec)node;
			if(func.funcName.equals("start")){
				assertTrue(func.body.get(0) instanceof UniMethodCall);
				assertEquals("helloWorld", ((UniMethodCall)func.body.get(0)).methodName);
			}
			
			if(func.funcName.equals("helloWorld")){
				assertTrue(func.body.get(0) instanceof UniMethodCall);
				assertEquals("print", ((UniMethodCall)func.body.get(0)).methodName);
			}
			
		}
		
		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();
		for (UniNode node : list) {
			dec.members.add((UniFuncDec) node);
		}

	}

}
