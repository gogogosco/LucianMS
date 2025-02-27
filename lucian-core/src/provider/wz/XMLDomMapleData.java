/*
	This file is part of the OdinMS Maple Story Server
    Copyright (C) 2008 Patrick Huy <patrick.huy@frz.cc>
		       Matthias Butz <matze@odinms.de>
		       Jan Christian Meyer <vimes@odinms.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation version 3 as published by
    the Free Software Foundation. You may not use, modify or distribute
    this program under any other version of the GNU Affero General Public
    License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package provider.wz;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import provider.MapleData;
import provider.MapleDataEntity;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class XMLDomMapleData implements MapleData {

    private Node node;
    private File file;

    /**
     * Usage for parsing documents provided by HaRepacker's XML serializer in attempts to decode using a specific charset
     */
    public XMLDomMapleData(Reader reader, File file) throws ParserConfigurationException, IOException, SAXException {
        this.file = file;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(reader));
        node = document.getFirstChild();
    }

    public XMLDomMapleData(FileInputStream fis, File file) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        DocumentBuilder b = f.newDocumentBuilder();
        Document document = b.parse(fis);
        this.node = document.getFirstChild();
        this.file = file;
    }

    private XMLDomMapleData(Node node) {
        this.node = node;
    }

    @Override
    public String toString() {
        Node name = node.getAttributes().getNamedItem("name");
        Node value = node.getAttributes().getNamedItem("value");
        return String.format("XMLDomMapleData(<%s %s %s/>)", node.getNodeName(), name, value);
    }

    @Override
    public MapleData getChildByPath(String path) {
        String segments[] = path.split("/");
        if (segments[0].equals("..")) {
            return ((MapleData) getParent()).getChildByPath(path.substring(path.indexOf("/") + 1));
        }

        Node myNode = node;
        for (String segment : segments) {
            NodeList childNodes = myNode.getChildNodes();
            boolean foundChild = false;
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode == null) {
                    continue;
                }
                NamedNodeMap attr = childNode.getAttributes();
                if (childNode.getNodeType() == Node.ELEMENT_NODE
                        && attr.getNamedItem("name") != null && attr.getNamedItem("name").getNodeValue().equals(segment)) {
                    myNode = childNode;
                    foundChild = true;
                    break;
                }
            }
            if (!foundChild) {
                return null;
            }
        }
        XMLDomMapleData ret = new XMLDomMapleData(myNode);
        ret.file = new File(file, getName() + "/" + path).getParentFile();
        return ret;
    }

    @Override
    public List<MapleData> getChildren() {
        List<MapleData> ret = new ArrayList<>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                XMLDomMapleData child = new XMLDomMapleData(childNode);
                child.file = new File(file, getName());
                ret.add(child);
            }
        }
        return ret;
    }

    @Override
    public Object getData() {
        NamedNodeMap attributes = node.getAttributes();
        MapleDataType type = getType();
        switch (type) {
            case DOUBLE:
            case FLOAT:
            case INT:
            case SHORT:
            case STRING:
            case UOL: {
                String value = attributes.getNamedItem("value").getNodeValue();
                switch (type) {
                    case DOUBLE:
                        if (value.contains(",")) {
                            value = value.replaceAll(",", ".");
                        }
                        return Double.parseDouble(value);
                    case FLOAT:
                        if (value.contains(",")) {
                            value = value.replaceAll(",", ".");
                        }
                        return Float.parseFloat(value);
                    case INT:
                        return Integer.parseInt(value);
                    case SHORT:
                        return Short.parseShort(value);
                    case STRING:
                    case UOL:
                        return value;
                    default:
                        break;
                }
            }
            case VECTOR: {
                String x = attributes.getNamedItem("x").getNodeValue();
                String y = attributes.getNamedItem("y").getNodeValue();
                return new Point(Integer.parseInt(x), Integer.parseInt(y));
            }
            case CANVAS: {
                String width = attributes.getNamedItem("width").getNodeValue();
                String height = attributes.getNamedItem("height").getNodeValue();
                return new FileStoredPngMapleCanvas(Integer.parseInt(width), Integer.parseInt(height), new File(file, getName() + ".png"));
            }
            default:
                return null;
        }
    }

    @Override
    public String getValue() {
        return node.getAttributes().getNamedItem("value").getNodeValue();
    }

    @Override
    public MapleDataType getType() {
        String nodeName = node.getNodeName();
        switch (nodeName) {
            case "imgdir":
                return MapleDataType.PROPERTY;
            case "canvas":
                return MapleDataType.CANVAS;
            case "convex":
                return MapleDataType.CONVEX;
            case "sound":
                return MapleDataType.SOUND;
            case "uol":
                return MapleDataType.UOL;
            case "double":
                return MapleDataType.DOUBLE;
            case "float":
                return MapleDataType.FLOAT;
            case "int":
                return MapleDataType.INT;
            case "short":
                return MapleDataType.SHORT;
            case "string":
                return MapleDataType.STRING;
            case "vector":
                return MapleDataType.VECTOR;
            case "null":
                return MapleDataType.IMG_0x00;
        }
        return null;
    }

    @Override
    public MapleDataEntity getParent() {
        Node parentNode = node.getParentNode();
        if (parentNode.getNodeType() == Node.DOCUMENT_NODE) {
            return null;
        }
        XMLDomMapleData parentData = new XMLDomMapleData(parentNode);
        parentData.file = file.getParentFile();
        return parentData;
    }

    @Override
    public String getName() {
        return node.getAttributes().getNamedItem("name").getNodeValue();
    }

    @Override
    public Iterator<MapleData> iterator() {
        return getChildren().iterator();
    }

    public String getValue(String property) {
        return node.getAttributes().getNamedItem(property).getNodeValue();
    }
}
