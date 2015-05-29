package net.unicoen.generator.blockeditor;

import java.io.PrintStream;

import net.unicoen.node.Traverser;
import net.unicoen.node.UniArg;
import net.unicoen.node.UniBinOp;
import net.unicoen.node.UniBlock;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniBreak;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniContinue;
import net.unicoen.node.UniDoWhile;
import net.unicoen.node.UniDoubleLiteral;
import net.unicoen.node.UniFieldAccess;
import net.unicoen.node.UniFor;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniLongLiteral;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniReturn;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniTernaryOp;
import net.unicoen.node.UniUnaryOp;
import net.unicoen.node.UniVariableDec;
import net.unicoen.node.UniVariableDecWithValue;
import net.unicoen.node.UniWhile;

public class BlockModelGenerator extends Traverser {

	private final PrintStream out;
	private BlockClassModel classModel;


	public BlockModelGenerator(PrintStream out) {
		this.out = out;
	}


	@Override
	public void traverseBoolLiteral(UniBoolLiteral node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseIntLiteral(UniIntLiteral node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseLongLiteral(UniLongLiteral node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseDoubleLiteral(UniDoubleLiteral node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseStringLiteral(UniStringLiteral node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseIdent(UniIdent node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseFieldAccess(UniFieldAccess node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseMethodCall(UniMethodCall node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseUnaryOp(UniUnaryOp node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseBinOp(UniBinOp node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseTernaryOp(UniTernaryOp node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseReturn(UniReturn node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseBreak(UniBreak node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseContinue(UniContinue node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseBlock(UniBlock node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseIf(UniIf node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseFor(UniFor node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseWhile(UniWhile node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseDoWhile(UniDoWhile node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseVariableDec(UniVariableDec node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseVariableDecWithValue(UniVariableDecWithValue node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseMethodDec(UniMethodDec node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseArg(UniArg node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void traverseClassDec(UniClassDec node) {
		

	}

}
