package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBlock;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniDoubleLiteral;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniUnaryOp;
import net.unicoen.node.UniVariableDecWithValue;

import org.junit.Test;

public class UniToBlockUnaryTest {

	@Test
	public void test() throws IOException {
		UniClassDec dec = new UniClassDec();
		dec.className = "UniToBlockUnaryOperator";

		List<UniMemberDec> members = new ArrayList<>();


		List<UniExpr> body = new ArrayList<>();

		UniVariableDecWithValue var =  new UniVariableDecWithValue(null, "int", "i", new UniIntLiteral(1));
		body.add(var);

		UniVariableDecWithValue dvar =  new UniVariableDecWithValue(null, "double", "d", new UniDoubleLiteral(1.0));
		body.add(dvar);

		UniUnaryOp op = new UniUnaryOp("_++", new UniIdent(var.name));
		body.add(op);



		UniToBlockTestUtil.createUnaryOpModel(body, "_++", new UniIdent(var.name));
		UniToBlockTestUtil.createUnaryOpModel(body, "_--", new UniIdent(var.name));
		UniToBlockTestUtil.createUnaryOpModel(body, "++_", new UniIdent(var.name));
		UniToBlockTestUtil.createUnaryOpModel(body, "--_", new UniIdent(var.name));

		UniToBlockTestUtil.createBinOpModel(body, "=", new UniIdent(var.name), UniToBlockTestUtil.createUnaryOpModel("_++", new UniIdent(var.name)));
		UniToBlockTestUtil.createBinOpModel(body, "=", new UniIdent(var.name), UniToBlockTestUtil.createUnaryOpModel("_--", new UniIdent(var.name)));
		UniToBlockTestUtil.createBinOpModel(body, "=", new UniIdent(var.name), UniToBlockTestUtil.createUnaryOpModel("++_", new UniIdent(var.name)));
		UniToBlockTestUtil.createBinOpModel(body, "=", new UniIdent(var.name), UniToBlockTestUtil.createUnaryOpModel("--_", new UniIdent(var.name)));

		UniToBlockTestUtil.createUnaryOpModel(body, "_++", new UniIdent(dvar.name));
		UniToBlockTestUtil.createUnaryOpModel(body, "_--", new UniIdent(dvar.name));
		UniToBlockTestUtil.createUnaryOpModel(body, "++_", new UniIdent(dvar.name));
		UniToBlockTestUtil.createUnaryOpModel(body, "--_", new UniIdent(dvar.name));

		UniToBlockTestUtil.createBinOpModel(body, "=", new UniIdent(dvar.name), UniToBlockTestUtil.createUnaryOpModel("_++", new UniIdent(dvar.name)));
		UniToBlockTestUtil.createBinOpModel(body, "=", new UniIdent(dvar.name), UniToBlockTestUtil.createUnaryOpModel("_--", new UniIdent(dvar.name)));
		UniToBlockTestUtil.createBinOpModel(body, "=", new UniIdent(dvar.name), UniToBlockTestUtil.createUnaryOpModel("++_", new UniIdent(dvar.name)));
		UniToBlockTestUtil.createBinOpModel(body, "=", new UniIdent(dvar.name), UniToBlockTestUtil.createUnaryOpModel("--_", new UniIdent(dvar.name)));


		List<UniExpr> args = new ArrayList<>();
		args.add(new UniIdent(var.name));


		UniMethodDec method = new UniMethodDec("start", null, "void", null, new UniBlock(body));
		members.add(method);

		dec.members = members;

		String fileName = dec.className;
		String filePath = "blockeditor/" + fileName + ".xml";

		File file = new File(filePath);
		file.createNewFile();

		PrintStream out = new PrintStream(file);

		BlockGenerator parser = new BlockGenerator(out);
		parser.parse(dec);

	}

}
