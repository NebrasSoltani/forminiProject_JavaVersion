package tn.formini.services;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import tn.formini.services.cart.CartItem;
import tn.formini.entities.produits.Produit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service pour générer des factures PDF en utilisant iText
 */
public class PDFInvoiceService {
    private static PDFInvoiceService instance;
    
    private PDFInvoiceService() {}
    
    public static PDFInvoiceService getInstance() {
        if (instance == null) {
            instance = new PDFInvoiceService();
        }
        return instance;
    }
    
    /**
     * Génère une facture PDF pour les articles du panier
     */
    public void generateInvoicePDF(List<CartItem> cartItems, BigDecimal totalAmount, 
                              String customerName, String customerEmail, 
                              String customerPhone, String customerAddress, 
                              ByteArrayOutputStream outputStream) throws IOException, com.itextpdf.text.DocumentException {
        
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, outputStream);
        document.open();
        
        try {
            // Ajouter l'en-tête
            addHeader(document, customerName, customerEmail, customerPhone, customerAddress);
            
            // Ajouter les détails des produits
            addProductsTable(document, cartItems);
            
            // Ajouter le total
            addTotalSection(document, totalAmount);
            
            // Ajouter le pied de page
            addFooter(document);
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération du PDF: " + e.getMessage());
            throw new IOException("Erreur lors de la génération du PDF: " + e.getMessage());
        } finally {
            document.close();
        }
    }
    
    /**
     * Ajoute l'en-tête de la facture
     */
    private void addHeader(Document document, String customerName, String customerEmail, 
                          String customerPhone, String customerAddress) throws IOException, com.itextpdf.text.DocumentException {
        
        // Titre de l'entreprise
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph companyTitle = new Paragraph("FORMINI.TN - L'EXCELLENCE EN FORMATION", titleFont);
        companyTitle.setAlignment(Element.ALIGN_CENTER);
        companyTitle.setSpacingAfter(10);
        document.add(companyTitle);
        
        // Titre de la facture
        Font invoiceFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
        Paragraph invoiceTitle = new Paragraph("FACTURE", invoiceFont);
        invoiceTitle.setAlignment(Element.ALIGN_CENTER);
        invoiceTitle.setSpacingAfter(20);
        document.add(invoiceTitle);
        
        // Informations de la facture
        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String invoiceNumber = "FACT-" + System.currentTimeMillis();
        
        Paragraph info = new Paragraph();
        info.add(new Paragraph("Numéro: " + invoiceNumber, infoFont));
        info.add(new Paragraph("Date: " + timestamp, infoFont));
        info.setSpacingAfter(15);
        document.add(info);
        
        // Informations client
        Font clientFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
        Paragraph clientTitle = new Paragraph("INFORMATIONS CLIENT", clientFont);
        clientTitle.setSpacingAfter(10);
        document.add(clientTitle);
        
        Paragraph clientInfo = new Paragraph();
        clientInfo.add(new Paragraph("Nom: " + (customerName != null ? customerName : "N/A"), infoFont));
        clientInfo.add(new Paragraph("Email: " + (customerEmail != null ? customerEmail : "N/A"), infoFont));
        clientInfo.add(new Paragraph("Téléphone: " + (customerPhone != null ? customerPhone : "N/A"), infoFont));
        clientInfo.add(new Paragraph("Adresse: " + (customerAddress != null ? customerAddress : "N/A"), infoFont));
        clientInfo.setSpacingAfter(20);
        document.add(clientInfo);
        
        // Ligne de séparation
        LineSeparator separator = new LineSeparator();
        document.add(separator);
    }
    
    /**
     * Ajoute le tableau des produits
     */
    private void addProductsTable(Document document, List<CartItem> cartItems) throws IOException, com.itextpdf.text.DocumentException {
        Font tableHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font tableFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        // Créer le tableau
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(20);
        table.setSpacingAfter(20);
        
        // En-têtes du tableau
        String[] headers = {"Produit", "Quantité", "Prix Unitaire", "Total"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, tableHeaderFont));
            cell.setBackgroundColor(com.itextpdf.text.BaseColor.LIGHT_GRAY);
            cell.setPadding(8);
            table.addCell(cell);
        }
        
        // Données des produits
        DecimalFormat df = new DecimalFormat("#,##0.000 DT");
        for (CartItem item : cartItems) {
            Produit product = item.getProduit();
            
            // Nom du produit
            PdfPCell nameCell = new PdfPCell(new Paragraph(
                product.getNom() != null ? product.getNom() : "Produit", tableFont));
            nameCell.setPadding(8);
            nameCell.setNoWrap(false);
            table.addCell(nameCell);
            
            // Quantité
            PdfPCell qtyCell = new PdfPCell(new Paragraph(
                String.valueOf(item.getQuantity()), tableFont));
            qtyCell.setPadding(8);
            qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(qtyCell);
            
            // Prix unitaire
            PdfPCell priceCell = new PdfPCell(new Paragraph(
                df.format(product.getPrix()), tableFont));
            priceCell.setPadding(8);
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(priceCell);
            
            // Total ligne
            PdfPCell totalCell = new PdfPCell(new Paragraph(
                df.format(item.getLineTotal()), tableFont));
            totalCell.setPadding(8);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(totalCell);
        }
        
        document.add(table);
    }
    
    /**
     * Ajoute la section du total
     */
    private void addTotalSection(Document document, BigDecimal totalAmount) throws IOException, com.itextpdf.text.DocumentException {
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        DecimalFormat df = new DecimalFormat("#,##0.000 DT");
        
        Paragraph totalParagraph = new Paragraph();
        totalParagraph.setAlignment(Element.ALIGN_RIGHT);
        totalParagraph.add(new Paragraph("MONTANT TOTAL: " + df.format(totalAmount), totalFont));
        totalParagraph.setSpacingBefore(30);
        document.add(totalParagraph);
    }
    
    /**
     * Ajoute le pied de page
     */
    private void addFooter(Document document) throws IOException, com.itextpdf.text.DocumentException {
        Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        
        Paragraph footer = new Paragraph();
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(40);
        
        footer.add(new Paragraph("Merci de votre confiance !", footerFont));
        footer.add(new Paragraph("Formini.tn - L'EXCELLENCE EN FORMATION", footerFont));
        footer.add(new Paragraph("Contact: contact@formini.tn | www.formini.tn", footerFont));
        
        document.add(footer);
    }
}
