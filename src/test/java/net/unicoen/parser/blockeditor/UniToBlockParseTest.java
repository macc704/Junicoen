package net.unicoen.parser.blockeditor;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.interpreter.Engine;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniStringLiteral;

import org.junit.Test;

public class UniToBlockParseTest {

	@Test
	public void test() throws UnsupportedEncodingException {
		// TODO Auto-generated method stub		
		UniClassDec classDec = new UniClassDec();
		classDec.className = "UniToBlockHelloTest";
		
		List<UniMemberDec> member = new ArrayList<UniMemberDec>();
		
		UniFuncDec funcDec = new UniFuncDec();
		funcDec.funcName = "start";
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
		
		
		funcDec.body = new ArrayList<UniExpr>();
		funcDec.body.add(mcall);
		
		member.add(funcDec);
		
		classDec.members = member;
		
		executeTest(classDec, "Hello World" + System.lineSeparator());
		
		UniToBlockParser parser = new UniToBlockParser();
		
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
