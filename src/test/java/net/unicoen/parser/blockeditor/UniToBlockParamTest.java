package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import net.unicoen.node.UniArg;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniMethodDec;

import org.junit.Test;

public class UniToBlockParamTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = new UniClassDec("UniToBlockParam", new ArrayList<String>(), new ArrayList<>());

		UniMethodDec voidMethod = UniToBlockTestUtil.createMethod("voidMethod", "void");
		voidMethod.args.add(new UniArg("int", "param"));

		dec.members.add(voidMethod);

		File file = new File("blockeditor/" + dec.className + ".xml");
		file.createNewFile();
		PrintStream out = new PrintStream(file);

		BlockGenerator generator = new BlockGenerator(out);
	}

}
