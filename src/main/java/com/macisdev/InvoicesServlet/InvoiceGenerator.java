package com.macisdev.InvoicesServlet;

import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Locale;

public class InvoiceGenerator {
	private static final  String BUSINESS_NAME = "Pizzería Giuseppe SL";
	private static final  String BUSINESS_TAX_NUMBER = "45814859-D";
	private static final  String BUSINESS_ADDRESS = "C/Real 28, Gelves 41120, Sevilla";
	private static final  String BUSINESS_PHONE = "955760472";

	private final Order order;

	public InvoiceGenerator(Order order) {
		this.order = order;
	}

	public void generateInvoice(String date, String destDirectory) throws FileNotFoundException {
		//Creates the document objects
		File destFile = new File(destDirectory, order.getOrderId() + ".pdf");
		PdfWriter writer = new PdfWriter(destFile);
		PdfDocument pdf = new PdfDocument(writer);
		Document document = new Document(pdf);

		//Header of the pdf (business information)
		Paragraph header = new Paragraph();
		header.add(new Text(BUSINESS_NAME).setFontSize(22));
		header.add("\n");
		header.add("CIF: ");
		header.add(BUSINESS_TAX_NUMBER);
		header.add("\n");
		header.add(BUSINESS_ADDRESS);
		header.add("\n");
		header.add(BUSINESS_PHONE);
		header.setBorder(new SolidBorder(0.6f));
		header.setPaddingLeft(10);

		//The information of the order itself
		Paragraph orderInfo = new Paragraph();
		orderInfo.add("Nº Pedido: ");
		orderInfo.add(order.getOrderId());
		orderInfo.add("\n");

		orderInfo.add("Fecha: ");
		orderInfo.add(date);
		orderInfo.add("\n");

		orderInfo.add("Cliente: ");
		orderInfo.add(order.getCustomerName());
		orderInfo.add("\n");

		orderInfo.add("Teléfono: ");
		orderInfo.add(order.getCustomerPhone());
		orderInfo.add("\n");

		//The table of order elements
		Paragraph orderContentHeader = new Paragraph("Contenido del pedido:");

		//Sets the relative size of each column
		Table orderContent = new Table(new float[]{1, 3, 3, 4, 1}).setFontSize(10f);
		orderContent.setWidth(UnitValue.createPercentValue(100));

		//Creates the header of the table
		orderContent.addHeaderCell("Cód.");
		orderContent.addHeaderCell("Nombre");
		orderContent.addHeaderCell("Tamaño");
		orderContent.addHeaderCell("Extras");
		orderContent.addHeaderCell("Precio");

		//Sets the properties of the header
		orderContent.getHeader().setBackgroundColor(new DeviceGray(0.85f));
		orderContent.getHeader().setBold();

		//Add each order element as a row
		for (OrderElement orderElement : order.getOrderElements()) {
			orderContent.addCell(orderElement.getCode());
			orderContent.addCell(orderElement.getName());
			orderContent.addCell(orderElement.getSize());
			orderContent.addCell(orderElement.getExtras().trim());
			orderContent.addCell(String.format(Locale.getDefault(), "%.2f€", orderElement.getPrice()));
		}

		orderContent.addCell("");
		orderContent.addCell("");
		orderContent.addCell("");
		orderContent.addCell("Base Imponible");
		orderContent.addCell(String.format(Locale.getDefault(), "%.2f€", order.getTotalPrice() / 1.10));

		orderContent.addCell("");
		orderContent.addCell("");
		orderContent.addCell("");
		orderContent.addCell("IVA 10%");
		orderContent.addCell(String.format(Locale.getDefault(), "%.2f€",
				order.getTotalPrice() - (order.getTotalPrice() / 1.10)));

		orderContent.addCell("");
		orderContent.addCell("");
		orderContent.addCell("");
		orderContent.addCell("Total");
		orderContent.addCell(String.format(Locale.getDefault(), "%.2f€", order.getTotalPrice()));

		//Adds every section to the final document
		document.add(header);
		document.add(orderInfo);
		document.add(orderContentHeader);
		document.add(orderContent);
		//Creates the physical document
		document.close();
	}
}