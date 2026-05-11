package com.doctorat.suividoctorat.service;

import com.doctorat.suividoctorat.entity.PhDRegistration;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PDFService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generateRegistrationCertificate(PhDRegistration registration) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            Paragraph title = new Paragraph("ATTESTATION D'INSCRIPTION EN DOCTORAT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            Paragraph universityInfo = new Paragraph("Universite Mohammed V - Rabat\nFaculte des Sciences\nEcole Doctorale", normalFont);
            universityInfo.setAlignment(Element.ALIGN_CENTER);
            universityInfo.setSpacingAfter(20);
            document.add(universityInfo);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(20);
            table.setSpacingAfter(20);

            addTableCell(table, "Numero d'inscription:", registration.getId().toString(), headerFont, normalFont);
            addTableCell(table, "Doctorant:", registration.getDoctorant().getFullName(), headerFont, normalFont);
            addTableCell(table, "Email:", registration.getDoctorant().getEmail(), headerFont, normalFont);
            addTableCell(table, "Sujet de these:", registration.getThesisSubject(), headerFont, normalFont);
            addTableCell(table, "Domaine de recherche:", registration.getResearchDomain(), headerFont, normalFont);
            addTableCell(table, "Directeur de these:", registration.getDirectorName(), headerFont, normalFont);

            if (registration.getCoDirectorName() != null && !registration.getCoDirectorName().isEmpty()) {
                addTableCell(table, "Co-directeur:", registration.getCoDirectorName(), headerFont, normalFont);
            }

            addTableCell(table, "Date d'inscription:", registration.getSubmissionDate().format(DATE_FORMATTER), headerFont, normalFont);

            document.add(table);

            Paragraph footer = new Paragraph("Le present document certifie que l'etudiant est regulierement inscrit au programme de doctorat.", normalFont);
            footer.setSpacingBefore(30);
            document.add(footer);

            Paragraph signature = new Paragraph("Fait a Rabat, le " + java.time.LocalDate.now().format(DATE_FORMATTER) + "\n\nLe Chef d'Etablissement", normalFont);
            signature.setAlignment(Element.ALIGN_RIGHT);
            signature.setSpacingBefore(30);
            document.add(signature);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public byte[] generateDefenseAuthorization(PhDRegistration registration) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

            Paragraph title = new Paragraph("AUTORISATION DE SOUTENANCE DE THESE", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(30);
            document.add(title);

            Paragraph reference = new Paragraph("Ref: DR/2026/" + registration.getId(), normalFont);
            reference.setAlignment(Element.ALIGN_RIGHT);
            reference.setSpacingAfter(20);
            document.add(reference);

            Paragraph content = new Paragraph("Nous, soussigne, Chef de l'Etablissement, certifions que :", normalFont);
            content.setSpacingAfter(15);
            document.add(content);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(15);
            table.setSpacingAfter(15);

            addTableCell(table, "Nom complet:", registration.getDoctorant().getFullName(), headerFont, normalFont);
            addTableCell(table, "Sujet de these:", registration.getThesisSubject(), headerFont, normalFont);
            addTableCell(table, "Directeur de these:", registration.getDirectorName(), headerFont, normalFont);

            if (registration.getCoDirectorName() != null && !registration.getCoDirectorName().isEmpty()) {
                addTableCell(table, "Co-directeur:", registration.getCoDirectorName(), headerFont, normalFont);
            }

            document.add(table);

            Paragraph approvalText = new Paragraph("EST AUTORISE A SOUTENIR SA THESE DE DOCTORAT", headerFont);
            approvalText.setAlignment(Element.ALIGN_CENTER);
            approvalText.setSpacingBefore(20);
            approvalText.setSpacingAfter(20);
            document.add(approvalText);

            Paragraph conditions = new Paragraph("Conditions de soutenance:\n- Le manuscrit doit etre depose au moins 15 jours avant la soutenance\n- Le jury doit etre compose conformement a la reglementation\n- Les rapports de these doivent etre favorables", normalFont);
            conditions.setSpacingBefore(15);
            conditions.setSpacingAfter(25);
            document.add(conditions);

            Paragraph signature = new Paragraph("Fait a Rabat, le " + java.time.LocalDate.now().format(DATE_FORMATTER) + "\n\n\nLe Chef d'Etablissement", normalFont);
            signature.setAlignment(Element.ALIGN_RIGHT);
            document.add(signature);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addTableCell(PdfPTable table, String label, String value, Font headerFont, Font normalFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, headerFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}