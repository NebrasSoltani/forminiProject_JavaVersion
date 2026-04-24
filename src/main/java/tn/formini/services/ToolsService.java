package tn.formini.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import tn.formini.entities.evenements.Evenement;
import tn.formini.entities.evenements.Blog;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ToolsService {

    /**
     * Génère un QR Code Premium avec couleur personnalisée.
     */
    public Image generateAdvancedQRCode(String content, int size) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        int onColor = new Color(105, 116, 232).getRGB(); // Indigo Formini
        int offColor = Color.WHITE.getRGB();

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? onColor : offColor);
            }
        }
        return SwingFXUtils.toFXImage(image, null);
    }

    /**
     * Génère le rapport PDF Premium pour un événement.
     */
    public void generateEvenementPDF(Evenement evt, String dest) throws IOException {
        generateBasePDF(dest, "Rapport d'Événement", evt.getTitre(), 
            new String[][]{
                {"Lieu", evt.getLieu()},
                {"Date début", evt.getDate_debut() != null ? evt.getDate_debut().toString() : "N/A"},
                {"Type", evt.getType()},
                {"Places", String.valueOf(evt.getNombre_places())},
                {"Description", evt.getDescription()}
            },
            "ID Événement: " + evt.getId()
        );
    }

    /**
     * Génère le rapport PDF Premium pour un blog.
     */
    public void generateBlogPDF(Blog blog, String dest) throws IOException {
        generateBasePDF(dest, "Détails du Blog", blog.getTitre(),
            new String[][]{
                {"Catégorie", blog.getCategorie()},
                {"Date publication", blog.getDate_publication() != null ? blog.getDate_publication().toString() : "N/A"},
                {"Auteur ID", String.valueOf(blog.getAuteur_id())},
                {"Résumé", blog.getResume()},
                {"Contenu", blog.getContenu()}
            },
            "ID Blog: " + blog.getId()
        );
    }

    private void generateBasePDF(String dest, String reportType, String title, String[][] data, String qrContent) throws IOException {
        PdfWriter writer = new PdfWriter(dest);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Header Styling
        com.itextpdf.kernel.colors.DeviceRgb mainColor = new com.itextpdf.kernel.colors.DeviceRgb(105, 116, 232); // Indigo Formini

        // Mini Header
        document.add(new Paragraph(reportType.toUpperCase())
                .setFontSize(8)
                .setCharacterSpacing(2f)
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.RIGHT));

        // Title with decorative line
        document.add(new Paragraph(title)
                .setFontSize(26)
                .setBold()
                .setFontColor(mainColor)
                .setMarginTop(10)
                .setMarginBottom(0));
        
        // Decorative line
        com.itextpdf.layout.element.LineSeparator ls = new com.itextpdf.layout.element.LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(2f));
        ls.setFontColor(mainColor);
        document.add(ls);
        document.add(new Paragraph("\n"));

        // Table with styling
        Table table = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setMarginTop(20);

        for (String[] row : data) {
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(row[0]).setBold())
                    .setBackgroundColor(new com.itextpdf.kernel.colors.DeviceRgb(248, 250, 252))
                    .setPadding(10)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(row[1] != null ? row[1] : "N/A"))
                    .setPadding(10)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        }
        document.add(table);

        // Footer
        document.add(new Paragraph("\n\n\n"));
        document.add(new Paragraph("FORMINI ADMINISTRATIVE TOOLS")
                .setFontSize(9)
                .setBold()
                .setFontColor(mainColor)
                .setTextAlignment(TextAlignment.CENTER));
        
        document.add(new Paragraph("Ce document est officiel et généré numériquement le " + new java.util.Date())
                .setFontSize(8)
                .setItalic()
                .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER));
        
        document.close();
    }
}
