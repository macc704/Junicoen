package net.unicoen.parser.blockeditor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class BlockNameResolver {

	private static String path = "blockeditor/blocks/";
	
	private static Map<String, String> turtleMethods = new HashMap<String, String>();
	
	public static Map<String, String> getTurtleMethodsName(){
		return turtleMethods;
	}

	public static void parseTurtleXml() {
		
		DOMParser parser = new DOMParser();
		// lang_def.xmlを読み込む
		try {
			parser.parse(path + "lang_def.xml");

			Document doc = parser.getDocument();
			Element root = doc.getDocumentElement();

			NodeList genusNodes = root.getElementsByTagName("BlockGenus");
			
			for (int i = 0; i < genusNodes.getLength(); i++) { // find them
				Node node = genusNodes.item(i);
				if(ToBlockEditorParser.getChildNode(node, "Name") != null){
					turtleMethods.put(convertMethodName(ToBlockEditorParser.getAttribute(node, "name")), getNameSpace(node));
				}
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getNameSpace(Node node){
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

}
