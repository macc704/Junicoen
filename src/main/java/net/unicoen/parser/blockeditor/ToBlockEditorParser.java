package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.unicoen.node.UniArg;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniNode;
import net.unicoen.node.UniStringLiteral;

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
			while (true) {
				if (nextNodeId == null)
					break;
				Node next = map.get(nextNodeId);
				// add body
				UniExpr expr = parseToExpr(next, map);
				body.add(expr);
				nextNodeId = getChildText(next, "AfterBlockId");
			}

			d.body = body;
			ret.add(d);
		}
		return ret;
	}

	private static UniExpr parseToExpr(Node node, HashMap<String, Node> map) {
		String blockType = getAttribute(node, "genus-name");
		switch (blockType) {
		case "Turtle-print[@string]":
			// 引数の処理
			Node argsNode = getChildNode(node, "Sockets");
			List<UniExpr> args = new ArrayList<>();
			for (Node argNode : eachChild(argsNode)) {
				assert argNode.getNodeName().equals("BlockConnector");
				String argElemId = getAttribute(argNode, "con-block-id");
				Node realArgNode = map.get(argElemId);
				UniExpr argExpr = parseToExpr(realArgNode, map);
				args.add(argExpr);
			}
			UniMethodCall mcall = new UniMethodCall();
			mcall.args = args;
			UniIdent ident = new UniIdent();
			ident.name = "MyLib";
			mcall.receiver = ident;
			mcall.methodName = "print";
			return mcall;
		case "string":
			String value = getChildText(node, "Label");
			UniStringLiteral lit = new UniStringLiteral();
			lit.value = value;
			return lit;
		}
		return null;
	}

	private static Node getRooteNote(File xmlFile) {
		Document d = toXmlDoc(xmlFile);

		NodeList list = d.getElementsByTagName("PageBlocks");
		if (list.getLength() != 1)
			return null;
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
		}
		return null;
	}

	private static String getAttribute(Node node, String attributeName) {
		assert node != null;
		assert attributeName != null;

		Node attrNode = node.getAttributes().getNamedItem(attributeName);
		if (attrNode == null)
			return null;
		return attrNode.getNodeValue();
	}

	private static Node getChildNode(Node node, String... nodeName) {
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
				};
			}
		};
	}
}
