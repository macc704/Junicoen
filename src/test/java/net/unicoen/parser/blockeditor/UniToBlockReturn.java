package net.unicoen.parser.blockeditor;

import java.io.IOException;
import java.util.ArrayList;

import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniDoubleLiteral;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniReturn;
import net.unicoen.node.UniStringLiteral;

import org.junit.Test;

public class UniToBlockReturn {

	@Test
	public void test() {
		UniClassDec dec = new UniClassDec("UniToBlockReturn", new ArrayList<String>(), new ArrayList<>());

		UniMethodDec voidMethod = UniToBlockTestUtil.createMethod("voidMethod", "void");
		dec.members.add(voidMethod);

		UniMethodDec stringMethod = UniToBlockTestUtil.createMethod("stringMethod", "String");
		stringMethod.block.body.add(new UniReturn(new UniStringLiteral("hoge")));
		dec.members.add(stringMethod);

		UniMethodDec booleanMethod = UniToBlockTestUtil.createMethod("booleanMethod", "boolean");
		booleanMethod.block.body.add(new UniReturn(new UniBoolLiteral(true)));
		dec.members.add(booleanMethod);

		UniMethodDec doubleMethod = UniToBlockTestUtil.createMethod("doubleMethod", "double");
		doubleMethod.block.body.add(new UniReturn(new UniDoubleLiteral(1.0)));
		dec.members.add(doubleMethod);

		UniMethodDec intMethod = UniToBlockTestUtil.createMethod("intMethod", "int");
		intMethod.block.body.add(new UniReturn(new UniIntLiteral(1)));
		dec.members.add(intMethod);

		BlockGenerator generator = new BlockGenerator(true);
		try {
			generator.parse(dec, "blockeditor/");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
