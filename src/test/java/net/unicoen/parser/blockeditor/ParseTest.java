package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class ParseTest {
	
	@Test
	public void test() {
		String file = "hello.xml";
		String filePath = "blockeditor/" + file;
		assertTrue(new File(filePath).exists());
	}
}
