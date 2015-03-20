package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.unicoen.node.UniBinOp;
import net.unicoen.node.UniBlock;
import net.unicoen.node.UniBoolLiteral;
import net.unicoen.node.UniBreak;
import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniContinue;
import net.unicoen.node.UniDoWhile;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFieldAccess;
import net.unicoen.node.UniFor;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniLongLiteral;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniMethodDec;
import net.unicoen.node.UniNode;
import net.unicoen.node.UniReturn;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniUnaryOp;
import net.unicoen.node.UniVariableDec;
import net.unicoen.node.UniVariableDecWithValue;
import net.unicoen.node.UniWhile;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class UniToBlockParser {

	/** Name space definition */
	private static String XML_CODEBLOCKS_NS = "http://education.mit.edu/openblocks/ns";

	/** Location of schema */
	private static String XML_CODEBLOCKS_SCHEMA_URI = "http://education.mit.edu/openblocks/codeblocks.xsd";

	private long ID_COUNTER = 1000;

	private BlockNameResolver resolver;

	private static Map<String, Element> addedModels = new HashMap<String, Element>();

	public UniToBlockParser() {
		resolver = new BlockNameResolver();
	}

	public static Map<String, Element> getAddedModels() {
		return addedModels;
	}

	public void parse(UniClassDec classDec) throws IOException {
		// クラス名のxmlファイルを作成する
		addedModels.clear(); // cashクリア

		File file = new File("blockEditor/" + classDec.className + ".xml");

		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			fileWriter.write(getSaveString(classDec));
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}

	public void parseClass(UniClassDec classDec, Document document, Element pageElement) {
		for (UniMemberDec member : classDec.members) {
			if (member instanceof UniMethodDec) {
				parseFunctionDec((UniMethodDec) member, document, pageElement);
			}
		}
	}

	public List<Element> getFunctionNodes(UniMethodDec funcDec, Document document,
			Element pageElement) {
		List<Element> blocks = new ArrayList<Element>();
		Element procedureElement = createBlockElement(document, "procedure", ID_COUNTER++,
				"procedure");

		addLabelElement(document, funcDec.methodName, procedureElement);
		addLocationElement(document, "50", "50", procedureElement);

		blocks.add(procedureElement);
		if (funcDec.args != null) {
			throw new RuntimeException("function args parsing has not been supported yet");
		}

		// funcDec.body ボディのパース
		blocks = parseBody(document, procedureElement, funcDec.block);

		blocks.add(blocks.size(), procedureElement);
		blocks.remove(0);

		for (Element element : blocks) {
			pageElement.appendChild(element);
		}
		// メソッドノード登録
		addedModels.put(Integer.toString(((UniNode) funcDec).hashCode()), procedureElement);

		return blocks;
	}

	public List<Element> parseBody(Document document, Element parentElement, UniBlock block) {
		if (block == null) {
			return new ArrayList<>(); // can be Collections.emptyList();
		}
		List<UniExpr> body = block.body;
		if (body == null) {
			return new ArrayList<>(); // can be Collections.emptyList();
		}

		List<Element> blocks = new ArrayList<Element>();
		String beforeId = parentElement.getAttribute("id");
		for (int i = 0; i < body.size(); i++) {
			UniExpr expr = body.get(i);
			List<Element> elements = parseExpr(expr, document, null);

			if (i + 1 < body.size()) {
				addAfterBlockNode(document, elements.get(0), String.valueOf(ID_COUNTER));
			}
			addBeforeBlockNode(document, elements.get(0), beforeId);

			blocks.addAll(elements);

			beforeId = blocks.get(blocks.size() - elements.size()).getAttribute("id");
		}
		return blocks;
	}

	public void addBlockElements(List<Element> blocks, List<Element> elements) {
		// 抽象化ブロックだけ順番が逆...
		if (elements.get(0).getAttribute("genus-name").equals("abstraction")) {
			Collections.reverse(elements);
		}
		blocks.addAll(elements);
	}

	public void parseFunctionDec(UniMethodDec funcDec, Document document, Element pageElement) {
		List<Element> blocks = new ArrayList<Element>();
		Element procedureElement = createBlockElement(document, "procedure", ID_COUNTER++,
				"procedure");

		addLabelElement(document, funcDec.methodName, procedureElement);
		addLocationElement(document, "50", "50", procedureElement);

		blocks.add(procedureElement);
		if (funcDec.args != null) {
			throw new RuntimeException("function args parsing has not been supported yet");
		}

		// funcDec.body ボディのパース
		if (funcDec.block != null && funcDec.block.body != null) {
			addAfterBlockNode(document, blocks.get(blocks.size() - 1), String.valueOf(ID_COUNTER));
			String beforeId = procedureElement.getAttribute("id");
			List<UniExpr> body = funcDec.block.body;
			for (int i = 0; i < body.size(); i++) {
				// expressionの解析 行き掛け順
				UniExpr expr = body.get(i);
				List<Element> elements = parseExpr(expr, document, null);// 木で返す．
				// 木の最上部が左辺ブロックになる　左辺ブロックに次のブロックのidをセットする
				if (i + 1 < body.size()) {
					addAfterBlockNode(document, elements.get(0), String.valueOf(ID_COUNTER));
				}

				// 左辺ブロックに直前のブロックのIDを追加する
				addBeforeBlockNode(document, elements.get(0), beforeId);

				addBlockElements(blocks, elements);

				beforeId = blocks.get(blocks.size() - elements.size()).getAttribute("id");
			}
		}

		for (Element element : blocks) {
			pageElement.appendChild(element);
		}
	}

	public static void addAfterBlockNode(Document document, Element blockNode, String id) {
		Element element = document.createElement("AfterBlockId");
		element.setTextContent(id);

		blockNode.appendChild(element);
	}

	public List<Element> parseExpr(UniExpr expr, Document document, Element parent) {
		if (expr instanceof UniMethodCall) {
			return parseMethodCall((UniMethodCall) expr, document, parent);
		} else if (expr instanceof UniStringLiteral) {
			return parseStringLiteral((UniStringLiteral) expr, document, parent);
		} else if (expr instanceof UniIntLiteral) {
			return parseIntLiteral((UniIntLiteral) expr, document, parent);
		} else if (expr instanceof UniBoolLiteral) {
			return parseBoolLiteral((UniBoolLiteral) expr, document, parent);
		} else if (expr instanceof UniIf) {
			return parseIf((UniIf) expr, document, parent);
		} else if (expr instanceof UniWhile) {
			return parseWhile((UniWhile) expr, document, parent);
		} else if (expr instanceof UniBinOp) {
			return parseBinOp((UniBinOp) expr, document, parent);
		} else if (expr instanceof UniBreak) {
			return parseContinueBreak("break", document, parent);
		} else if (expr instanceof UniContinue) {
			return parseContinueBreak("continue", document, parent);
		} else if (expr instanceof UniUnaryOp) {
			return parseUnaryOperator((UniUnaryOp) expr, document, parent);
		} else if (expr instanceof UniVariableDec) {
			return parseVarDec(((UniVariableDec) expr).type, ((UniVariableDec) expr).name,
					document, parent);
		} else if (expr instanceof UniVariableDecWithValue) {
			return parseVarDec((UniVariableDecWithValue) expr, document, parent);
		} else if (expr instanceof UniIdent) {
			throw new RuntimeException("The expr has not been supported yet");
		} else if (expr instanceof UniLongLiteral) {
			throw new RuntimeException("The expr has not been supported yet");
		} else if (expr instanceof UniReturn) {
			return parseReturn((UniReturn) expr, document, parent);
		} else if (expr instanceof UniFieldAccess) {
			throw new RuntimeException("The expr has not been supported yet");
		} else if (expr instanceof UniBlock) {
			return parseBlock((UniBlock) expr, document, parent);
		} else if (expr instanceof UniDoWhile) {
			return parseDoWhile((UniDoWhile) expr, document, parent);
		} else if (expr instanceof UniFor) {
			return parseFor((UniFor) expr, document, parent);
		} else {
			throw new RuntimeException("The expr has not been supported yet");
		}
	}

	public List<Element> parseFor(UniFor forExpr, Document document, Node parent) {
		// whileに変換
		return null;
	}

	public List<Element> parseDoWhile(UniDoWhile doWhileExpr, Document document, Node parent) {
		//
		return null;
	}

	public List<Element> parseBlock(UniBlock blockExpr, Document document, Node parent) {
		Element blockElement = createBlockElement(document, "abstraction", ID_COUNTER++, "command");
		List<Element> elements = parseBody(document, blockElement, blockExpr);
		Map<String, String[]> socketsInfo = calcSocketsInfo(resolver.getSocketNodes("abstraction"));

		List<Element> bodyTopElement = new ArrayList<>();
		bodyTopElement.add(elements.get(0));

		addSocketsNode(bodyTopElement, document, blockElement, socketsInfo.get("connector-type"),
				socketsInfo.get("position-type"), socketsInfo.get("label"));
		elements.addAll(elements);
		elements.add(0, blockElement);

		return elements;
	}

	public List<Element> parseReturn(UniReturn returnExpr, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement = createBlockElement(document, "return", ID_COUNTER++, "command");

		if (returnExpr != null) {
			List<Element> returnValue = parseExpr(returnExpr.value, document, blockElement);

			Map<String, String[]> socketsInfo = calcSocketsInfo(resolver.getSocketNodes("return"));

			// socketの追加
			addSocketsNode(returnValue, document, blockElement, socketsInfo.get("connector-type"),
					socketsInfo.get("position-type"), socketsInfo.get("label"));

			elements.addAll(returnValue);
		}

		elements.add(0, blockElement);
		return elements;
	}

	public List<Element> parseVarDec(UniVariableDecWithValue varDec, Document document, Node parent) {
		List<Element> elements = parseVarDec(varDec.type, varDec.name, document, parent);
		// 初期値のパース
		List<Element> initializer = parseExpr(varDec.value, document, elements.get(0));

		List<Node> socketNodes = resolver
				.getSocketNodes(elements.get(0).getAttribute("genus-name"));
		Map<String, String[]> sockets = calcSocketsInfo(socketNodes);

		addSocketsNode(initializer, document, elements.get(0), sockets.get("connector-type"),
				sockets.get("position-type"), sockets.get("label"));

		return elements;
	}

	public Map<String, String[]> calcSocketsInfo(List<Node> socketNodes) {
		if (socketNodes.size() <= 0) {
			return null;
		} else {
			Map<String, String[]> socketsInfo = new HashMap<>();
			String[] socketLabels = new String[socketNodes.size()];
			String[] socketTypes = new String[socketNodes.size()];
			String[] socketPositionTypes = new String[socketNodes.size()];

			for (int i = 0; i < socketNodes.size(); i++) {
				socketLabels[i] = ToBlockEditorParser.getAttribute(socketNodes.get(i), "label");
				socketTypes[i] = ToBlockEditorParser.getAttribute(socketNodes.get(i),
						"connector-type");
				socketPositionTypes[i] = ToBlockEditorParser.getAttribute(socketNodes.get(i),
						"position-type");
			}

			socketsInfo.put("label", socketLabels);
			socketsInfo.put("connector-type", socketTypes);
			socketsInfo.put("position-type", socketPositionTypes);

			return socketsInfo;
		}
	}

	public Map<String, String[]> calcSocketsInfoByElement(List<Element> socketNodes) {
		if (socketNodes.size() <= 0) {
			return null;
		} else {
			Map<String, String[]> socketsInfo = new HashMap<>();
			String[] socketLabels = new String[socketNodes.size()];
			String[] socketTypes = new String[socketNodes.size()];
			String[] socketPositionTypes = new String[socketNodes.size()];

			for (int i = 0; i < socketNodes.size(); i++) {
				socketLabels[i] = ToBlockEditorParser.getAttribute(socketNodes.get(i), "label");
				socketTypes[i] = ToBlockEditorParser.getAttribute(socketNodes.get(i),
						"connector-type");
				socketPositionTypes[i] = ToBlockEditorParser.getAttribute(socketNodes.get(i),
						"position-type");
			}

			socketsInfo.put("label", socketLabels);
			socketsInfo.put("connector-type", socketTypes);
			socketsInfo.put("position-type", socketPositionTypes);

			return socketsInfo;
		}
	}

	public Map<String, String> calcPlugInfo(Node plugNode) {
		if (plugNode == null) {
			return null;
		} else {
			Map<String, String> socketsInfo = new HashMap<>();

			String plugLabel;
			String socketTypes;
			String socketPositionTypes;

			plugLabel = ToBlockEditorParser.getAttribute(plugNode, "label");
			socketTypes = ToBlockEditorParser.getAttribute(plugNode, "connector-type");
			socketPositionTypes = ToBlockEditorParser.getAttribute(plugNode, "position-type");

			socketsInfo.put("label", plugLabel);
			socketsInfo.put("connector-type", socketTypes);
			socketsInfo.put("position-type", socketPositionTypes);

			return socketsInfo;
		}
	}

	public List<Element> parseVarDec(String type, String name, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement;

		blockElement = createBlockElement(document, "local-var-" + convertBlockTypeName(type),
				ID_COUNTER++, "local-var");

		addLabelElement(document, name, blockElement);
		addTypeElement(document, type, blockElement);

		elements.add(blockElement);
		return elements;
	}

	public String convertBlockTypeName(String type) {
		switch (type) {
		case "int":
			return "int-number";
		case "double":
			return "double-number";
		case "String":
			return type.toLowerCase();
		case "boolean":
			return type;
		default:
			return type.toLowerCase();
		}
	}

	public String convertTypeToBlockConnectorType(String type) {
		switch (type) {
		case "int":
			return "number";
		case "double":
			return "double-number";
		case "String":
			return type.toLowerCase();
		case "boolean":
			return type;
		default:
			return "object";
		}
	}

	public List<Element> parseUnaryOperator(UniUnaryOp uniOp, Document document, Node parent) {
		// !,++,--,+,-
		switch (uniOp.operator) {
		case "!":
			List<Element> elements = new ArrayList<Element>();
			Element blockElement = createBlockElement(document, "not", ID_COUNTER++, "function");
			addPlugElement(document, blockElement, parent, "boolean", "single");

			List<Element> args = new ArrayList<Element>();
			args = parseExpr(uniOp.expr, document, blockElement);

			elements.add(blockElement);
			elements.addAll(args);

			List<Node> socketNodes = resolver.getSocketNodes("not");
			Map<String, String[]> sockets = calcSocketsInfo(socketNodes);
			addSocketsNode(args, document, elements.get(0), sockets.get("connector-type"),
					sockets.get("position-type"), sockets.get("label"));

			return elements;
		default:
			throw new RuntimeException("not supported unary operator");
		}
	}

	public List<Element> parseContinueBreak(String name, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		elements.add(createBlockElement(document, name, ID_COUNTER++, "command"));
		return elements;
	}

	public List<Element> parseBinOp(UniBinOp binopExpr, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement;

		if (binopExpr.operator.equals("&&")) {
			blockElement = createBlockElement(document, "and", ID_COUNTER++, "function");
		} else if (binopExpr.operator.equals("||")) {
			blockElement = createBlockElement(document, "or", ID_COUNTER++, "function");
		} else {
			throw new RuntimeException("unequipment operator");
		}
		Node plugNode = resolver.getPlugElement(blockElement.getAttribute("genus-name"));

		addPlugElement(document, blockElement, parent,
				ToBlockEditorParser.getAttribute(plugNode, "connector-type"),
				ToBlockEditorParser.getAttribute(plugNode, "position-type"));

		List<Element> leftBlocks = parseExpr(binopExpr.left, document, blockElement);
		List<Element> rightBlocks = parseExpr(binopExpr.right, document, blockElement);

		List<Element> args = new ArrayList<Element>();
		args.add(leftBlocks.get(0));
		args.add(rightBlocks.get(0));

		elements.add(blockElement);

		elements.addAll(leftBlocks);
		elements.addAll(rightBlocks);

		List<Node> socketNodes = resolver.getSocketNodes(blockElement.getAttribute("genus-name"));
		Map<String, String[]> sockets = calcSocketsInfo(socketNodes);
		addSocketsNode(args, document, elements.get(0), sockets.get("connector-type"),
				sockets.get("position-type"), sockets.get("label"));

		addedModels.put(Integer.toString(((UniNode) binopExpr).hashCode()), blockElement);

		return elements;
	}

	public static String[] createStringArray(String... labels) {
		String[] socketLabels = new String[labels.length];
		for (int i = 0; i < labels.length; i++) {
			socketLabels[i] = labels[i];
		}

		return socketLabels;
	}

	public List<Element> parseWhile(UniWhile whileExpr, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement = createBlockElement(document, "while", ID_COUNTER++, "command");

		List<Element> sockets = parseExpr(whileExpr.cond, document, blockElement);
		List<Element> trueBlock = parseBody(document, blockElement, whileExpr.block);

		List<Element> blockSockets = new ArrayList<>();
		// socket will have an element
		blockSockets.add(sockets.get(0));

		if (trueBlock.size() > 0) {
			blockSockets.add(trueBlock.get(0));
		} else {
			blockSockets.add(null);
		}

		// genusnameからソケットのラベルとポジションタイプを作成
		List<Node> whileSocketNodes = resolver.getSocketNodes("while");
		Map<String, String[]> socketsInfo = calcSocketsInfo(whileSocketNodes);

		// ソケットの出力
		addSocketsNode(blockSockets, document, blockElement, socketsInfo.get("connector-type"),
				socketsInfo.get("position-type"), socketsInfo.get("label"));

		elements.add(blockElement);
		elements.addAll(sockets);
		elements.addAll(trueBlock);

		addedModels.put(Integer.toString(((UniNode) whileExpr).hashCode()), blockElement);

		return elements;
	}

	public List<Element> parseBoolLiteral(UniBoolLiteral boolexpr, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		if (boolexpr.value) {
			elements.add(createLiteralElement(document, parent, "true",
					ToBlockEditorParser.getAttribute(resolver.getBlockNode("true"), "initlabel"),
					boolexpr.hashCode()));
		} else {
			elements.add(createLiteralElement(document, parent, "false",
					ToBlockEditorParser.getAttribute(resolver.getBlockNode("false"), "initlabel"),
					boolexpr.hashCode()));
		}

		return elements;
	}

	public Element createLiteralElement(Document document, Node parent, String genusName,
			String label, int hash) {
		Element blockElement = createBlockElement(document, genusName, ID_COUNTER++,
				ToBlockEditorParser.getAttribute(resolver.getBlockNode(genusName), "kind"));

		addLabelElement(document, label, blockElement);

		Node plugNode = resolver.getPlugElement(genusName);
		Map<String, String> plugInfo = calcPlugInfo(plugNode);

		addPlugElement(document, blockElement, parent, plugInfo.get("connector-type"),
				plugInfo.get("position-type"));

		addedModels.put(Integer.toString(hash), blockElement);

		return blockElement;
	}

	public List<Element> parseIf(UniIf ifexpr, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement = createBlockElement(document, "ifelse", ID_COUNTER++, "command");

		List<Element> sockets = parseExpr(ifexpr.cond, document, blockElement);
		List<Element> trueBlock = parseBody(document, blockElement, ifexpr.trueBlock);
		List<Element> falseBlock = parseBody(document, blockElement, ifexpr.falseBlock);

		// 各ソケットの先頭ブロックを保持する 変換に利用する情報
		List<Element> blockSockets = new ArrayList<>();

		// socket will have an element
		blockSockets.add(sockets.get(0));

		if (trueBlock.size() > 0) {
			blockSockets.add(trueBlock.get(0));
		} else {
			blockSockets.add(null);
		}

		// falseブロックのパース結果をソケットに追加する
		if (falseBlock.size() > 0) {
			blockSockets.add(falseBlock.get(0));
		} else {
			blockSockets.add(null);
		}

		List<Node> ifSocketNodes = resolver.getSocketNodes("ifelse");
		Map<String, String[]> socketsInfo = calcSocketsInfo(ifSocketNodes);

		// ソケットの出力
		addSocketsNode(blockSockets, document, blockElement, socketsInfo.get("connector-type"),
				socketsInfo.get("position-type"), socketsInfo.get("label"));

		elements.add(blockElement);
		elements.addAll(sockets);
		elements.addAll(trueBlock);
		elements.addAll(falseBlock);

		addedModels.put(Integer.toString(((UniNode) ifexpr).hashCode()), blockElement);

		return elements;
	}

	public List<Element> parseIntLiteral(UniIntLiteral num, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		elements.add(createLiteralElement(document, parent, "number", Integer.toString(num.value),
				num.hashCode()));
		return elements;
	}

	public List<Element> parseStringLiteral(UniStringLiteral str, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		elements.add(createLiteralElement(document, parent, "string", str.value,
				((UniNode) str).hashCode()));
		return elements;
	}

	public static void addPlugElement(Document document, Element target, Node parentBlockNode,
			String plugType, String positionType) {
		Element plugNode = document.createElement("Plug");
		Element blockConnectorNode = document.createElement("BlockConnector");

		blockConnectorNode.setAttribute("con-block-id",
				ToBlockEditorParser.getAttribute(parentBlockNode, "id"));
		blockConnectorNode.setAttribute("connector-kind", "plug");
		blockConnectorNode.setAttribute("connector-type", plugType);
		blockConnectorNode.setAttribute("init-type", plugType);
		blockConnectorNode.setAttribute("label", "");
		blockConnectorNode.setAttribute("position-type", positionType);

		plugNode.appendChild(blockConnectorNode);

		target.appendChild(plugNode);
	}

	public List<Element> parseMethodCall(UniMethodCall method, Document document, Node parent) {
		String genusName = calcMethodCallGenusName(method);
		String kind = "command";
		List<Element> exprs = new ArrayList<Element>();

		Element element = createBlockElement(document, genusName, ID_COUNTER++, kind);

		if (method.args != null) {
			// 引数パース
			for (UniExpr expr : method.args) {
				for (Element arg : parseExpr(expr, document, element)) {
					exprs.add(arg);
				}
			}

			List<Node> socketNodes = resolver.getSocketNodes(genusName);
			Map<String, String[]> socketsInfo = calcSocketsInfo(socketNodes);

			// socketの追加
			addSocketsNode(exprs, document, element, socketsInfo.get("connector-type"),
					socketsInfo.get("position-type"), socketsInfo.get("label"));
		}

		exprs.add(0, element);

		addedModels.put(Integer.toString(((UniNode) method).hashCode()), element);

		return exprs;
	}

	/*
	 * UniMethodCallの関数名からBlockの名前を計算する
	 */
	private String calcMethodCallGenusName(UniMethodCall method) {
		String genusName = "";
		// 名前空間補完}

		genusName += method.methodName + "[";

		for (UniExpr arg : method.args) {
			genusName += "@" + convertParamTypeName(calcParamType(arg));
		}

		genusName += "]";

		genusName = resolver.getNamespace(genusName) + genusName;

		return genusName;
	}

	public static String convertParamTypeName(String name) {
		if (name.equals("number") || name.equals("double-number")) {
			return "int";
		} else {
			return name;
		}
	}

	private static String calcParamType(UniExpr param) {
		String type = "";
		if (param instanceof UniStringLiteral) {
			type = "string";
		} else if (param instanceof UniIntLiteral) {
			type = "number";
		} else {
			throw new RuntimeException(param.toString() + "has not been supported yet.");
		}
		return type;
	}

	public static void addBeforeBlockNode(Document document, Element blockNode, String id) {
		Element element = document.createElement("BeforeBlockId");
		element.setTextContent(id);

		blockNode.appendChild(element);
	}

	public static void addSocketsNode(List<Element> args, Document document, Element blockNode,
			String[] connectorTypes, String[] positionTypes, String[] socketLabels) {
		Element sockets = document.createElement("Sockets");
		sockets.setAttribute("num-sockets", String.valueOf(args.size()));

		if (socketLabels != null) {
			for (int i = 0; i < args.size(); i++) {
				addSocketNode(document, args.get(i), sockets, connectorTypes[i], positionTypes[i],
						socketLabels[i]);
			}
		}

		blockNode.appendChild(sockets);
	}

	public static void addSocketNode(Document document, Element argElement, Node socketsNode,
			String connectorType, String positionType, String socketLabel) {
		Element connector = document.createElement("BlockConnector");

		connector.setAttribute("connector-kind", "socket");
		connector.setAttribute("position-type", positionType);
		connector.setAttribute("label", socketLabel);

		if (argElement != null) {
			connector.setAttribute("con-block-id",
					ToBlockEditorParser.getAttribute(argElement, "id"));
			connector.setAttribute("connector-type", connectorType);
			connector.setAttribute("init-type", connectorType);
		} else {
			connector.setAttribute("connector-type", connectorType);
			connector.setAttribute("init-type", connectorType);
		}

		socketsNode.appendChild(connector);
	}

	public static String getSocketConnectorType(String kind) {
		if (kind == null) {
			return "cmd";
		} else {
			return kind;
		}
	}

	public static void addLocationElement(Document document, String x, String y,
			Element blockElement) {
		Element locationElement = document.createElement("Location");
		Element xElement = document.createElement("X");
		Element yElement = document.createElement("Y");

		xElement.setTextContent(x);
		yElement.setTextContent(y);

		locationElement.appendChild(xElement);
		locationElement.appendChild(yElement);

		blockElement.appendChild(locationElement);
	}

	public static void addLabelElement(Document document, String label, Element blockElement) {
		Element element = document.createElement("Label");
		element.setTextContent(label);
		blockElement.appendChild(element);
	}

	public static void addTypeElement(Document document, String type, Element blockElement) {
		Element element = document.createElement("Type");
		element.setTextContent(type);
		blockElement.appendChild(element);
	}

	public static Element createBlockElement(Document document, String genusName, long id,
			String kind) {
		Element element = document.createElement("Block");
		element.setAttribute("genus-name", genusName);
		element.setAttribute("id", String.valueOf(id));
		element.setAttribute("kind", kind);

		return element;
	}

	public String getSaveString(UniClassDec classDec) {
		try {
			Node node = getSaveNode(classDec);

			StringWriter writer = new StringWriter();
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(node), new StreamResult(writer));
			return writer.toString();
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
	}

	public Node getSaveNode(UniClassDec classDec) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Element documentElement = document.createElementNS(XML_CODEBLOCKS_NS, "cb:CODEBLOCKS");

			// schema reference
			documentElement.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
					"xsi:schemaLocation", XML_CODEBLOCKS_NS + " " + XML_CODEBLOCKS_SCHEMA_URI);

			// Pagesノードの作成
			Element pagesElement = document.createElement("Pages");
			pagesElement.setAttribute("collapsible-pages", "no");
			// Pageノードの作成
			Element pageElement = document.createElement("Page");
			pageElement.setAttribute("page-color", "40 40 40");
			pageElement.setAttribute("page-drawer", "");
			pageElement.setAttribute("page-infullview", "yes");
			pageElement.setAttribute("page-name", classDec.className);
			pageElement.setAttribute("page-width", "1366");

			Element pageBlocksElement = document.createElement("PageBlocks");

			// クラスのパース
			parseClass(classDec, document, pageBlocksElement);

			pageElement.appendChild(pageBlocksElement);
			pagesElement.appendChild(pageElement);
			documentElement.appendChild(pagesElement);
			document.appendChild(documentElement);

			return document;
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

}
