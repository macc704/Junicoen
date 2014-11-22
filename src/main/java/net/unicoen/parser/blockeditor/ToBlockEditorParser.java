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
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniNode;
import net.unicoen.node.UniStringLiteral;
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
			if (name.startsWith("#"))
				continue;

			String nodeId = getAttribute(node, "id");
			String blockType = getAttribute(node, "genus-name");
			map.put(nodeId, node);
			if ("procedure".equals(blockType)) {
				procs.add(node);
			}
		}

		List<UniNode> ret = new ArrayList<>();
		for (Node procNode : procs) {
			UniFuncDec d = new UniFuncDec();
			d.funcName = getChildText(procNode, "Label");
			List<UniExpr> body = new ArrayList<>();

			String nextNodeId = getChildText(procNode, "AfterBlockId");
			if (nextNodeId != null) {
				body = parseBody(map.get(nextNodeId), map);
			}

			d.body = body;
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
				//				String num = getChildText(node, "Label");
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
			}
			break;
		case "command":
			Node argsNode = getChildNode(node, "Sockets");
			List<List<UniExpr>> args = parseSocket(argsNode, map);
			return parseCommand(args, blockType);
		case "function":
			Node functionArgsNode = getChildNode(node, "Sockets");
			List<List<UniExpr>> functionArgs = parseSocket(functionArgsNode, map);
			return parseFunction(functionArgs, blockType);
		}
		throw new RuntimeException("Unsupported node: " + node);
	}

	private static UniExpr parseFunction(List<List<UniExpr>> functionArgs, String blockType) {
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
		} else {
			throw new RuntimeException("Unknown operator type: " + blockType);
		}

		return binOp;
	}

	private static List<List<UniExpr>> parseSocket(Node argsNode, HashMap<String, Node> map) {
		List<List<UniExpr>> args = new ArrayList<>();
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
		return args;
	}

	private static UniExpr parseCommand(List<List<UniExpr>> args, String blockType) {
		if ("ifelse".equals(blockType)) {// if statement
			UniIf uniIf = new UniIf();
			uniIf.cond = args.get(0).get(0);
			if (args.get(1) != null) {
				uniIf.trueBlock = args.get(1);
			}
			if (args.get(2) != null) {
				uniIf.falseBlock = args.get(2);
			}
			return uniIf;
		} else if ("while".equals(blockType)) {// while statement
			UniWhile uniWhile = new UniWhile();
			uniWhile.cond = args.get(0).get(0);
			if (args.get(1) != null) {
				uniWhile.body = args.get(1);
			}
			return uniWhile;
		} else {
			UniMethodCall mcall = getProtoType(blockType);
			if (mcall != null) {
				mcall.args = args.get(0);
				return mcall;
			} else {
				throw new RuntimeException("Unknown method type: " + blockType);
			}
		}
	}

	private static UniMethodCall getProtoType(String blockType) {
		/*
		 * 最初にテーブルを作って、呼ばれるたびに、nodeのクローンを作って返す。
		 */
		switch (blockType) {
		case "Turtle-print[@string]": {
			UniMethodCall mcall = new UniMethodCall();
			mcall.receiver = new UniIdent("MyLib");
			mcall.methodName = "print";
			return mcall;
		}
		case "Turtle-rt[@int]":
			return new UniMethodCall(null, "rt", null);
		case "Turtle-lt[@int]":
			return new UniMethodCall(null, "lt", null);
		case "Turtle-fd[@int]":
			return new UniMethodCall(null, "fd", null);
		case "Turtle-bk[@int]":
			return new UniMethodCall(null, "bk", null);
		}

		throw new RuntimeException("Unknown method call: " + blockType);
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
						// TODO Auto-generated method stub

					}
				};
			}
		};
	}
}
