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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.unicoen.node.UniClassDec;
import net.unicoen.node.UniExpr;
import net.unicoen.node.UniFuncDec;
import net.unicoen.node.UniMemberDec;

public class UniToBlockParser {
	
	/** Name space definition */
	private static String XML_CODEBLOCKS_NS = "http://education.mit.edu/openblocks/ns";
	
	/** Location of schema */
	private static String XML_CODEBLOCKS_SCHEMA_URI = "http://education.mit.edu/openblocks/codeblocks.xsd";
	
	private static long ID_COUNTER = 1000; 
	

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
		Element labelElement = createLabelElement(document, funcDec.funcName);
		
		
		//Location?
		Element locationElement = createLocationElement(document, funcDec);
		//Plug
		//Socket
		
		procedureElement.appendChild(labelElement);
		procedureElement.appendChild(locationElement);
		
		blocks.add(procedureElement);
		//funcDec.args　引数のパース
		
		//funcDec.body ボディのパース

		
		
		for(Element element : blocks){
			pageElement.appendChild(element);
		}
	}
	
	public static Element createLocationElement(Document document, UniFuncDec funcDec){
		Element locationElement = document.createElement("Location");
		Element xElement = document.createElement("X");
		Element yElement = document.createElement("Y");
		
		xElement.setTextContent("50");
		yElement.setTextContent("50");
		
		locationElement.appendChild(xElement);
		locationElement.appendChild(yElement);
		
		return locationElement;
	}
	
	
	public static Element createLabelElement(Document document, String label){
		Element element = document.createElement("Label");
		element.setTextContent(label);
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
