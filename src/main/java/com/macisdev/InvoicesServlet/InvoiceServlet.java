package com.macisdev.InvoicesServlet;

import com.macisdev.InvoicesServlet.webservicereference.PizzaShopService;
import com.macisdev.InvoicesServlet.webservicereference.PizzaShopWebService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

		PizzaShopWebService psws = new PizzaShopWebService();
		PizzaShopService pizzaShopService = psws.getPizzaShopServicePort();

		String order = pizzaShopService.getStoredOrder(id);
		System.out.println(order);

		//Generates the invoice
		InvoiceGenerator generator = new InvoiceGenerator(ParserXML.parseXmlToOrder(order, ParserXML.RESTAURANT));
		generator.generateInvoice("12/04/2021", "invoices");

		//Set the type of file to download
		response.setContentType("application/pdf");

		//Configure the download
		response.setHeader("Content-Disposition", String.format("inline; filename=%s.pdf", id));

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
}