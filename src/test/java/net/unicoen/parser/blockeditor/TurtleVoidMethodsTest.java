package net.unicoen.parser.blockeditor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniNode;

import org.junit.Test;

public class TurtleVoidMethodsTest {

	@Test
	public void test() throws UnsupportedEncodingException {
		String file = "TurtleVoidMethods.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);
		List<UniNode> list = ToBlockEditorParser.parse(targetXml);

		UniClassDec dec = new UniClassDec();
		dec.members = new ArrayList<>();
		for (UniNode node : list) {
			dec.members.add((UniFuncDec) node);
		}


		
		UniFuncDec startMethod = (UniFuncDec) dec.members.get(0);

		String[] expecteds = new String[startMethod.block.body.size()];
		expecteds[0] = "fd";
		expecteds[1] = "bk";
		expecteds[2] = "rt";
		expecteds[3] = "lt";
		expecteds[4] = "up";
		expecteds[5] = "down";
		expecteds[6] = "print";
		expecteds[7] = "warp";
		expecteds[8] = "scale";
		expecteds[9] = "size";
		expecteds[10] = "large";
		expecteds[11] = "small";
		expecteds[12] = "wide";
		expecteds[13] = "narrow";
		expecteds[14] = "tall";
		expecteds[15] = "little";
		expecteds[16] = "show";
		expecteds[17] = "hide";
		expecteds[18] = "update";
		expecteds[19] = "sleep";
		
		for (int i = 0; i < startMethod.block.body.size(); i++) {
			assertEquals(expecteds[i], ((UniMethodCall) startMethod.block.body.get(i)).methodName);
		}

	}

}
