package net.unicoen.parser.blockeditor;

import java.io.IOException;

import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class UniToBlockNotOperationTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = UniToBlockWhileTest.parseClass("NotOperator");
		
		UniToBlockParser parser = new UniToBlockParser();
		parser.parse(dec);
	}

}
