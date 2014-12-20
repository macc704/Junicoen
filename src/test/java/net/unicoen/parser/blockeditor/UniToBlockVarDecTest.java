package net.unicoen.parser.blockeditor;

import java.io.IOException;

import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class UniToBlockVarDecTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = UniToBlockWhileTest.parseClass("VarDec");
		UniToBlockParser parser = new UniToBlockParser();
		parser.parse(dec);
		
	}

}
