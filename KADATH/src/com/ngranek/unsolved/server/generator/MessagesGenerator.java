/***************************************************************************
 *   Copyright (C) 2007 by Francisco Andrades Grassi                       *
 *   bigjocker@gmail.com                                                   *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 *   This program is distributed in the hope that it will be useful,       *
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of        *
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         *
 *   GNU General Public License for more details.                          *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this program; if not, write to the                         *
 *   Free Software Foundation, Inc.,                                       *
 *   59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.             *
 ***************************************************************************/

package com.ngranek.unsolved.server.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MessagesGenerator {
	private String fileName = null;

	private String javaTemplate = null;
	private String javaIndexerTemplate = null;
	
	private String cClassTemplate = null;
	private String cFileTemplate = null;

	public static void main(String args[]) throws Exception {
		new MessagesGenerator("messages.xml").execute();
	}

	public MessagesGenerator(String fileName) throws Exception {
		this.fileName = fileName;

		javaTemplate = FileUtils.readFileToString(new File("message.java.template"));
		javaIndexerTemplate = FileUtils.readFileToString(new File("indexer.java.template"));
		
		cClassTemplate = FileUtils.readFileToString(new File("message.c.template"));
		cFileTemplate = FileUtils.readFileToString(new File("messages.c.template"));
	}

	public void execute() throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new File(fileName));

		String javaIndexerSwitchCode = "";
		
		String cInstancerCode = "";
		String cDispatcherCode = "";
		String cClassesCode = "";

		NodeList messageNodes = doc.getElementsByTagName("message");
		for (int i = 0; i < messageNodes.getLength(); i++) {
			Node messageNode = messageNodes.item(i);

			String javaReaderCode = "";
			String javaWriterCode = "";
			String javaFieldsCode = "";
			
			String cVarsCode = "";
			String cInitCode = "";
			String cAccessorsCode = "";
			String cWriterCode = "";
			String cReaderCode = "";

			if (messageNode.getNodeType() == Node.ELEMENT_NODE) {
				NodeList messageFields = messageNode.getChildNodes();
				for (int j = 0; j < messageFields.getLength(); j++) {
					Node fieldNode = messageFields.item(j);
					
					if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
						//System.out.println(fieldNode.getAttributes());

						String name = fieldNode.getAttributes().getNamedItem("name").getNodeValue();
						String type = fieldNode.getAttributes().getNamedItem("type").getNodeValue();

						if (type.equals("int")) {
							javaReaderCode += "\t\t" + name + " = dis.readInt();\n";
							javaWriterCode += "\t\tdos.writeInt(" + name + ");\n";
							javaFieldsCode += generateJavaFieldCode("int", name);
							
							cVarsCode += "\t\tint " + name + ";\n";
							cInitCode += "\t\t\t" + name + "= 0;\n";
							cAccessorsCode += generateCAccessorsCode("int", name);
							cWriterCode += "\t\t\tnetworkManager->addParameterToMessage(message->m_pParams, PARAM_TYPE_INT, (long) " + name + ", 0);\n";
							cReaderCode += "\t\t\t" + name + " = networkManager->getInt32(bt, pos);\npos += 4;\n";
						} else if (type.equals("long")) {
							javaReaderCode += "\t\t" + name + " = dis.readLong();\n";
							javaWriterCode += "\t\tdos.writeLong(" + name + ");\n";
							javaFieldsCode += generateJavaFieldCode("long", name);
							
							cVarsCode += "long " + name + ";\n";
							cInitCode += name + "= 0;\n";
							cAccessorsCode += generateCAccessorsCode("long", name);
							cWriterCode += "networkManager->addParameterToMessage(message->m_pParams, PARAM_TYPE_LONG, (long) " + name + ", 0);\n";
							cReaderCode += name + " = networkManager->getInt32(bt, pos);\npos += 4;\n";
						} else if (type.equals("byte")) {
							javaReaderCode += "\t\t" + name + " = dis.readByte();\n";
							javaWriterCode += "\t\tdos.writeByte(" + name + ");\n";
							javaFieldsCode += generateJavaFieldCode("byte", name);
							
							cVarsCode += "byte " + name + ";\n";
							cInitCode += name + "= 0;\n";
							cAccessorsCode += generateCAccessorsCode("byte", name);
							cWriterCode += "networkManager->addParameterToMessage(message->m_pParams, PARAM_TYPE_BYTE, (long) " + name + ", 0);\n";
							cReaderCode += name + " = networkManager->getByte(bt, pos);\npos += 1;\n";
						} else if (type.equals("String")) {
							javaReaderCode += "\t\t" + name + " = dis.readUTF();\n";
							javaWriterCode += "\t\tdos.writeUTF(" + name + ");\n";
							javaFieldsCode += generateJavaFieldCode("String", name);
							
							cVarsCode += "string " + name + ";\n";
							//cInitCode += name + "= \"\";\n";
							cAccessorsCode += generateCAccessorsCode("string", name);
							cWriterCode += "networkManager->addParameterToMessage(message->m_pParams, PARAM_TYPE_STRING, (long) &" + name + ", " + name + ".length());\n";
							
							cReaderCode += "size = networkManager->getInt16(bt, pos);\n";
							cReaderCode += "pos += 2;\n";
							cReaderCode += "str = new char[size];\n";
							cReaderCode += "networkManager->getString(str, pos, size, bt);\n";
							cReaderCode += name + " = str;\n";
							cReaderCode += "delete str;\n";
							cReaderCode += "pos += size;\n\n";
						} else if (type.equals("boolean")) {
							javaReaderCode += "\t\t" + name + " = dis.readByte() == 1;\n";
							javaWriterCode += "\t\tdos.writeByte(" + name + "?1:0);\n";
							javaFieldsCode += generateJavaFieldCode("boolean", name);
							
							cVarsCode += "bool " + name + ";\n";
							cInitCode += name + "= false;\n";
							cAccessorsCode += generateCAccessorsCode("bool", name);
							cWriterCode += "networkManager->addParameterToMessage(message->m_pParams, PARAM_TYPE_BYTE, (long) (" + name + " ? 1 : 0), 0);\n";
							cReaderCode += name + " = networkManager->getByte(bt, pos) == 1;\npos += 1;\n";
						}
					}
				}
				String messageClassName = messageNode.getAttributes().getNamedItem("name").getNodeValue();
				
				cInitCode += "name = \"" + messageClassName + "\";\nmessageId = " + i + ";\n";
				
				cInstancerCode += "case " + i + ":\nmessage = new " + messageClassName + "();\nbreak;\n";
				cDispatcherCode += "case " + i + ":\nconsume" + messageClassName + "((" + messageClassName + " *) message);\nbreak;\n";
				
				cClassesCode += MessageFormat.format(cClassTemplate, new Object[] {messageClassName, cVarsCode, cInitCode, cAccessorsCode, cWriterCode, cReaderCode});
				
				String javaCode = MessageFormat.format(javaTemplate, new Object[] { messageClassName, i, javaReaderCode, javaWriterCode,
						javaFieldsCode });
				FileOutputStream fos = new FileOutputStream(new File("src/com/ngranek/unsolved/server/messages/" + messageClassName + ".java"));
				fos.write(javaCode.getBytes());
				fos.close();

				javaIndexerSwitchCode += "if (id == " + i + ") {return new " + messageClassName + "();}\n";
			}

			String javaIndexerCode = MessageFormat.format(javaIndexerTemplate, new Object[] { javaIndexerSwitchCode });
			FileOutputStream fos = new FileOutputStream(new File("src/com/ngranek/unsolved/server/messages/MessageIndexer.java"));
			fos.write(javaIndexerCode.getBytes());
			fos.close();
		}
		
		String cCode = MessageFormat.format(cFileTemplate, new Object[] {cClassesCode, cInstancerCode, cDispatcherCode});
		FileOutputStream fos = new FileOutputStream(new File("/home/bigjocker/code/ogre/unsolved/src/network-autogen.h"));
		fos.write(cCode.getBytes());
		fos.close();
	}

	public String generateJavaFieldCode(String type, String name) {
		String javaFieldsCode = "";

		String mName = firstToUpper(name);

		javaFieldsCode += "\tprivate " + type + " " + name + ";\n";
		javaFieldsCode += "\tpublic void set" + mName + "(" + type + " " + name + ") {\n";
		javaFieldsCode += "\t\nthis." + name + " = " + name + ";\n";
		javaFieldsCode += "\t}\n";
		javaFieldsCode += "\tpublic " + type + " get" + mName + "() {\n";
		javaFieldsCode += "\t\treturn " + name + ";\n";
		javaFieldsCode += "\t}\n";

		return javaFieldsCode;
	}
	
	public String generateCAccessorsCode(String type, String name) {
		String code = "";

		String mName = firstToUpper(name);

		code += "\tvoid set" + mName + "(" + type + " " + name + ") {\n";
		code += "\t\nthis->" + name + " = " + name + ";\n";
		code += "\t}\n";
		code += "\t" + type + " get" + mName + "() {\n";
		code += "\t\treturn " + name + ";\n";
		code += "\t}\n";

		return code;
	}

	public String firstToUpper(String text) {
		return Character.toUpperCase(text.charAt(0)) + text.substring(1);
	}
}
