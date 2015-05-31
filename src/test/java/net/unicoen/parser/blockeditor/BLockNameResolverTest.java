package net.unicoen.parser.blockeditor;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

public class BLockNameResolverTest {

	@Test
	public void test() {
		BlockNameResolver resolver = new BlockNameResolver(true);
		Map<String, String> turtleMethods = resolver.getTurtleMethodsName();

		assertEquals("Turtle", turtleMethods.get("fd[@int]"));
		assertEquals("InputTurtle", turtleMethods.get("toEnglishMode[]"));
		assertEquals("CardTurtle", turtleMethods.get("getText[]"));

	}

}
