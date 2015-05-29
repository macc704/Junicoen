package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.unicoen.node.UniBinOp;
import net.unicoen.node.UniBlock;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniBreak;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniContinue;
import net.unicoen.node.UniDoWhile;
import net.unicoen.node.UniDoubleLiteral;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniReturn;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniUnaryOp;
import net.unicoen.node.UniVariableDec;
import net.unicoen.node.UniVariableDecWithValue;
import net.unicoen.node.UniWhile;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ToBlockEditorParser {

	private static VariableNameResolver variableResolver = new VariableNameResolver();

	public static UniClassDec parse(File xmlFile) {

		UniClassDec classDec = new UniClassDec();
		classDec.className = getPageNameFromXML(xmlFile);
		classDec.modifiers = new ArrayList<String>();
		classDec.modifiers.add("");

		Node pageBlock = getRooteNote(xmlFile);
		HashMap<String, Node> map = new HashMap<>();
		ArrayList<Node> procs = new ArrayList<>();
		for (Node node : eachChild(pageBlock)) {
			String name = node.getNodeName();
			if (name.startsWith("#")) {
				continue;
			} else if (name.equals("BlockStub")) {
				node = getChildNode(node, "Block");
			}

			String nodeId = getAttribute(node, "id");
			String blockType = getAttribute(node, "genus-name");
			map.put(nodeId, node);
			if ("procedure".equals(blockType)) {
				procs.add(node);
			}
		}

		List<UniMemberDec> ret = new ArrayList<>();
		for (Node procNode : procs) {
			UniMethodDec d = new UniMethodDec();
			d.methodName = getChildText(procNode, "Label");
			d.modifiers = new ArrayList<>();
			d.modifiers.add("");
			d.returnType = "void";
			List<UniExpr> body = new ArrayList<>();

			String nextNodeId = getChildText(procNode, "AfterBlockId");
			if (nextNodeId != null) {
				body = parseBody(map.get(nextNodeId), map);
			}

			d.block = new UniBlock(body);
			ret.add(d);
			variableResolver.resetLocalVariables();
		}

		classDec.members = ret;

		return classDec;
	}

	public static String getPageNameFromXML(File xmlFile) {
		Document d = toXmlDoc(xmlFile);

		NodeList list = d.getElementsByTagName("Page");
		if (list == null) {
			throw new RuntimeException("page load error!");
		}

		return getAttribute(list.item(0), "page-name");
	}

	private static List<UniExpr> parseBody(Node bodyNode, HashMap<String, Node> map) {
		List<UniExpr> body = new ArrayList<>();

		body.add(parseToExpr(bodyNode, map));

		String nextNodeId = getChildText(bodyNode, "AfterBlockId");
		while (true) {
			if (nextNodeId == null)
				break;
			Node next = map.get(nextNodeId);
			// add body
			UniExpr expr = parseToExpr(next, map);
			body.add(expr);
			nextNodeId = getChildText(next, "AfterBlockId");
		}

		return body;
	}

	private static UniExpr parseToExpr(Node node, HashMap<String, Node> map) {
		if ("BlockStub".equals(node.getNodeName())) {
			node = ToBlockEditorParser.getChildNode(node, "Block");
		}

		String blockKind = getAttribute(node, "kind");
		// ブロックモデルに応じてJUNICOENの式モデルを生成する
		switch (blockKind) {
		case "data":
			return parseLiteral(node);// リテラルを解析して式モデルを返す
		case "command":
			return parseCommand(node, map);// if，while，メソッドなどを解析して式モデルを返す
		case "function":
			return parseFunction(node, map);// 二項演算，または単項演算を解析して式モデルを返す
		case "local-variable":
			return parseLocalVariable(node, map);// ローカル変数を解析して，式モデルを返す
		default:
			throw new RuntimeException("Unsupported node: " + blockKind);
		}
	}

	private static UniExpr parseLiteral(Node node) {
		String blockGenusName = getAttribute(node, "genus-name");
		if ("number".equals(blockGenusName)) {
			// String num = getChildText(node, "Label");
			UniIntLiteral num = new UniIntLiteral(Integer.parseInt(getChildText(node, "Label")));
			return num;
		} else if ("string".equals(blockGenusName)) {
			UniStringLiteral lit = new UniStringLiteral(getChildText(node, "Label"));
			return lit;
		} else if ("double-number".equals(blockGenusName)) {
			UniDoubleLiteral value = new UniDoubleLiteral(Double.parseDouble(getChildText(node,
					"Label")));
			return value;
		} else if ("true".endsWith(blockGenusName)) {
			UniBoolLiteral trueValue = new UniBoolLiteral();
			trueValue.value = true;
			return trueValue;
		} else if ("false".equals(blockGenusName)) {
			UniBoolLiteral falseValue = new UniBoolLiteral();
			falseValue.value = false;
			return falseValue;
		} else if (blockGenusName.startsWith("getter")) {
			UniIdent ident = new UniIdent(getChildText(node, "Name"));
			return ident;
		} else if (blockGenusName.startsWith("preinc")) {
			UniUnaryOp op = new UniUnaryOp("++_", new UniIdent(getChildText(node, "Name")));
			return op;
		} else if (blockGenusName.startsWith("predec")) {
			UniUnaryOp op = new UniUnaryOp("--_", new UniIdent(getChildText(node, "Name")));
			return op;
		} else if (blockGenusName.startsWith("postinc")) {
			UniUnaryOp op = new UniUnaryOp("_++", new UniIdent(getChildText(node, "Name")));
			return op;
		} else if (blockGenusName.startsWith("postdec")) {
			UniUnaryOp op = new UniUnaryOp("_--", new UniIdent(getChildText(node, "Name")));
			return op;
		} else {
			throw new RuntimeException("not supported data type:" + blockGenusName);
		}
	}

	private static UniExpr parseLocalVariable(Node node, HashMap<String, Node> map) {
		Node initValueNode = getChildNode(node, "Sockets");
		List<List<UniExpr>> initValues = parseSocket(initValueNode, map);

		String type = getChildText(node, "Type");
		String name = getChildText(node, "Name");

		variableResolver.addLocalVariable(getChildText(node, "Name"), node);

		if (initValues.get(0) != null && initValues.get(0).size() > 0) {
			// 初期値あり
			return new UniVariableDecWithValue(null, type, name, initValues.get(0).get(0));
		} else {
			return new UniVariableDec(null, type, name);
		}
	}

	private static UniExpr parseFunction(Node node, HashMap<String, Node> map) {
		Node functionArgsNode = getChildNode(node, "Sockets");
		List<List<UniExpr>> functionArgs = parseSocket(functionArgsNode, map);
		String blockType = getAttribute(node, "genus-name");

		if (isUnaryOp(blockType)) {
			UniUnaryOp unaryOp = new UniUnaryOp();
			unaryOp.operator = "!";
			unaryOp.expr = functionArgs.get(0).get(0);
			return unaryOp;
		}
		// else if(isCast(blockType)){
		// //キャストの変換処理
		// if("toIntFromDouble".equals(blockType) ||
		// "toDoubleFromInt".equals(blockType)){
		// throw new RuntimeException("not supported yet");
		// }else if("toStringFromInt".equals(blockType)){
		// throw new RuntimeException("not supported yet");
		// }else{
		// throw new RuntimeException("not supported yet");
		// }
		// }
		else {
			UniBinOp binOp = new UniBinOp();

			binOp.left = functionArgs.get(0).get(0);
			binOp.right = functionArgs.get(1).get(0);

			if (isEqualsOperator(blockType)) {
				binOp.operator = "==";
			} else if (isNotEqualsOperator(blockType)) {
				binOp.operator = "!=";
			} else if (isLessThanOperator(blockType)) {
				binOp.operator = "<";
			} else if (isLessThanOrEqualOperator(blockType)) {
				binOp.operator = "<=";
			} else if (isGreaterThanOperator(blockType)) {
				binOp.operator = ">";
			} else if (isGreaterThanOrEqualOperator(blockType)) {
				binOp.operator = ">=";
			} else if ("and".equals(blockType)) {
				binOp.operator = "&&";
			} else if ("or".equals(blockType)) {
				binOp.operator = "||";
			} else if (isAddOperator(blockType)) {
				binOp.operator = "+";
			} else if (isDifferenceOperator(blockType)) {
				binOp.operator = "-";
			} else if (isMulOperator(blockType)) {
				binOp.operator = "*";
			} else if (isDivOperator(blockType)) {
				binOp.operator = "/";
			} else if (isRemOperator(blockType)) {
				binOp.operator = "%";
			} else {
				throw new RuntimeException("Unknown operator type: " + blockType);
			}
			return binOp;
		}
	}

	private static boolean isCast(String blockType) {
		return true;
	}

	private static boolean isLessThanOperator(String blockType) {
		return "lessthan".equals(blockType) || "lessthan-double".equals(blockType);
	}

	private static boolean isLessThanOrEqualOperator(String blockType) {
		return "lessthanorequalto".equals(blockType)
				|| "lessthanorequalto-double".equals(blockType);
	}

	private static boolean isGreaterThanOperator(String blockType) {
		return "greaterthan".equals(blockType) || "greaterthan-double".equals(blockType);
	}

	private static boolean isGreaterThanOrEqualOperator(String blockType) {
		return "greaterthanorequalto".equals(blockType)
				|| "greaterthanorequalto-double".equals(blockType);
	}

	private static boolean isEqualsOperator(String blockType) {
		return "equals-number".equals(blockType) || "equals-string".equals(blockType)
				|| "equals-number-double".equals(blockType) || "equals-boolean".equals(blockType);
	}

	private static boolean isNotEqualsOperator(String blockType) {
		return "not-equals-number".equals(blockType) || "not-equals-string".equals(blockType)
				|| "not-equals-number-double".equals(blockType)
				|| "not-equals-boolean".equals(blockType);
	}

	private static boolean isAddOperator(String blockType) {
		return "sum".equals(blockType) || "sum-double".equals(blockType)
				|| "string-append".equals(blockType);
	}

	private static boolean isDifferenceOperator(String blockType) {
		return "difference".equals(blockType) || "difference-double".endsWith(blockType);
	}

	private static boolean isMulOperator(String blockType) {
		return "product".equals(blockType) || "product-double".endsWith(blockType);
	}

	private static boolean isDivOperator(String blockType) {
		return "quotient".equals(blockType) || "quotient-double".endsWith(blockType);
	}

	private static boolean isRemOperator(String blockType) {
		return "remainder".equals(blockType) || "remainder-double".endsWith(blockType);
	}

	private static boolean isUnaryOp(String blockType) {
		if ("not".equals(blockType)) {
			return true;
		} else {
			return false;
		}
	}

	private static List<List<UniExpr>> parseSocket(Node argsNode, HashMap<String, Node> map) {
		List<List<UniExpr>> args = new ArrayList<>();
		if (argsNode != null) {
			for (Node argNode : eachChild(argsNode)) {
				assert argNode.getNodeName().equals("BlockConnector");
				String argElemId = getAttribute(argNode, "con-block-id");
				Node realArgNode = map.get(argElemId);
				// 先読みして，afterを持ってなかったらexpression
				if (realArgNode != null) {
					if (getChildText(realArgNode, "BeforeBlockId") == null) {
						List<UniExpr> arg = new ArrayList<>();
						arg.add(parseToExpr(realArgNode, map));
						args.add(arg);
					} else {
						args.add(parseBody(realArgNode, map));
					}
				} else {
					// con-block-id がnullの場合は，空
					args.add(null);
				}
			}
		} else {
			List<UniExpr> arg = new ArrayList<>();
			args.add(arg);
		}
		return args;
	}

	private static UniIf parseIfBlock(Node node, HashMap<String, Node> map){
		Node argsNode = getChildNode(node, "Sockets");
		List<List<UniExpr>> args = parseSocket(argsNode, map);

		if(args.get(2) != null){
			return new UniIf(args.get(0).get(0), (UniBlock) args.get(1), null);
		}else{
			return new UniIf(args.get(0).get(0), (UniBlock) args.get(1), (UniBlock) args.get(2));
		}
	}

	private static UniExpr parseCommand(Node node, HashMap<String, Node> map) {
		Node argsNode = getChildNode(node, "Sockets");
		List<List<UniExpr>> args = parseSocket(argsNode, map);
		String blockGenusName = getAttribute(node, "genus-name");//ブロックの種類名を取得
		//BlockModelを解析して，UniversalModelを生成する
		String methodName = getChildText(node, "Name");

		if ("ifelse".equals(blockGenusName)) {
			return parseIfBlock(node, map);
		} else if ("while".equals(blockGenusName)) {
			UniWhile uniWhile = new UniWhile();
			uniWhile.cond = args.get(0).get(0);
			if (args.get(1) != null) {
				uniWhile.block = new UniBlock(args.get(1));
			}
			return uniWhile;
		} else if ("dowhile".equals(blockGenusName)) {
			UniDoWhile uniDoWhile = new UniDoWhile();
			uniDoWhile.block = new UniBlock(args.get(0));
			uniDoWhile.cond = args.get(1).get(0);
			return uniDoWhile;
		} else if ("continue".equals(blockGenusName)) {
			return new UniContinue();
		} else if ("break".equals(blockGenusName)) {
			return new UniBreak();
		} else if ("return".equals(blockGenusName)) {
			UniReturn uniReturn = new UniReturn();
			if (args.size() == 1) {
				uniReturn.value = args.get(0).get(0);
			}
			return uniReturn;
		} else if (blockGenusName.startsWith("setter")) {
			if (variableResolver.getVariableNode(methodName) != null || args.size() == 1) {
				// 代入式
				UniBinOp op = new UniBinOp("=", new UniIdent(methodName), args.get(0).get(0));
				return op;
			} else {
				throw new RuntimeException("illegal setter");
			}
		} else {
			UniMethodCall mcall = getProtoType(methodName);
			if (mcall != null) {
				mcall.args = args.get(0);
				return mcall;
			} else {
				throw new RuntimeException("Unknown method type: " + blockGenusName);
			}
		}
	}

	private static UniMethodCall getProtoType(String methodName) {
		/*
		 * 最初にテーブルを作って、呼ばれるたびに、nodeのクローンを作って返す。
		 */
		if (methodName != null) {
			UniMethodCall mcall = new UniMethodCall(new UniIdent("MyLib"), methodName, null);
			return mcall;
		} else {
			throw new RuntimeException("method name is null");
		}
	}

	private static Node getRooteNote(File xmlFile) {
		Document d = toXmlDoc(xmlFile);

		NodeList list = d.getElementsByTagName("PageBlocks");
		if (list.getLength() != 1)
			throw new RuntimeException("Root node must be one.");
		Node pageBlock = list.item(0);
		return pageBlock;
	}

	private static Document toXmlDoc(File xmlFile) {
		DOMParser dParser = new DOMParser();
		InputSource src = new InputSource();
		try (InputStream in = new FileInputStream(xmlFile)) {
			src.setByteStream(in);
			dParser.parse(src);
			return dParser.getDocument();
		} catch (SAXException | IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Fail to parse", e);
		}
	}

	public static String getAttribute(Node node, String attributeName) {
		assert node != null;
		assert attributeName != null;

		Node attrNode = node.getAttributes().getNamedItem(attributeName);
		if (attrNode == null)
			return null;
		return attrNode.getNodeValue();
	}

	public static Node getChildNode(Node node, String... nodeName) {
		outer: for (int depth = 0; depth < nodeName.length; depth++) {
			for (Node item : eachChild(node)) {
				if (item.getNodeName().equals(nodeName[depth])) {
					node = item;
					continue outer;
				}
			}
			return null;
		}
		return node;
	}

	private static String getChildText(Node node, String... nodeName) {
		Node foundNode = getChildNode(node, nodeName);
		if (foundNode == null)
			return null;

		return foundNode.getTextContent();
	}

	private static Iterable<Node> eachChild(Node node) {
		final NodeList list = node.getChildNodes();
		final int length = list.getLength();
		return new Iterable<Node>() {

			@Override
			public Iterator<Node> iterator() {
				return new Iterator<Node>() {

					private int index = -1;

					@Override
					public Node next() {
						index = nextIndex();
						return list.item(index);
					}

					@Override
					public boolean hasNext() {
						return nextIndex() >= 0;
					}

					private int nextIndex() {
						for (int start = index + 1; start < length; start++) {
							Node node = list.item(start);
							if (node.getNodeName().startsWith("#"))
								continue;
							return start;
						}
						return -1;
					}

					@Override
					public void remove() {
					}
				};
			}
		};
	}
}
