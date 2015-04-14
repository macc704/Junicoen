package net.unicoen.parser.blockeditor;

import static org.junit.Assert.*;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniContinue;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniNode;
import net.unicoen.node.UniWhile;

import org.junit.Test;

public class Break {

	@Test
	public void test() throws UnsupportedEncodingException {
		String file = "Break.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);
		List<UniNode> list = ToBlockEditorParser.parse(targetXml);
		assertTrue(list != null);
		assertEquals(1, list.size());

		assertTrue(list.get(0) instanceof UniMethodDec);
		
		
		UniMethodDec fdec = (UniMethodDec) list.get(0);
		assertEquals("start", fdec.methodName);
		
		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();
		for (UniNode node : list) {
			dec.members.add((UniMemberDec) node);
		}
		
		if(fdec.block.body.get(0) instanceof UniWhile){
			UniWhile uniWhile = (UniWhile)fdec.block.body.get(0);
			assertTrue(uniWhile.block.body.get(0) instanceof UniContinue);
		}		
	}

}
