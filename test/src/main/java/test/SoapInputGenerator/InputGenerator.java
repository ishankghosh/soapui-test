package test.SoapInputGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import com.eviware.soapui.model.iface.Operation;

public class InputGenerator {

	public static void main(String args[]) throws Exception {
		//String fileName = "F://Test//SI_ContractAccount_OUTService.wsdl";
		String fileName = "https://raw.githubusercontent.com/ishankghosh/soapui-test/master/redetails/SI_ContractAccount_OUTService.wsdl";
		//String fileName = "https://raw.githubusercontent.com/ishankghosh/soapui-test/master/redetails/SI_BusinessPartner_OUTService.wsdl";
		String xmlFileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
		String xsdPath = modifyXML(fileName, xmlFileName, "xsd:import", "schemaLocation");
		String endpointURL = modifyXML(fileName, xmlFileName, "soap:address", "location");
		System.out.println("XSDPATH::" + xsdPath);
		generateRequestResponseXMLs(fileName, xmlFileName);
		generateCSV(fileName, xmlFileName, xsdPath, endpointURL);
	}

	private static String modifyXML(String fileName, String xmlFileName, String tag, String attribute)
			throws ParserConfigurationException, SAXException, IOException {
		String attributeValue = null;
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(fileName);
		DocumentTraversal traversal = (DocumentTraversal) doc;
		Node a = doc.getDocumentElement();
		NodeIterator iterator = traversal.createNodeIterator(a, NodeFilter.SHOW_ELEMENT, null, true);
		Element b = null;
		for (Node n = iterator.nextNode(); n != null; n = iterator.nextNode()) {
			Element e = (Element) n;
			if (n.getNodeName().equalsIgnoreCase(tag)) {
				attributeValue = e.getAttribute(attribute);
				break;
			}
		}
		if (attributeValue == null || attributeValue.equalsIgnoreCase(""))
			return "NA";
		else
			return attributeValue;
	}

	private static void generateCSV(String fileName, String xmlFileName, String xsdPath, String endpointURL) throws Exception {

		File file = new File("csv//" + xmlFileName + ".csv");
		if (file.exists()) {
			file.delete();
			file = new File("csv//" + xmlFileName + ".csv");
		}
		FileOutputStream is = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(is);
		Writer w = new BufferedWriter(osw);
		w.write("Service Name" + ", " + "Operation Name" + ", " + "SOAP Action"
				  + "," + "NameSpace Uri" + ", " + "Endpoint URL" + ", " + "Xsd Path");
		w.write("\r\n");

		WSDLFactory factory = WSDLFactory.newInstance();
		WSDLReader reader = factory.newWSDLReader();
		Definition definition = reader.readWSDL(fileName);
		Map services = definition.getServices();
		Map bindings = definition.getAllBindings();
		// Map ports = definition.getAllPortTypes();
		Iterator servicesIterator = services.values().iterator();
		Iterator bindingsIterator = bindings.values().iterator();
		// Iterator portsIterator = ports.values().iterator();
		if (servicesIterator.hasNext()) {
			Service service = (Service) servicesIterator.next();
			Map ports = service.getPorts();
			Iterator portsIterator = ports.keySet().iterator();
			if (portsIterator.hasNext()) {
				String strPort = portsIterator.next().toString();
				Port port = service.getPort(strPort);
				Binding binding = port.getBinding();
				PortType portType = binding.getPortType();
				List operations = binding.getBindingOperations();
				Iterator opIterator = operations.iterator();
				while (opIterator.hasNext()) {
					BindingOperation operation = (BindingOperation) opIterator.next();
					List extensions = operation.getExtensibilityElements();
					if (extensions != null) {
						for (int i = 0; i < extensions.size(); i++) {
							ExtensibilityElement extElement = (ExtensibilityElement) extensions.get(i);
							if (extElement instanceof SOAPOperation) {
								SOAPOperation soapOp = (SOAPOperation) extElement;
								String serviceName;
								String operationName;
								String soapUri;
								String nameSpaceUri;
								if(service != null && service.getQName()!=null && service.getQName().getLocalPart()!= null)
									serviceName = service.getQName().getLocalPart();
								else
									serviceName = "NA";
								if(operation != null && operation.getName()!= null)
									operationName = operation.getName();
								else
									operationName = "NA";
								if(soapOp != null && soapOp.getSoapActionURI()!= null)
									soapUri = soapOp.getSoapActionURI();
								else
									soapUri = "NA";
								if(soapOp != null && soapOp.getElementType() != null && soapOp.getElementType().getNamespaceURI()!= null)
									nameSpaceUri = soapOp.getElementType().getNamespaceURI();
								else
									nameSpaceUri = "NA";
								w.write(serviceName + ", " + operationName + ", " + soapUri
								  + "," + nameSpaceUri + ", " + endpointURL + ", " + xsdPath);
								w.write("\r\n");
							}
						}
					}
				}
			}
		}
		w.close();

	}

	private static void generateRequestResponseXMLs(String fileName, String xmlFileName) throws Exception {
		WsdlProject project = new WsdlProject();
		WsdlInterface[] wsdls = WsdlImporter.importWsdl(project, fileName);
		WsdlInterface wsdl = wsdls[0];
		for (Operation operation : wsdl.getOperationList()) {
			WsdlOperation wsdlOperation = (WsdlOperation) operation;
			File requestFile = new File("xml//" + "Request" + "_"+ xmlFileName + "_" + wsdlOperation.getName() + ".xml");
			File responseFile = new File(
					"xml//" + "Response" + "_"+ xmlFileName + "_" + wsdlOperation.getName() + ".xml");
			
			if (requestFile.exists()) {
				requestFile.delete();
				requestFile = new File("xml//" + "Request" + "_"+ xmlFileName + "_" + wsdlOperation.getName() + ".xml");
			}
			if (responseFile.exists()) {
				responseFile.delete();
				responseFile = new File("xml//" + "Response" + "_"+ xmlFileName + "_" + wsdlOperation.getName() + ".xml");
			}
			FileOutputStream isReq = new FileOutputStream(requestFile);
			FileOutputStream isRes = new FileOutputStream(responseFile);
			OutputStreamWriter oswReq = new OutputStreamWriter(isReq);
			OutputStreamWriter oswRes = new OutputStreamWriter(isRes);
			
			Writer reqWriter = new BufferedWriter(oswReq);
			Writer resWriter = new BufferedWriter(oswRes);
			
			reqWriter.write("<Request>" + wsdlOperation.createRequest(true) + "</Request>");
			resWriter.write("<Response>" + wsdlOperation.createResponse(true) + "</Response>");
			reqWriter.close();
			resWriter.close();

		}

	}

}
