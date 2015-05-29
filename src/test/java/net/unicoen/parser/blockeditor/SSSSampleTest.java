package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBlock;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniWhile;

import org.junit.Test;

public class SSSSampleTest {

	@Test
	public void test() throws IOException {
		UniClassDec classDec = new UniClassDec("SSSSample",  new ArrayList<>(),  new ArrayList<>());

		//modifiers設定
		classDec.modifiers.add("");

		//member設定
		UniMethodDec startMethod = new UniMethodDec("start", new ArrayList<>(), "void", null, new UniBlock());
		startMethod.modifiers.add("");
		classDec.members.add(startMethod);

		List<UniExpr> elements = new ArrayList<>();
		//int i = 1;
		elements.add(UniToBlockTestUtil.createVariableDecModelWithValue("int", "i", "0"));
		//while(cond)
		UniWhile whileModel = UniToBlockTestUtil.createWhieDecModel(UniToBlockTestUtil.createBinOpModel("<", new UniIdent("i"), UniToBlockTestUtil.createLiteral("int", "4")));

		//fd(50)
		List<UniExpr> fdArgs = new ArrayList<>();
		fdArgs.add(UniToBlockTestUtil.createLiteral("int", "50"));
		whileModel.block.body.add(new UniMethodCall(new UniIdent("MyLib"), "fd", fdArgs));

		//rt(90)
		List<UniExpr> rtArgs = new ArrayList<>();
		rtArgs.add(UniToBlockTestUtil.createLiteral("int", "90"));
		whileModel.block.body.add(new UniMethodCall(new UniIdent("MyLib"), "rt", rtArgs));

		//i++;
		whileModel.block.body.add(UniToBlockTestUtil.createUnaryOpModel("_++", new UniIdent("i")));

		elements.add(whileModel);

		//startメソッドにelementsを追加
		startMethod.block.body = elements;

		UniToBlockParser parser = new UniToBlockParser(true);
		parser.setProjectPath("blockeditor/");
		File file = parser.parse(classDec);

		System.out.println("create xml file at " +  file.getAbsolutePath());


	}

}
