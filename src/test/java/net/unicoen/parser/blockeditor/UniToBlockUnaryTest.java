package net.unicoen.parser.blockeditor;

import java.io.IOException;

import net.unicoen.node.UniClassDec;

import org.junit.Test;

public class UniToBlockUnaryTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = UniToBlockWhileTest.parseClass("Break");
		
		UniToBlockParser parser = new UniToBlockParser();
		parser.parse(dec);

		
		UniClassDec contDec = UniToBlockWhileTest.parseClass("Continue");
		parser.parse(contDec);

	}

}
