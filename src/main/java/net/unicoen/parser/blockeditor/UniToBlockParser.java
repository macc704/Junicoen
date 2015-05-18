package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import net.unicoen.node.UniDoubleLiteral;
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
	private VariableNameResolver vnResolver = new VariableNameResolver();

	private static Map<String, Element> addedModels = new HashMap<String, Element>();
	private String projectPath;

	public UniToBlockParser() {
		resolver = new BlockNameResolver();
	}

	public void setProjectPath(String path){
		this.projectPath = path;
	}

	public static Map<String, Element> getAddedModels() {
		return addedModels;
	}

	/*
	 * Unicoenモデルを解析し、xmlファイルを作成し返す
	 */
	public File parse(UniClassDec classDec) throws IOException {
		// クラス名のxmlファイルを作成する
		addedModels.clear(); // cashクリア

		File file = new File(projectPath + classDec.className + ".xml");
		if(!file.exists()){
			file.createNewFile();
		}
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
		try {
			writer.write(getSaveString(classDec));
		} finally {
			if (writer != null) {
				writer.close();
			}
		}
		return file;
	}

	public File parse(UniClassDec classDec, String path) throws IOException {
		// クラス名のxmlファイルを作成する
		addedModels.clear(); // cashクリア

		File file = new File(path + classDec.className + ".xml");
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			fileWriter.write(getSaveString(classDec));
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
		return file;
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

		addElement("Label", document, funcDec.methodName, procedureElement);
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

		addElement("Label", document, funcDec.methodName, procedureElement);
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

				beforeId = getBlockAttibuteByElement(elements.get(0), "id");
			}
		}

		for (Element element : blocks) {
			pageElement.appendChild(element);
		}

		vnResolver.resetLocalVariables();
	}

	public String getBlockAttibuteByElement(Element element, String attributeName) {
		if (element.getNodeName().equals("BlockStub")) {
			Node blockNode = ToBlockEditorParser.getChildNode(element, "Block");
			return ToBlockEditorParser.getAttribute(blockNode, attributeName);
		} else {
			return element.getAttribute(attributeName);
		}
	}

	public static void addAfterBlockNode(Document document, Element blockNode, String id) {
		Element element = document.createElement("AfterBlockId");
		element.setTextContent(id);

		if (blockNode.getNodeName().equals("BlockStub")) {
			ToBlockEditorParser.getChildNode(blockNode, "Block").appendChild(element);
		} else {
			blockNode.appendChild(element);
		}
	}

	public static void addBeforeBlockNode(Document document, Element blockNode, String id) {
		Element element = document.createElement("BeforeBlockId");
		element.setTextContent(id);

		if (blockNode.getNodeName().equals("BlockStub")) {
			ToBlockEditorParser.getChildNode(blockNode, "Block").appendChild(element);
		} else {
			blockNode.appendChild(element);
		}
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
		} else if (expr instanceof UniDoubleLiteral) {
			return parseDoubleLiteral((UniDoubleLiteral) expr, document, parent);
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
			return parseIdent((UniIdent) expr, document, parent);
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

	public List<Element> parseIdent(UniIdent expr, Document document, Node parent) {
		Node varDecNode = vnResolver.getVariableNode(expr.name);
		List<Element> elements = new ArrayList<>();
		if (varDecNode != null) {
			String genusName = "getter";
			genusName += ToBlockEditorParser.getAttribute(varDecNode, "genus-name");
			// BlockStubノード作成
			Element stubElement = createBlockStubNode(document, expr.name,
					ToBlockEditorParser.getAttribute(varDecNode, "genus-name"));
			// Blockノード作成
			Element blockElement = createVariableBlockNode(document, genusName, expr.name, "data");

			List<Node> socketNodes = resolver.getSocketNodes(genusName.substring("setter".length(),
					genusName.length()));
			addPlugElement(document, blockElement, parent,
					ToBlockEditorParser.getAttribute(socketNodes.get(0), "connector-type"),
					"mirror");

			stubElement.appendChild(blockElement);

			elements.add(stubElement);

			return elements;
		} else {
			throw new RuntimeException("illegal variable:" + expr.name);
		}
	}

	public List<Element> parseFor(UniFor forExpr, Document document, Node parent) {
		// whileに変換
		List<Element> elements = new ArrayList<>();

		List<Element> initializer = parseExpr((UniExpr) forExpr.init, document, (Element) parent);

		UniWhile whileModel = new UniWhile(forExpr.cond, forExpr.block);
		whileModel.block.body.add(forExpr.step);
		List<Element> whileBlocks = parseExpr((UniExpr) whileModel, document, (Element) parent);
		if (initializer != null) {
			addBeforeBlockNode(document, whileBlocks.get(0),
					getBlockAttibuteByElement(initializer.get(0), "id"));
			addAfterBlockNode(document, initializer.get(0),
					getBlockAttibuteByElement(whileBlocks.get(0), "id"));
		}

		elements.addAll(initializer);
		elements.addAll(whileBlocks);

		return elements;
	}

	public List<Element> parseDoWhile(UniDoWhile doWhileExpr, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement = createBlockElement(document, "dowhile", ID_COUNTER++, "command");

		List<Element> sockets = parseExpr(doWhileExpr.cond, document, blockElement);
		List<Element> trueBlock = parseBody(document, blockElement, doWhileExpr.block);

		List<Element> blockSockets = new ArrayList<>();
		// socket will have an element
		if (trueBlock.size() > 0) {
			blockSockets.add(trueBlock.get(0));
		} else {
			blockSockets.add(null);
		}

		blockSockets.add(sockets.get(0));

		// genusnameからソケットのラベルとポジションタイプを作成
		List<Node> whileSocketNodes = resolver.getSocketNodes("dowhile");
		Map<String, String[]> socketsInfo = calcSocketsInfo(whileSocketNodes);

		// ソケットの出力
		addSocketsNode(blockSockets, document, blockElement, socketsInfo.get("connector-type"),
				socketsInfo.get("position-type"), socketsInfo.get("label"));

		elements.add(blockElement);
		elements.addAll(trueBlock);
		elements.addAll(sockets);

		addedModels.put(Integer.toString(((UniNode) doWhileExpr).hashCode()), blockElement);

		return elements;
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

		elements.addAll(initializer);

		return elements;
	}

	public Map<String, String[]> calcSocketsInfo(List<Node> socketNodes) {
		if (socketNodes == null || socketNodes.size() <= 0) {
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
		if (vnResolver.getVariableNode(name) == null) {
			List<Element> elements = new ArrayList<Element>();
			Element blockElement;

			blockElement = createBlockElement(document, "local-var-" + convertBlockTypeName(type),
					ID_COUNTER++, "local-variable");

			addElement("Label", document, name, blockElement);
			addElement("Name", document, name, blockElement);
			addElement("Type", document, type, blockElement);

			elements.add(blockElement);
			vnResolver.addLocalVariable(name, (Node) blockElement);
			return elements;
		} else {
			throw new RuntimeException(name + " is already defined");
		}
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
		List<Element> elements = new ArrayList<Element>();
		if ("!".equals(uniOp.operator)) {
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
		} else if ("_++".equals(uniOp.operator) || "_--".equals(uniOp.operator)
				|| "++_".equals(uniOp.operator) || "--_".equals(uniOp.operator)) {
			String genusName = "setter";
			UniIdent ident = (UniIdent) uniOp.expr;
			String operator = "+";
			if (uniOp.operator.contains("--")) {
				operator = "-";
			}

			Node varDecNode = vnResolver.getGlobalVariableID(ident.name);
			if (varDecNode == null) {
				// blockeditorではとりあえずprivateアクセス修飾子に無理やり変換する
				varDecNode = vnResolver.getLocalVariableID(ident.name);
				if (varDecNode == null) {
					throw new RuntimeException(ident.name + " is not defined");
				}
			}
			if (parent == null) {
				genusName += ToBlockEditorParser.getAttribute(varDecNode, "genus-name");
				// 代入演算右(i+1, i-1)
				// BlockStubノード作成
				Element blockStubElement = createBlockStubNode(document, ident.name,
						ToBlockEditorParser.getAttribute(varDecNode, "genus-name"));
				// Blockノード作成
				Element blockElement = createVariableBlockNode(document, genusName, ident.name,
						"command");

				List<Node> socketNodes = resolver.getSocketNodes(genusName.substring("setter".length(),genusName.length()));

				Map<String, String[]> sockets = calcSocketsInfo(socketNodes);

				List<Element> right = parseExpr((UniExpr) (new UniBinOp(operator, new UniIdent(ident.name), new UniIntLiteral(1))), document, blockElement);

				List<Element> rightElement = new ArrayList<>();
				rightElement.add(right.get(0));

				addSocketsNode(rightElement, document, blockElement, sockets.get("connector-type"),
						sockets.get("position-type"), sockets.get("label"));

				// BlockStubノードにBlockノードを追加する
				blockStubElement.appendChild(blockElement);
				elements.add(blockStubElement);
				elements.addAll(right);
				return elements;
			} else {
				if(uniOp.operator.equals("_++")){
					genusName = "postinc";
				} else if(uniOp.operator.equals("_--")){
					genusName = "postdec";
				} else if(uniOp.operator.equals("++_")){
					genusName = "preinc";
				}else{
					genusName = "predec";
				}

				if(!getExprType(uniOp.expr).equals("int")){
					genusName += "double";
				}

				Element blockStubElement = createBlockStubNode(document, ident.name, ToBlockEditorParser.getAttribute(varDecNode, "genus-name"));
				// Blockノード作成
				Element blockElement = createVariableBlockNode(document, genusName + ToBlockEditorParser.getAttribute(varDecNode, "genus-name"), ident.name, "command");

				// BlockStubノードにBlockノードを追加する
				blockStubElement.appendChild(blockElement);
				elements.add(blockStubElement);
				return elements;
			}
		} else {
			throw new RuntimeException("not supported unary operator");
		}
	}

	public Element createVariableBlockNode(Document document, String genusName, String name,
			String kind) {
		Element blockElement = createBlockElement(document, genusName, ID_COUNTER++, kind);
		addElement("Label", document, name, blockElement);
		addElement("Name", document, name, blockElement);
		return blockElement;
	}

	public Element createBlockStubNode(Document document, String parentName, String parentGenusName) {
		Element blockStubElement = document.createElement("BlockStub");
		addElement("StubParentName", document, parentName, blockStubElement);
		addElement("StubParentGenus", document, parentGenusName, blockStubElement);
		return blockStubElement;
	}

	public List<Element> parseContinueBreak(String name, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		elements.add(createBlockElement(document, name, ID_COUNTER++, "command"));
		return elements;
	}

	public List<Element> parseBinOp(UniBinOp binopExpr, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement;
		// 左右を先読みして，何型の演算か取得
		String type = getExprType(binopExpr.left, binopExpr.right);
		if (binopExpr.operator.equals("=")) {// 他の二項演算と扱いが別（ソケットが一つのみ）
			String genusName = "setter";
			if (binopExpr.left instanceof UniIdent) {
				UniIdent ident = (UniIdent) binopExpr.left;
				Node varDecNode = vnResolver.getGlobalVariableID(ident.name);
				if (varDecNode == null) {
					// blockeditorではとりあえずprivateアクセス修飾子に無理やり変換する
					varDecNode = vnResolver.getLocalVariableID(ident.name);
					if (varDecNode == null) {
						throw new RuntimeException(ident.name + " is not defined");
					}
				}
				genusName += ToBlockEditorParser.getAttribute(varDecNode, "genus-name");
				// BlockStubノード作成
				Element blockStubElement = createBlockStubNode(document, ident.name,
						ToBlockEditorParser.getAttribute(varDecNode, "genus-name"));

				// Blockノード作成
				blockElement = createVariableBlockNode(document, genusName, ident.name, "command");

				List<Element> right = parseExpr(binopExpr.right, document, blockElement);
				List<Node> socketNodes = resolver.getSocketNodes(genusName.substring(
						"setter".length(), genusName.length()));
				if (socketNodes != null) {
					Map<String, String[]> sockets = calcSocketsInfo(socketNodes);
					List<Element> rightElement = new ArrayList<>();
					rightElement.add(right.get(0));

					addSocketsNode(rightElement, document, blockElement,
							sockets.get("connector-type"), sockets.get("position-type"),
							sockets.get("label"));

					// BlockStubノードにBlockノードを追加する
					blockStubElement.appendChild(blockElement);
					elements.add(blockStubElement);
					elements.addAll(right);
					return elements;
				} else {
					throw new RuntimeException("illegal right element:" + binopExpr.right);
				}
			} else {
				throw new RuntimeException("illegal left op:" + binopExpr.left);
			}
		} else if (binopExpr.operator.equals("&&")) {
			blockElement = createBlockElement(document, "and", ID_COUNTER++, "function");
		} else if (binopExpr.operator.equals("||")) {
			blockElement = createBlockElement(document, "or", ID_COUNTER++, "function");
		} else if (binopExpr.operator.equals("==") || binopExpr.operator.equals("!=")) {
			String genusName = "equals-";
			if (binopExpr.operator.equals("!=")) {
				genusName = "not-" + genusName;
			}
			// 左右の型に応じてブロックの名前を変える(String, boolean, number, double number)
			if ("int".equals(type)) {
				genusName += "number";
			} else if ("double".equals(type)) {
				genusName += "number-double";
			} else if ("String".equals(type)) {
				genusName += "string";
			} else if ("boolean".equals(type)) {
				genusName += "boolean";
			} else {
				throw new RuntimeException(type + "is not supported type yet at "
						+ binopExpr.operator);
			}
			blockElement = createBlockElement(document, genusName, ID_COUNTER++, "function");
		} else if (binopExpr.operator.equals(">=") || binopExpr.operator.equals(">")) {
			String genusName = "greaterthan";
			if (binopExpr.operator.equals(">=")) {
				genusName = "greaterthanorequalto";
			}

			if ("int".equals(type)) {
			} else if ("double".equals(type)) {
				genusName += "-double";
			} else {
				throw new RuntimeException(type + "is not supported type yet at "
						+ binopExpr.operator);
			}
			blockElement = createBlockElement(document, genusName, ID_COUNTER++, "function");
		} else if (binopExpr.operator.equals("<=") || binopExpr.operator.equals("<")) {
			String genusName = "lessthan";
			if (binopExpr.operator.equals("<=")) {
				genusName = "lessthanorequalto";
			}

			if ("int".equals(type)) {
			} else if ("double".equals(type)) {
				genusName += "-double";
			} else {
				throw new RuntimeException(type + "is not supported type yet at "
						+ binopExpr.operator);
			}
			blockElement = createBlockElement(document, genusName, ID_COUNTER++, "function");
		} else if (binopExpr.operator.equals("+") || binopExpr.operator.equals("-")
				|| binopExpr.operator.equals("*") || binopExpr.operator.equals("/")
				|| binopExpr.operator.equals("%")) {
			// 左右の型に応じてブロックの名前を変える(number or double-number)
			String genusName = "sum";
			if (binopExpr.operator.equals("-")) {
				genusName = "difference";
			} else if (binopExpr.operator.equals("*")) {
				genusName = "product";
			} else if (binopExpr.operator.equals("/")) {
				genusName = "quotient";
			} else if (binopExpr.operator.equals("%")) {
				genusName = "remainder";
			}

			if (genusName.equals("sum")) {
				if ("int".equals(type)) {
				} else if ("double".equals(type)) {
					genusName += "-double";
				} else if ("String".equals(type)) {
					genusName = "string-append";
				} else {
					throw new RuntimeException(type + " is not supported type yet at "
							+ binopExpr.operator);
				}
			} else {
				if ("int".equals(type)) {
				} else if ("double".equals(type)) {
					genusName += "-double";
				} else {
					throw new RuntimeException(type + "is not supported type yet at "
							+ binopExpr.operator);
				}
			}
			blockElement = createBlockElement(document, genusName, ID_COUNTER++, "function");
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

	public String getBlockTypeName(String type) {
		if ("int".equals(type)) {
			return "int-number";
		} else if ("String".equals(type)) {
			return "string";
		} else if ("double".equals(type)) {
			return "double-number";
		} else if ("boolean".equals(type)) {
			return "boolean";
		} else {
			return type;
		}
	}

	public String getExprType(UniExpr left, UniExpr right) {
		// exprの型を取得する
		String leftType = getExprType(left);
		String rightType = getExprType(right);
		if (leftType.equals(rightType)) {
			return leftType;
		} else if ((leftType.equals("int") && rightType.equals("double"))
				|| (leftType.equals("double") && rightType.equals("int"))) {
			return "int";
		} else {
			return null;
		}
	}

	public String getExprType(UniExpr expr) {
		String type = null;
		if (expr instanceof UniIntLiteral) {
			type = "int";
		} else if (expr instanceof UniStringLiteral) {
			type = "String";
		} else if (expr instanceof UniBoolLiteral) {
			type = "boolean";
		} else if (expr instanceof UniDoubleLiteral) {
			type = "double";
		} else if (expr instanceof UniMethodCall) {
			throw new RuntimeException("not supported yet");
		} else if (expr instanceof UniIdent) {
			type = ToBlockEditorParser.getChildNode(
					vnResolver.getVariableNode(((UniIdent) expr).name), "Type").getTextContent();
		} else if (expr instanceof UniBinOp) {
			type = getExprType(((UniBinOp) expr).left, ((UniBinOp) expr).right);
		} else if (expr instanceof UniUnaryOp) {
			type = getExprType(((UniUnaryOp) expr).expr);
		}
		return type;
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

		addElement("Label", document, label, blockElement);

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

	public List<Element> parseDoubleLiteral(UniDoubleLiteral num, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		elements.add(createLiteralElement(document, parent, "double-number",
				Double.toString(num.value), num.hashCode()));
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

	private String calcParamType(UniExpr param) {
		String type = "";
		if (param instanceof UniStringLiteral) {
			type = "string";
		} else if (param instanceof UniIntLiteral) {
			type = "number";
		} else if (param instanceof UniIdent) {
			type = ToBlockEditorParser.getChildNode(
					vnResolver.getVariableNode(((UniIdent) param).name), "Type").getTextContent();
		} else {
			throw new RuntimeException(param.toString() + "has not been supported yet.");
		}
		return type;
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
			connector.setAttribute("con-block-id", getIdFromElement(argElement));
			connector.setAttribute("connector-type", connectorType);
			connector.setAttribute("init-type", connectorType);
		} else {
			connector.setAttribute("connector-type", connectorType);
			connector.setAttribute("init-type", connectorType);
		}

		socketsNode.appendChild(connector);
	}

	public static String getIdFromElement(Element element) {
		if ("BlockStub".equals(element.getNodeName())) {
			return ToBlockEditorParser.getAttribute(
					ToBlockEditorParser.getChildNode(element, "Block"), "id");
		} else {
			return ToBlockEditorParser.getAttribute(element, "id");
		}
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

	public static void addElement(String elementName, Document document, String name,
			Element blockElement) {
		Element element = document.createElement(elementName);
		element.setTextContent(name);
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
