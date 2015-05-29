package net.unicoen.parser.blockeditor;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node_helper.Builder;

import org.junit.Test;

public class UniToBlockParseTest {

	@Test
	public void test() throws IOException {
		UniClassDec classDec = new UniClassDec();
		classDec.className = "UniToBlockHelloTest";

		List<UniMemberDec> member = new ArrayList<UniMemberDec>();

		UniMethodDec funcDec = new UniMethodDec();
		funcDec.methodName = "start";
		funcDec.returnType = "void";

		UniMethodCall mcall = new UniMethodCall();
		UniIdent ident = new UniIdent();
		ident.name = "MyLib";
		mcall.receiver = ident;
		mcall.methodName = "print";


		List<UniExpr> params = new ArrayList<>();
		UniStringLiteral str = new UniStringLiteral();
		str.value = "Hello World";
		params.add(str);

		mcall.args = params;

		funcDec.block = Builder.block();
		funcDec.block.body.add(mcall);

		member.add(funcDec);

		classDec.members = member;

		executeTest(classDec, "Hello World" + System.lineSeparator());

		String fileName = classDec.className;
		String filePath = "blockeditor/" + fileName + ".xml";

		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		BlockGenerator parser = new BlockGenerator(out);

		try {
			parser.parse(classDec);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void executeTest(UniClassDec dec, String expect) throws UnsupportedEncodingException{
		Engine engine = new Engine();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		engine.out = new PrintStream(baos);

		engine.execute(dec);
		String output = baos.toString("UTF8");

		assertEquals(expect, output);
	}

}
