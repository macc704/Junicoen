package net.unicoen.parser.blockeditor;

import java.util.ArrayList;
import java.util.List;

import net.unicoen.node.UniBinOp;
import net.unicoen.node.UniBlock;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniDoubleLiteral;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniUnaryOp;
import net.unicoen.node.UniVariableDec;
import net.unicoen.node.UniVariableDecWithValue;
import net.unicoen.node.UniWhile;

public class UniToBlockTestUtil {


	public static void addVariableDecModel(String type, String name, List<UniExpr> blocks){
		blocks.add(createVariableDecModel(type, name));
	}


	public static UniVariableDec createVariableDecModel(String type, String name){
		//int 型の変数i作成
		UniVariableDec vdec = new UniVariableDec();
		vdec.name = name;
		vdec.type = type;
		return vdec;
	}

	public static UniVariableDecWithValue createVariableDecModelWithValue(String type, String name, String value){
		//int 型の変数i作成
		UniVariableDecWithValue vdec = new UniVariableDecWithValue(new ArrayList<String>(), type, name, createLiteral(type, value));
		vdec.modifiers.add("");
		return vdec;
	}

	public static UniWhile createWhieDecModel(UniExpr cond){
		UniWhile whileModel = new UniWhile(cond, new UniBlock());
		whileModel.block.body = new ArrayList<>();
		return whileModel;
	}

	public static void createUnaryOpModel(List<UniExpr> blocks, String operator, UniExpr expr){
		blocks.add(createUnaryOpModel(operator, expr));
	}

	public static UniUnaryOp createUnaryOpModel(String operator, UniExpr expr){
		return new UniUnaryOp(operator, expr);
	}

	public static UniBinOp createBinOpModel(String operator, UniExpr left, UniExpr right){
		UniBinOp op = new UniBinOp(operator, left, right);
		return op;
	}

	public static void createBinOpModel(List<UniExpr> blocks, String operator, UniExpr left, UniExpr right){
		UniBinOp op = new UniBinOp(operator, left, right);
		blocks.add(op);
	}


	public static UniExpr createLiteral(String type, String value){
		if("int".equals(type)){
			return new UniIntLiteral(Integer.parseInt(value));
		}else if("String".equals(type)){
			return new UniStringLiteral(value);
		}else if("doube".equals(type)){
			return new UniDoubleLiteral(Double.parseDouble(value));
		}else if("bool".equals(type)){
			return new UniBoolLiteral(Boolean.parseBoolean(value));
		}else{
			throw new RuntimeException("illegal type:" + type);
		}
	}

}
