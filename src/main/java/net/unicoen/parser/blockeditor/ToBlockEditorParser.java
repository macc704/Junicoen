package net.unicoen.parser.blockeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.unicoen.node.UniNode;

public class ToBlockEditorParser {
	public static List<UniNode> parse(File xmlFile) {
		Document d = toXmlDoc(xmlFile);
		
		NodeList list = d.getElementsByTagName("PageBlocks");
		if (list.getLength() != 1) return null;
		Node pageBlock = list.item(0);
		
		String name = pageBlock.getNodeName();
		
		return null;
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
}
