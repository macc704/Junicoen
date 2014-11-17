package net.unicoen.parser.blockeditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniMemberDec;

public class UniToBlockBasicTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		UniToBlockParser parser = new UniToBlockParser();
		
		UniClassDec classDec = new UniClassDec();
		classDec.className = "Test";
		
		List<UniMemberDec> member = new ArrayList<UniMemberDec>();
		
		UniFuncDec funcDec = new UniFuncDec();
		funcDec.funcName = "start";
		funcDec.returnType = "void";
		member.add(funcDec);
		
		classDec.members = member;
		
		try {
			parser.parse(classDec);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
