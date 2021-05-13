package com.macisdev.InvoicesServlet;

import com.macisdev.InvoicesServlet.webservicereference.PizzaShopService;
import com.macisdev.InvoicesServlet.webservicereference.PizzaShopWebService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet(name = "InvoiceServlet", value = "/invoice")
public class InvoiceServlet extends HttpServlet {

	public void init() {
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		//URL to call the servlet: localhost:8080/Gradle___com_macisdev___InvoicesServlet_0_1_war/invoice?id=1234

		//Gets the id of the order from the url
		String id = request.getParameter("id");

		//Sample order, real one should be gotten from the SOAP webservice
		Order orderSample = ParserXML.parseXmlToOrder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><order><order_info><order_datetime>1616518900531</order_datetime><customer_name>Miguel Ángel Macías</customer_name><customer_phone>649425570</customer_phone><delivery_method>Recoger en local</delivery_method><customer_address>Recoger en local</customer_address><payment_method>Efectivo</payment_method><order_status>0</order_status><total_price>91.90</total_price></order_info><pizza><code>1</code><name>Margarita</name><size>Mediana</size><extras>EXTRA: Jamón York </extras><price>6.0</price></pizza><pizza><code>6</code><name>Carbonara</name><size>Familiar</size><extras>EXTRA: Champiñones, Cebolla </extras><price>16.8</price></pizza><pizza><code>10</code><name>Mixta</name><size>Mediana</size><extras/><price>7.5</price></pizza><pizza><code>9</code><name>Hawaiana</name><size>Mediana</size><extras>\n" +
				"SIN: Piña</extras><price>6.5</price></pizza><pizza><code>103</code><name>Patatas con cheddar y bacon</name><size>Normal</size><extras/><price>4.5</price></pizza><pizza><code>105</code><name>Nuggets de pollo</name><size>Ración: 10 unidades</size><extras/><price>4.0</price></pizza><pizza><code>108</code><name>Salsas</name><size>Churrasco</size><extras/><price>1.1</price></pizza><pizza><code>201</code><name>Pasta carbonara</name><size>Tagliatelle</size><extras/><price>5.1</price></pizza><pizza><code>207</code><name>Serranito</name><size>Cerdo</size><extras/><price>4.0</price></pizza><pizza><code>205</code><name>Hamburguesa casera completa</name><size>Sin patatas</size><extras>\n" +
				"SIN: Tomate</extras><price>4.5</price></pizza><pizza><code>301</code><name>Coca-Cola</name><size>Zero Botella 2L</size><extras/><price>2.5</price></pizza><pizza><code>306</code><name>Cruzcampo</name><size>Litrona 1L</size><extras/><price>1.8</price></pizza><pizza><code>11</code><name>4 Estaciones</name><size>Mediana</size><extras>EXTRA: Gambas \n" +
				"SIN: Alcachofas</extras><price>7.8</price></pizza><pizza><code>8</code><name>Di Marco</name><size>Mediana</size><extras/><price>6.8</price></pizza><pizza><code>13</code><name>Vegetal</name><size>Mediana</size><extras/><price>6.5</price></pizza><pizza><code>14</code><name>Andaluza</name><size>Mediana</size><extras/><price>6.5</price></pizza></order>",
				ParserXML.WEBSERVICE);
		orderSample.setOrderId(id);

		//Connect to the webservice
		String soapEndpointUrl = "http://localhost:8080/PizzaShopWebService/PizzaShopWebService";
		String soapAction = "";

		//callSoapWebService(soapEndpointUrl, soapAction);

		PizzaShopWebService psws = new PizzaShopWebService();
		PizzaShopService pizzaShopService = psws.getPizzaShopServicePort();

		String order = pizzaShopService.getStoredOrder(id);
		System.out.println(order);

		//Generates the invoice

		//Real order
		InvoiceGenerator generator = new InvoiceGenerator(ParserXML.parseXmlToOrder(order, ParserXML.RESTAURANT));
		generator.generateInvoice("12/04/2021", "invoices");
		//sample order
		//InvoiceGenerator generatorSample = new InvoiceGenerator(orderSample);
		//generator.generateInvoice("19/04/2021", "invoices");


		//Set the type of file to download
		response.setContentType("application/pdf");

		//Configure the download
		response.setHeader("Content-disposition", String.format("inline; filename=%s.pdf", id));

		//File to download
		File invoice = new File(String.format("%s/%s.pdf", "invoices", id));
		System.out.println(invoice.createNewFile());

		//Sends the pdf file to the browser
		OutputStream out = response.getOutputStream();
		FileInputStream in = new FileInputStream(invoice);
		byte[] buffer = new byte[4096];
		int length;
		while ((length = in.read(buffer)) > 0){
			out.write(buffer, 0, length);
		}
		in.close();
		out.flush();
	}

	public void destroy() {
	}

	private static void createSoapEnvelope(SOAPMessage soapMessage) throws SOAPException {
		SOAPPart soapPart = soapMessage.getSOAPPart();

		String myNamespace = "piz";
		String myNamespaceURI = "http://pizzashopwebservice.macisdev.com/";

		// SOAP Envelope
		SOAPEnvelope envelope = soapPart.getEnvelope();
		envelope.addNamespaceDeclaration(myNamespace, myNamespaceURI);

            /*
            Constructed SOAP Request Message:
            <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:myNamespace="http://www.webserviceX.NET">
                <SOAP-ENV:Header/>
                <SOAP-ENV:Body>
                    <myNamespace:GetInfoByCity>
                        <myNamespace:USCity>New York</myNamespace:USCity>
                    </myNamespace:GetInfoByCity>
                </SOAP-ENV:Body>
            </SOAP-ENV:Envelope>
            */

		// SOAP Body
		SOAPBody soapBody = envelope.getBody();
		SOAPElement soapBodyElem = soapBody.addChildElement("getStoredOrder", myNamespace);
		SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("arg0");
		soapBodyElem1.addTextNode("00009-357");
	}

	private static void callSoapWebService(String soapEndpointUrl, String soapAction) {
		try {
			// Create SOAP Connection
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			SOAPConnection soapConnection = soapConnectionFactory.createConnection();

			// Send SOAP Message to SOAP Server
			SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(soapAction), soapEndpointUrl);

			// Print the SOAP Response
			System.out.println("Response SOAP Message:");
			soapResponse.writeTo(System.out);
			System.out.println();

			soapConnection.close();
		} catch (Exception e) {
			System.err.println("\nError occurred while sending SOAP Request to Server!\nMake sure you have the correct endpoint URL and SOAPAction!\n");
			e.printStackTrace();
		}
	}

	private static SOAPMessage createSOAPRequest(String soapAction) throws Exception {
		MessageFactory messageFactory = MessageFactory.newInstance();
		SOAPMessage soapMessage = messageFactory.createMessage();

		createSoapEnvelope(soapMessage);

		MimeHeaders headers = soapMessage.getMimeHeaders();
		headers.addHeader("SOAPAction", soapAction);

		soapMessage.saveChanges();

		/* Print the request message, just for debugging purposes */
		System.out.println("Request SOAP Message:");
		soapMessage.writeTo(System.out);
		System.out.println("\n");

		return soapMessage;
	}
}