package net.unicoen.parser.blockeditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BlockNameResolver {

	private String path = "ext/blocks/";

	private Map<String, String> turtleMethods = new HashMap<String, String>();
	private Map<String, Node> allAvailableBlocks = new HashMap<String, Node>();


	public BlockNameResolver(){
		parseGnuses();
		parseTurtleXml();
	}

	public BlockNameResolver(boolean isUseAtUNICOEN){
		if(isUseAtUNICOEN){
			path = "blockeditor/blocks/";
		}
		parseGnuses();
		parseTurtleXml();
	}

	public Map<String, String> getTurtleMethodsName(){
		return turtleMethods;
	}

	public Node getBlockNode(String genusName){
		return allAvailableBlocks.get(genusName);
	}

	/*
	 *	全ブロックをハッシュマップに登録する キー：genus-name 値:ノード
	 */
	public void parseGnuses(){
		DOMParser parser = new DOMParser();
		// lang_def.xmlを読み込む
		try {
			parser.parse(path + "lang_def.xml");

			Document doc = parser.getDocument();
			Element root = doc.getDocumentElement();

			NodeList genusNodes = root.getElementsByTagName("BlockGenus");

			for (int i = 0; i < genusNodes.getLength(); i++) { // find them
				Node node = genusNodes.item(i);
				allAvailableBlocks.put(ToBlockEditorParser.getAttribute(node, "name"), node);
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void parseTurtleXml() {
		DOMParser parser = new DOMParser();
		// lang_def.xmlを読み込む
		try {
			parser.parse(path + "method_lang_def.xml");

			Document doc = parser.getDocument();
			Element root = doc.getDocumentElement();

			NodeList genusNodes = root.getElementsByTagName("BlockGenus");

			for (int i = 0; i < genusNodes.getLength(); i++) { // find them
				Node node = genusNodes.item(i);

				if(ToBlockEditorParser.getChildNode(node, "Name") != null){
					turtleMethods.put(convertMethodName(ToBlockEditorParser.getAttribute(node, "name")), getNameSpaceString(node));
				}
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getNameSpaceString(Node node){
		String name = ToBlockEditorParser.getAttribute(node, "name");
		return name.substring(0, name.indexOf("-"));
	}

	private static String convertMethodName(String methodName){
		int namespaceIndex = methodName.indexOf("-");
		if(namespaceIndex>-1){
			return methodName.substring(namespaceIndex+1);
		}
		return methodName;
	}

	public String getNamespace(String name) {
		String namespace = turtleMethods.get(name);
		if(namespace != null){
			return namespace + "-";
		}
		return "";
	}

	public Node getPlugElement(String genusName){
		Node genusNode = allAvailableBlocks.get(genusName);
		Node plugNode = null;

		if(genusNode == null){
			return null;
		} else{
			Node socketConnectors = ToBlockEditorParser.getChildNode(genusNode, "BlockConnectors");
			for(int i = 0; i < socketConnectors.getChildNodes().getLength();i++){
				Node connector = socketConnectors.getChildNodes().item(i);
				if(connector.getNodeName().equals("BlockConnector") && ToBlockEditorParser.getAttribute(connector, "connector-kind").equals("plug")){
					plugNode = connector;
				}
			}
		}

		return plugNode;
	}

	public ArrayList<Node> getSocketNodes(String genusName){
		Node genusNode = allAvailableBlocks.get(genusName);
		ArrayList<Node> socketsNode = new ArrayList<>();

		if(genusNode == null){
			return null;
		} else{
			Node socketConnectors = ToBlockEditorParser.getChildNode(genusNode, "BlockConnectors");
			for(int i = 0; i < socketConnectors.getChildNodes().getLength();i++){
				Node connector = socketConnectors.getChildNodes().item(i);
				if(connector.getNodeName().equals("BlockConnector") && ToBlockEditorParser.getAttribute(connector, "connector-kind").equals("socket")){
					socketsNode.add(connector);
				}
			}
		}

		return socketsNode;
	}

}
