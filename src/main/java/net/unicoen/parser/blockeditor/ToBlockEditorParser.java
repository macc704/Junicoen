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
import net.unicoen.node.UniContinue;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniNode;
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

	public static List<UniNode> parse(File xmlFile) {
		Node pageBlock = getRooteNote(xmlFile);
		HashMap<String, Node> map = new HashMap<>();
		ArrayList<Node> procs = new ArrayList<>();
		for (Node node : eachChild(pageBlock)) {
			String name = node.getNodeName();
			if (name.startsWith("#")){
				continue;
			}else if(name.equals("BlockStub")){
				node = getChildNode(node, "Block");
			}

			String nodeId = getAttribute(node, "id");
			String blockType = getAttribute(node, "genus-name");
			map.put(nodeId, node);
			if ("procedure".equals(blockType)) {
				procs.add(node);
			}
		}

		List<UniNode> ret = new ArrayList<>();
		for (Node procNode : procs) {
			UniMethodDec d = new UniMethodDec();
			d.methodName = getChildText(procNode, "Label");
			List<UniExpr> body = new ArrayList<>();

			String nextNodeId = getChildText(procNode, "AfterBlockId");
			if (nextNodeId != null) {
				body = parseBody(map.get(nextNodeId), map);
			}

			d.block = new UniBlock(body);
			ret.add(d);
		}
		return ret;
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
		String blockKind = getAttribute(node, "kind");
		String blockType = getAttribute(node, "genus-name");
		switch (blockKind) {
		case "procedure":
			break;
		case "data":
			switch (blockType) {
			case "number":
				// String num = getChildText(node, "Label");
				UniIntLiteral num = new UniIntLiteral();
				num.value = Integer.parseInt(getChildText(node, "Label"));
				return num;
			case "string":
				String value = getChildText(node, "Label");
				UniStringLiteral lit = new UniStringLiteral();
				lit.value = value;
				return lit;
			case "true":
				UniBoolLiteral trueValue = new UniBoolLiteral();
				trueValue.value = true;
				return trueValue;
			case "false":
				UniBoolLiteral falseValue = new UniBoolLiteral();
				falseValue.value = false;
				return falseValue;
			default:
				throw new RuntimeException("not supported data type:" + blockType);
			}
		case "command":
			Node argsNode = getChildNode(node, "Sockets");
			List<List<UniExpr>> args = parseSocket(argsNode, map);
			String methodName = getChildText(node, "Name");
			return parseCommand(args, blockType, methodName);
		case "function":
			Node functionArgsNode = getChildNode(node, "Sockets");
			List<List<UniExpr>> functionArgs = parseSocket(functionArgsNode, map);
			return parseFunction(functionArgs, blockType);
		case "local-variable":
			Node initValueNode = getChildNode(node, "Sockets");
			List<List<UniExpr>> initValues = parseSocket(initValueNode, map);
			return parseLocalVariable(initValues, getChildText(node, "Type"), getChildText(node, "Name"));
		}
		throw new RuntimeException("Unsupported node: " + node);
	}

	private static UniExpr parseLocalVariable(List<List<UniExpr>> initValues, String type, String name){
		if(initValues.get(0)!=null){
			//初期値あり
			return new UniVariableDecWithValue(null, type, name, initValues.get(0).get(0));
		}else{
			return new UniVariableDec(null, type, name);
		}
	}

	private static UniExpr parseFunction(List<List<UniExpr>> functionArgs, String blockType) {
		if(isUnaryOp(blockType)){
			UniUnaryOp unaryOp = new UniUnaryOp();
			unaryOp.operator = "!";
			unaryOp.expr = functionArgs.get(0).get(0);
			return unaryOp;
		}else{
			UniBinOp binOp = new UniBinOp();

			binOp.left = functionArgs.get(0).get(0);
			binOp.right = functionArgs.get(1).get(0);

			if ("equals-boolean".equals(blockType)) {
				binOp.operator = "==";
			} else if ("lessthan".equals(blockType)) {
				binOp.operator = "<";
			} else if ("and".equals(blockType)) {
				binOp.operator = "&&";
			} else if ("or".equals(blockType)) {
				binOp.operator = "||";
			}else {
				throw new RuntimeException("Unknown operator type: " + blockType);
			}
			return binOp;
		}
	}

	private static boolean isUnaryOp(String blockType){
		if("not".equals(blockType)){
			return true;
		}else{
			return false;
		}
	}

	private static List<List<UniExpr>> parseSocket(Node argsNode, HashMap<String, Node> map) {
		List<List<UniExpr>> args = new ArrayList<>();
		if(argsNode != null){
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
		}else{
			List<UniExpr> arg = new ArrayList<>();
			args.add(arg);
		}
		return args;
	}

	private static UniExpr parseCommand(List<List<UniExpr>> args, String blockType, String methodName) {
		switch (blockType) {
		case "ifelse": {
			UniIf uniIf = new UniIf();
			uniIf.cond = args.get(0).get(0);
			if (args.get(1) != null) {
				uniIf.trueBlock = new UniBlock(args.get(1));
			}
			if (args.get(2) != null) {
				uniIf.falseBlock = new UniBlock(args.get(2));
			}
			return uniIf;
		}
		case "while": {
			UniWhile uniWhile = new UniWhile();
			uniWhile.cond = args.get(0).get(0);
			if (args.get(1) != null) {
				uniWhile.block = new UniBlock(args.get(1));
			}
			return uniWhile;
		}
		case "continue":{
			return new UniContinue();
		}
		case "break":{
			return new UniBreak();
		}
		case "return":{
			UniReturn uniReturn = new UniReturn();
			if(args.size() == 1){
				uniReturn.value = args.get(0).get(0);
			}
		}
		default: {
			UniMethodCall mcall = getProtoType(methodName);
			if (mcall != null) {
				mcall.args = args.get(0);
				return mcall;
			} else {
				throw new RuntimeException("Unknown method type: " + blockType);
			}
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
//		switch (blockType) {
//		case "Turtle-print[@string]": {
//			UniMethodCall mcall = new UniMethodCall();

//			mcall.methodName = "print";
//			return mcall;
//		}
//		case "Turtle-rt[@int]":
//			return new UniMethodCall(null, "rt", null);
//		case "Turtle-lt[@int]":
//			return new UniMethodCall(null, "lt", null);
//		case "Turtle-fd[@int]":
//			return new UniMethodCall(null, "fd", null);
//		case "Turtle-bk[@int]":
//			return new UniMethodCall(null, "bk", null);
//		}
//
//		throw new RuntimeException("Unknown method call: " + blockType);

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
