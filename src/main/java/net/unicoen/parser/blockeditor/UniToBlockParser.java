package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

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

import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniIdent;
import net.unicoen.node.UniMemberDec;
import net.unicoen.node.UniMethodCall;
import net.unicoen.node.UniStringLiteral;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class UniToBlockParser {
	
	/** Name space definition */
	private static String XML_CODEBLOCKS_NS = "http://education.mit.edu/openblocks/ns";
	
	/** Location of schema */
	private static String XML_CODEBLOCKS_SCHEMA_URI = "http://education.mit.edu/openblocks/codeblocks.xsd";
	
	private static long ID_COUNTER = 1100; 
	
//	private HashMap<String, String> idMap = new HashMap<String , String>();

	public void parse(UniClassDec classDec) throws IOException{
		//クラス名のxmlファイルを作成する
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
	
	public static void parseFunctionDec(UniFuncDec funcDec, Document document, Element pageElement){
		List<Element> blocks = new ArrayList<Element>();
		Element procedureElement = createBlockElement(document, "procedure", ID_COUNTER++ ,"procedure");

		addLabelElement(document, funcDec.funcName, procedureElement);
		addLocationElement(document, "50", "50", procedureElement);

		blocks.add(procedureElement);
		if(funcDec.args != null){
			throw new RuntimeException("function args parsing has not been supported yet");
		}
		
		//funcDec.body ボディのパース
		if(funcDec.body != null){
			for(UniExpr expr : funcDec.body){
				addAfterBlockNode(document, blocks.get(blocks.size()-1), String.valueOf(ID_COUNTER));
				List<Element> elements = parseExpr(expr, document, null);
				addBeforeBlockNode(document, elements.get(0), blocks.get(blocks.size()-1).getAttribute("id"));
				blocks.addAll(elements);
			}		
		}
		
		blocks.add(blocks.size(), procedureElement);
		blocks.remove(0);
		
		for(Element element : blocks){
			pageElement.appendChild(element);
		}
	}
	
	public static void addAfterBlockNode(Document document, Element blockNode, String id){
		Element element = document.createElement("AfterBlockId");
		element.setTextContent(id);
		
		blockNode.appendChild(element);
	}
	
	public static List<Element> parseExpr(UniExpr expr, Document document, Element parent){
		if(expr instanceof UniMethodCall){
			return parseMethodCall((UniMethodCall)expr, document, parent);
		} else if(expr instanceof UniStringLiteral){
			return parseStringLiteral((UniStringLiteral)expr, document, parent);
		} else{
			throw new RuntimeException("The expr has not been supported yet");			
		}
	}
	
	public static List<Element> parseStringLiteral(UniStringLiteral str, Document document, Node parent){
		List<Element> elements = new ArrayList<Element>();
		Element blockElement = createBlockElement(document, "string", ID_COUNTER++, "data");
		
		addLabelElement(document, str.value, blockElement);
//		addLocationElement(document, "50", "50", blockElement);
		addPlugElement(document, blockElement, parent, "string");
		
		elements.add(blockElement);
		return elements;
	}
	
	public static void addPlugElement(Document document, Element target, Node parentBlockNode, String plugType){
		Element plugNode = document.createElement("Plug");
		Element blockConnectorNode = document.createElement("BlockConnector");

		blockConnectorNode.setAttribute("con-block-id", ToBlockEditorParser.getAttribute(parentBlockNode, "id"));
		blockConnectorNode.setAttribute("connector-kind", "plug");
		blockConnectorNode.setAttribute("connector-type",plugType);
		blockConnectorNode.setAttribute("init-type", plugType);
		blockConnectorNode.setAttribute("label", "");
		blockConnectorNode.setAttribute("position-type", "mirror");
		
		plugNode.appendChild(blockConnectorNode);
		
		target.appendChild(plugNode);
	}
	
	public static List<Element> parseMethodCall(UniMethodCall method, Document document, Node parent){
		String genusName = calcMethodCallGenusName(method);
		String kind = "command";
		List<Element> exprs = new ArrayList<Element>();

		Element element = createBlockElement(document, genusName, ID_COUNTER++, kind);
		
		
		
		if(method.args != null){
			//引数パース
			for(UniExpr expr : method.args){
				for(Element arg : parseExpr(expr, document, element)){
					exprs.add(arg);	
				}
			}
			//socketの追加
			addSocketsNode(exprs, document, element);			
		}

		exprs.add(0, element);
		
		return exprs;
	}
	
	private static String calcMethodCallGenusName(UniMethodCall method){
		String genusName = "";
		//名前空間補完
		if(method.receiver instanceof UniIdent){
			if(((UniIdent)method.receiver).name.equals("MyLib")){
				genusName += "Turtle-";
			}
		}
		
		genusName += method.methodName + "[";		
		
		for(UniExpr arg : method.args){
			genusName += "@" + calcParamType(arg);
		}
		
		genusName += "]";
		
		return genusName;
	}
	
	private static String calcParamType(UniExpr param){
		String type = "";
		if(param instanceof UniStringLiteral){
			type = "string";
		}else{
			throw new RuntimeException("the param type calculate has not been supported yet");
		}
		return type;
	}
	
	public static void addBeforeBlockNode(Document document, Element blockNode,  String id){
		Element element = document.createElement("BeforeBlockId");
		element.setTextContent(id);
		
		blockNode.appendChild(element);
	}
	
	public static void addSocketsNode(List<Element> args, Document document, Element blockNode){
		Element sockets = document.createElement("Sockets");
		sockets.setAttribute("num-sockets", String.valueOf(args.size()));
		
		for(Element arg : args){
			addSocketNode(document, arg, sockets);	
		}
		
		blockNode.appendChild(sockets);
	}
	
	public static void addSocketNode(Document document, Element argElement, Node socketsNode){
		Element connector = document.createElement("BlockConnector");
		Node plugNode = ToBlockEditorParser.getChildNode(ToBlockEditorParser.getChildNode(argElement, "Plug"), "BlockConnector");
		
		connector.setAttribute("con-block-id", argElement.getAttribute("id"));
		connector.setAttribute("connector-kind", "socket");
		connector.setAttribute("connector-type", ToBlockEditorParser.getAttribute(plugNode, "connector-type"));
		connector.setAttribute("init-type", ToBlockEditorParser.getAttribute(plugNode, "init-type"));
		connector.setAttribute("label", "");
		connector.setAttribute("position-type", "single");
		
		socketsNode.appendChild(connector);
	}
	
	public static void addLocationElement(Document document, String x, String y, Element blockElement){
		Element locationElement = document.createElement("Location");
		Element xElement = document.createElement("X");
		Element yElement = document.createElement("Y");
		
		xElement.setTextContent(x);
		yElement.setTextContent(y);
		
		locationElement.appendChild(xElement);
		locationElement.appendChild(yElement);
		
		blockElement.appendChild(locationElement);
	}
	
	
	public static Element addLabelElement(Document document, String label, Element blockElement){
		Element element = document.createElement("Label");
		element.setTextContent(label);
		blockElement.appendChild(element);
		return element;
	}
	
	public static Element createBlockElement(Document document, String genusName, long id, String kind){
		Element element = document.createElement("Block");
		element.setAttribute("genus-name", genusName);
		element.setAttribute("id", String.valueOf(id));
		element.setAttribute("kind", kind);
		
		return element;
	}
	
	
	public static void parseBody(List<UniExpr> body){
		
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

	
	public  static Node getSaveNode(UniClassDec classDec) {
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
			
			//Pagesノードの作成
			Element pagesElement = document.createElement("Pages");
			pagesElement.setAttribute("collapsible-pages", "no");
			//Pageノードの作成
			Element pageElement = document.createElement("Page");
			pageElement.setAttribute("page-color", "40 40 40");			
			pageElement.setAttribute("page-drawer", "");
			pageElement.setAttribute("page-infullview", "yes");
			pageElement.setAttribute("page-name", classDec.className);
			pageElement.setAttribute("page-width", "1366");

			Element pageBlocksElement = document.createElement("PageBlocks");
			
			
			//クラスのパース
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
	
	public static void parseClass(UniClassDec classDec, Document document, Element pageElement){
		for(UniMemberDec member : classDec.members){
			if(member instanceof UniFuncDec){
				parseFunctionDec((UniFuncDec)member, document, pageElement);
			}
		}
	}
	
}
