package net.unicoen.parser.blockeditor;

import static org.junit.Assert.*;

import java.io.File;

import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodDec;

import org.junit.Test;

public class ReturnTest {

	@Test
	public void test() {
		String file = "Return.xml";
		String filePath = "blockeditor/" + file;
		File targetXml = new File(filePath);

		BlockMapper mapper = new BlockMapper();
		UniClassDec dec = mapper.parse(targetXml);

		for(UniMemberDec method : dec.members){
			if(method instanceof UniMethodDec){
				UniMethodDec m = (UniMethodDec)method;
				if(m.methodName.equals("start")){
					assertEquals("void", m.returnType);
				}else if(m.methodName.equals("returnDouble")){
					assertEquals("double", m.returnType);
				}else if(m.methodName.equals("returnInt")){
					assertEquals("int", m.returnType);
				}else if(m.methodName.equals("returnString")){
					assertEquals("String", m.returnType);
				}else if(m.methodName.equals("returnBoolean")){
					assertEquals("boolean", m.returnType);
				}
			}
		}

	}

}
