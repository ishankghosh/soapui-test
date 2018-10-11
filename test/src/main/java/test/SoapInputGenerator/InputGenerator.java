package test.SoapInputGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
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
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import org.apache.xmlbeans.XmlException;

import com.eviware.soapui.impl.wsdl.WsdlInterface;
import com.eviware.soapui.impl.wsdl.WsdlOperation;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlImporter;
import javax.wsdl.Service;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPOperation;

import com.eviware.soapui.model.iface.Operation;
import com.eviware.soapui.support.SoapUIException;

public class InputGenerator {

	public static void main(String args[]) throws Exception {
		String fileName = "F://Test//SI_ContractAccount_OUTService.wsdl";
		String xmlFileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
		generateRequestResponseXMLs(fileName, xmlFileName);
		generateCSV(fileName, xmlFileName);
	}

	private static void generateCSV(String fileName, String xmlFileName) throws Exception {
		File file = new File(xmlFileName + ".csv");
		if (file.exists()) {
			file.delete();
			file = new File(xmlFileName +".csv");
		}
		FileOutputStream is = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(is);
		Writer w = new BufferedWriter(osw);
		w.write("Web service" + ", " + "SOAPACTION" + ", "
				+ "OPERATION" + ", " + "NAMESPACE URI");
		
		WSDLFactory factory = WSDLFactory.newInstance();
		WSDLReader reader = factory.newWSDLReader();
		Definition definition = reader.readWSDL(fileName);
		Map services = definition.getServices();
		Map bindings = definition.getAllBindings();
		// Map ports = definition.getAllPortTypes();
		Iterator servicesIterator = services.values().iterator();
		Iterator bindingsIterator = bindings.values().iterator();
		// Iterator portsIterator = ports.values().iterator();
		while (servicesIterator.hasNext()) {
			Service service = (Service) servicesIterator.next();
			Map ports = service.getPorts();
			Iterator portsIterator = ports.keySet().iterator();

			// System.out.println(service.getQName().getLocalPart());

			while (portsIterator.hasNext()) {
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

							// ....

							if (extElement instanceof SOAPOperation) {
								SOAPOperation soapOp = (SOAPOperation) extElement;
								System.out.println(service.getQName().getLocalPart() + ", " + soapOp.getSoapActionURI() + ", "
										+ operation.getName() + ", " + soapOp.getElementType().getNamespaceURI() + "\n\n\n");
								w.write(service.getQName().getLocalPart() + ", " + soapOp.getSoapActionURI() + ", "
										+ operation.getName() + ", " + soapOp.getElementType().getNamespaceURI());
								w.write("\r\n");
							}

							// ....
						}
					}

					//System.out.println(service.getQName() + ", " + operation.getOperation());
				}
			}
		}
		w.close();

	}

	private static void generateRequestResponseXMLs(String fileName, String xmlFileName) throws Exception {
		File file = new File(xmlFileName + ".xml");
		if (file.exists()) {
			file.delete();
			file = new File(xmlFileName +".xml");
		}
		FileOutputStream is = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(is);
		Writer w = new BufferedWriter(osw);

		WsdlProject project = new WsdlProject();
		WsdlInterface[] wsdls = WsdlImporter.importWsdl(project, fileName);
		WsdlInterface wsdl = wsdls[0];
		for (Operation operation : wsdl.getOperationList()) {
			WsdlOperation wsdlOperation = (WsdlOperation) operation;
			System.out.println("<" + wsdlOperation.getName() + ">");
			w.write("OP:" + wsdlOperation.getName());
			w.write("\r\n");
			System.out.println("<" + "Request" + ">");
			System.out.println(wsdlOperation.createRequest(true));
			w.write("Request:" + wsdlOperation.createRequest(true));
			System.out.println("</" + "Request" + ">");

			w.write("\r\n");
			System.out.println("<" + "Response"+ ">");
			System.out.println(wsdlOperation.createResponse(true));
			w.write("Response:" + wsdlOperation.createResponse(true));
			System.out.println("</" + "Response"+ ">");

			System.out.println("</" + wsdlOperation.getName() + ">");

			w.write("\r\n");
		}
		w.close();

	}

}
