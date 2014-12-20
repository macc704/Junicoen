package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
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
import net.unicoen.node.UniDecVar;
import net.unicoen.node.UniDecVarWithValue;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniIf;
import net.unicoen.node.UniIntLiteral;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniNode;
import net.unicoen.node.UniStringLiteral;
import net.unicoen.node.UniUnaryOp;
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

	private static Map<String, String> turtleMethods;

	private static Map<String, Element> addedModels = new HashMap<String, Element>();

	public UniToBlockParser(){
		BlockNameResolver.parseTurtleXml();
		turtleMethods = BlockNameResolver.getTurtleMethodsName();
	}
	
	public static Map<String, Element> getAddedModels() {
		return addedModels;
	}

	public void parse(UniClassDec classDec) throws IOException {
		// クラス名のxmlファイルを作成する
		addedModels.clear(); // cash初期化

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

	public void parseClass(UniClassDec classDec, Document document,
			Element pageElement) {
		for (UniMemberDec member : classDec.members) {
			if (member instanceof UniFuncDec) {
				parseFunctionDec((UniFuncDec) member, document, pageElement);
			}
		}
	}

	public List<Element> getFunctionNodes(UniFuncDec funcDec,
			Document document, Element pageElement) {
		List<Element> blocks = new ArrayList<Element>();
		Element procedureElement = createBlockElement(document, "procedure",
				ID_COUNTER++, "procedure");

		addLabelElement(document, funcDec.funcName, procedureElement);
		addLocationElement(document, "50", "50", procedureElement);

		blocks.add(procedureElement);
		if (funcDec.args != null) {
			throw new RuntimeException(
					"function args parsing has not been supported yet");
		}

		// funcDec.body ボディのパース
		blocks = parseBody(document, procedureElement, funcDec.block);

		blocks.add(blocks.size(), procedureElement);
		blocks.remove(0);

		for (Element element : blocks) {
			pageElement.appendChild(element);
		}
		// メソッドノード登録
		addedModels.put(Integer.toString(((UniNode) funcDec).hashCode()),
				procedureElement);

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
				addAfterBlockNode(document, elements.get(0),
						String.valueOf(ID_COUNTER));
			}
	
			blocks.addAll(elements);
			addBeforeBlockNode(document, elements.get(0), beforeId);
			beforeId = blocks.get(blocks.size() - elements.size())
					.getAttribute("id");
		}
		return blocks;
	}

	public void parseFunctionDec(UniFuncDec funcDec, Document document, Element pageElement) {
		List<Element> blocks = new ArrayList<Element>();
		Element procedureElement = createBlockElement(document, "procedure", ID_COUNTER++, "procedure");

		addLabelElement(document, funcDec.funcName, procedureElement);
		addLocationElement(document, "50", "50", procedureElement);

		blocks.add(procedureElement);
		if (funcDec.args != null) {
			throw new RuntimeException(
					"function args parsing has not been supported yet");
		}

		// funcDec.body ボディのパース
		if (funcDec.block != null && funcDec.block.body != null) {
			addAfterBlockNode(document, blocks.get(blocks.size() - 1),
					String.valueOf(ID_COUNTER));
			String beforeId = procedureElement.getAttribute("id");
			List<UniExpr> body = funcDec.block.body;
			for (int i = 0; i < body.size(); i++) {
				// expressionの解析 行き掛け順
				UniExpr expr = body.get(i);
				List<Element> elements = parseExpr(expr, document, null);// 木で返す．
				// 木の最上部が左辺ブロックになる　左辺ブロックに次のブロックのidをセットする
				if (i + 1 < body.size()) {
					addAfterBlockNode(document, elements.get(0),
							String.valueOf(ID_COUNTER));
				}

				blocks.addAll(elements);

				// 左辺ブロックに直前のブロックのIDを追加する
				addBeforeBlockNode(document, elements.get(0), beforeId);
				beforeId = blocks.get(blocks.size() - elements.size())
						.getAttribute("id");
			}
		}

		blocks.add(blocks.size(), procedureElement);
		blocks.remove(0);

		for (Element element : blocks) {
			pageElement.appendChild(element);
		}
	}

	public static void addAfterBlockNode(Document document, Element blockNode,
			String id) {
		Element element = document.createElement("AfterBlockId");
		element.setTextContent(id);

		blockNode.appendChild(element);
	}

	public List<Element> parseExpr(UniExpr expr, Document document,
			Element parent) {
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
		} else if(expr instanceof UniBreak){ 
			return parseContinueBreak("break", document, parent);
		} else if(expr instanceof UniContinue){
			return parseContinueBreak("continue", document, parent);
		} else if(expr instanceof UniUnaryOp){
			return parseUnaryOperator((UniUnaryOp)expr, document, parent);
		} else if(expr instanceof UniDecVar){
			return parseVarDec(((UniDecVar)expr).type,((UniDecVar)expr).name, document, parent);
		} else if(expr instanceof UniDecVarWithValue){
			return parseVarDec((UniDecVarWithValue)expr, document, parent);
		}
		else if(expr instanceof UniIdent){
			throw new RuntimeException("The expr has not been supported yet");
		} else {
			throw new RuntimeException("The expr has not been supported yet");
		}
	}
	
	public List<Element> parseVarDec(UniDecVarWithValue varDec, Document document, Node parent){
		List<Element> elements = parseVarDec(varDec.type, varDec.name, document, parent);
		//初期値のパース
		List<Element> initializer = parseExpr(varDec.value, document, elements.get(0));
		
		addSocketsNode(initializer, document, elements.get(0), createStringArray(convertTypeToBlockConnectorType(varDec.type)), createStringArray( "初期値"));
		
		return elements;
	}
	
	public List<Element> parseVarDec(String type, String name, Document document, Node parent){
		List<Element> elements = new ArrayList<Element>();
		Element blockElement;
		
		blockElement = createBlockElement(document, "local-var-" + convertBlockTypeName(type), ID_COUNTER++, "local-var");

		addLabelElement(document, name, blockElement);
		addTypeElement(document, type, blockElement);
		
		elements.add(blockElement);
		return elements;
	}

	public String convertBlockTypeName(String type){
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
	
	public String convertTypeToBlockConnectorType(String type){
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
	
	public List<Element> parseUnaryOperator(UniUnaryOp uniOp, Document document, Node parent){
		switch (uniOp.operator) {
		case "!":
			List<Element> elements = new ArrayList<Element>();
			Element blockElement = createBlockElement(document, "not", ID_COUNTER++, "function");
			addPlugElement(document, blockElement, parent, "boolean", "single");
			
			List<Element> args = new ArrayList<Element>();
			args = parseExpr(uniOp.expr, document, blockElement);
			
			addSocketsNode(args, document, blockElement, createStringArray("single"), createStringArray(""));

			elements.add(blockElement);
			elements.addAll(args);
			
			return elements;
		default:
			throw new RuntimeException("not supported unary operator");
		}
	}
	
	public List<Element> parseContinueBreak(String name,Document document, Node parent){
		List<Element> elements = new ArrayList<Element>();
		elements.add(createBlockElement(document, name, ID_COUNTER++, "command"));
		return elements;
	}

	public List<Element> parseBinOp(UniBinOp binopExpr, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement;
		
		if(binopExpr.operator.equals("&&")){ 
			 blockElement = createBlockElement(document, "and", ID_COUNTER++, "function");
		}else if(binopExpr.operator.equals("||")){
			blockElement = createBlockElement(document, "or", ID_COUNTER++, "function");
		}else if(binopExpr.operator.equals("!")){
			blockElement = createBlockElement(document, "not", ID_COUNTER++, "function");
		}else{
			throw new RuntimeException("unequipment operator");
		}
		
		addPlugElement(document, blockElement,parent, "boolean", "mirror");
		
		List<Element> leftBlocks = parseExpr(binopExpr.left, document, blockElement);
		List<Element> rightBlocks = parseExpr(binopExpr.right, document, blockElement);
		
		List<Element> args = new ArrayList<Element>();
		args.add(leftBlocks.get(0));
		args.add(rightBlocks.get(0));
		
		addSocketsNode(args, document, blockElement, createStringArray("bottom", "bottom") ,createStringArray("", ""));
		
		elements.add(blockElement);
		elements.addAll(leftBlocks);
		elements.addAll(rightBlocks);
		
		addedModels.put(Integer.toString(((UniNode) binopExpr).hashCode()), blockElement);
		
		return elements;
	}

	public static String[] createStringArray(String... labels){
		String[] socketLabels = new String[labels.length];
		for(int i = 0; i< labels.length;i++){
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
		//socket will have an element
		blockSockets.add(sockets.get(0));
		
		if (trueBlock.size() > 0) {
			blockSockets.add(trueBlock.get(0));
		}else{
			blockSockets.add(null);
		}
		
		// ソケットの出力
		addSocketsNode(blockSockets, document, blockElement, createStringArray("single", "single"), createStringArray("かどうか調べて", "真の間"));

		elements.add(blockElement);
		
		elements.addAll(sockets);
		elements.addAll(trueBlock);
		
		addedModels.put(Integer.toString(((UniNode) whileExpr).hashCode()), blockElement);

		return elements;
	}

	public List<Element> parseBoolLiteral(UniBoolLiteral boolexpr,
			Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement;
		if(boolexpr.value){
			blockElement = createBlockElement(document, "true",	ID_COUNTER++, "data");
			
			addLabelElement(document, "真", blockElement);
			addPlugElement(document, blockElement, parent, "boolean", "mirror");
		}else{
			blockElement = createBlockElement(document, "false", ID_COUNTER++, "data");			
			
			addLabelElement(document, "偽", blockElement);
			addPlugElement(document, blockElement, parent, "boolean", "mirror");
		}

		elements.add(blockElement);
		
		addedModels.put(Integer.toString(((UniNode) boolexpr).hashCode()), blockElement);

		return elements;
	}

	public List<Element> parseIf(UniIf ifexpr, Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement = createBlockElement(document, "ifelse", ID_COUNTER++, "command");

		List<Element> sockets = parseExpr(ifexpr.cond, document, blockElement);
		List<Element> trueBlock = parseBody(document, blockElement, ifexpr.trueBlock);
		List<Element> falseBlock = parseBody(document, blockElement, ifexpr.falseBlock);

		List<Element> blockSockets = new ArrayList<>();
		//socket will have an element
		blockSockets.add(sockets.get(0));
		
		if (trueBlock.size() > 0) {
			blockSockets.add(trueBlock.get(0));
		}else{
			blockSockets.add(null);
		}
		
		if (falseBlock.size() > 0) {
			blockSockets.add(falseBlock.get(0));
		}else{
			blockSockets.add(null);
		}

		// ソケットの出力
		addSocketsNode(blockSockets, document, blockElement, createStringArray("single", "single", "single"), createStringArray("かどうか調べて", "真のとき", "偽のとき"));

		elements.add(blockElement);
		
		elements.addAll(sockets);
		elements.addAll(trueBlock);
		elements.addAll(falseBlock);
		
		addedModels.put(Integer.toString(((UniNode) ifexpr).hashCode()), blockElement);

		return elements;
	}

	public List<Element> parseIntLiteral(UniIntLiteral num, Document document,
			Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement = createBlockElement(document, "number",
				ID_COUNTER++, "data");

		addLabelElement(document, Integer.toString(num.value), blockElement);
		addPlugElement(document, blockElement, parent, "number", "mirror");

		elements.add(blockElement);

		addedModels.put(String.valueOf(num.hashCode()), blockElement);

		return elements;
	}

	public List<Element> parseStringLiteral(UniStringLiteral str,
			Document document, Node parent) {
		List<Element> elements = new ArrayList<Element>();
		Element blockElement = createBlockElement(document, "string",
				ID_COUNTER++, "data");

		addLabelElement(document, str.value, blockElement);

		addPlugElement(document, blockElement, parent, "string", "mirror");

		elements.add(blockElement);

		addedModels.put(Integer.toString(((UniNode) str).hashCode()),
				blockElement);

		return elements;
	}

	public static void addPlugElement(Document document, Element target,
			Node parentBlockNode, String plugType, String positionType) {
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
			String[] paramsLabel = new String[method.args.size()];
			String[] argsPositionType = new String[method.args.size()];
			int i = 0;
			for (UniExpr expr : method.args) {
				for (Element arg : parseExpr(expr, document, element)) {
					exprs.add(arg);
				}
				paramsLabel[i] = "";
				argsPositionType[i] ="single";
			}
			// socketの追加
			addSocketsNode(exprs, document, element, argsPositionType, paramsLabel);
		}

		exprs.add(0, element);

		addedModels.put(Integer.toString(((UniNode) method).hashCode()), element);

		return exprs;
	}
	
	/*
	 *	UniMethodCallの関数名からBlockの名前を計算する 
	 */
	private static String calcMethodCallGenusName(UniMethodCall method) {
		String genusName = "";
		// 名前空間補完}

		genusName += method.methodName + "[";

		for (UniExpr arg : method.args) {
			genusName += "@" + convertParamTypeName(calcParamType(arg));
		}

		genusName += "]";
		
		genusName = getNamespace(genusName) + genusName;
		
		return genusName;
	}

	public static String convertParamTypeName(String name) {
		if (name.equals("number") || name.equals("double-number")) {
			return "int";
		} else {
			return name;
		}
	}

	private static String getNamespace(String name) {
		String namespace = turtleMethods.get(name);
		if(namespace != null){
			return namespace + "-";
		}
		return "";
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

	public static void addBeforeBlockNode(Document document, Element blockNode,
			String id) {
		Element element = document.createElement("BeforeBlockId");
		element.setTextContent(id);

		blockNode.appendChild(element);
	}

	public static void addSocketsNode(List<Element> args, Document document, Element blockNode, String[] connectorTypes,String[] socketLabels) {
		Element sockets = document.createElement("Sockets");
		sockets.setAttribute("num-sockets", String.valueOf(args.size()));
		
		if(socketLabels != null){
			for (int i = 0; i < args.size();i++) {
				addSocketNode(document, args.get(i), sockets, connectorTypes[i], socketLabels[i]);
			}	
		}

		blockNode.appendChild(sockets);
	}

	public static void addSocketNode(Document document, Element argElement,	Node socketsNode , String positionType, String socketLabel) {
		Element connector = document.createElement("BlockConnector");
		
		connector.setAttribute("connector-kind", "socket");
		connector.setAttribute("position-type", positionType);
		connector.setAttribute("label", socketLabel);
		Node plugNode;
		
		if(argElement != null){
			connector.setAttribute("con-block-id", ToBlockEditorParser.getAttribute(argElement, "id"));
			if(ToBlockEditorParser.getChildNode(ToBlockEditorParser.getChildNode(argElement, "Plug") )!= null){
				plugNode = ToBlockEditorParser.getChildNode(ToBlockEditorParser.getChildNode(argElement, "Plug"), "BlockConnector");
				connector.setAttribute("connector-type", getSocketConnectorType(ToBlockEditorParser.getAttribute(plugNode, "connector-type")));
				connector.setAttribute("init-type", getSocketConnectorType(ToBlockEditorParser.getAttribute(plugNode, "connector-type")));
			}else{
				connector.setAttribute("connector-type", "cmd");
				connector.setAttribute("init-type", "cmd");			
			}
		}else{
			connector.setAttribute("connector-type", "cmd");
			connector.setAttribute("init-type", "cmd");					
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

	public static void addLocationElement(Document document, String x,
			String y, Element blockElement) {
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

	public static Element createBlockElement(Document document,
			String genusName, long id, String kind) {
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
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer
					.transform(new DOMSource(node), new StreamResult(writer));
			return writer.toString();
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		} catch (TransformerException e) {
			throw new RuntimeException(e);
		}
	}

	public Node getSaveNode(UniClassDec classDec) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();

			Element documentElement = document.createElementNS(
					XML_CODEBLOCKS_NS, "cb:CODEBLOCKS");

			// schema reference
			documentElement.setAttributeNS(
					XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
					"xsi:schemaLocation", XML_CODEBLOCKS_NS + " "
							+ XML_CODEBLOCKS_SCHEMA_URI);

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
