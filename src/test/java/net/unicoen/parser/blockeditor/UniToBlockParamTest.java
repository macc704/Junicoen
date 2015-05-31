package net.unicoen.parser.blockeditor;

import java.io.IOException;
import java.util.ArrayList;

import net.unicoen.node.UniArg;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniMethodDec;

import org.junit.Test;

public class UniToBlockParamTest {

	@Test
	public void test() {
		UniClassDec dec = new UniClassDec("UniToBlockParam", new ArrayList<String>(), new ArrayList<>());

		UniMethodDec voidMethod = UniToBlockTestUtil.createMethod("voidMethod", "void");
		voidMethod.args.add(new UniArg("int", "param"));

		dec.members.add(voidMethod);


		BlockGenerator generator = new BlockGenerator(true);
		try {
			generator.parse(dec, "blockeditor/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
