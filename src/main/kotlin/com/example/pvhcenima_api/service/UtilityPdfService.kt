package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.PdfLanguage
import com.example.pvhcenima_api.model.PdfTranslations
import com.example.pvhcenima_api.model.response.UtilityResponse
import com.example.pvhcenima_api.repository.HouseRepository
import com.lowagie.text.*
import com.lowagie.text.pdf.BaseFont
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class UtilityPdfService(
    private val utilityService: UtilityService,
    private val houseRepository: HouseRepository
) {
    // Khmer font path in resources
    private val khmerFontPath = "fonts/NotoSansKhmer-Regular.ttf"

    fun generateUtilityReport(houseId: UUID, month: LocalDate?, lang: PdfLanguage = PdfLanguage.EN): ByteArray {
        val house = houseRepository.findById(houseId)
            .orElseThrow { IllegalArgumentException("House not found") }
        
        val utilities = utilityService.getUtilitiesByHouse(houseId, month)
        val t = { key: String -> PdfTranslations.get(key, lang) }
        
        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A4)
        PdfWriter.getInstance(document, outputStream)
        
        document.open()
        
        // Load fonts based on language
        val baseFont = loadFont(lang)
        val titleFont = Font(baseFont, 18f, Font.BOLD)
        val subTitleFont = Font(baseFont, 14f, Font.BOLD)
        val normalFont = Font(baseFont, 11f, Font.NORMAL)
        
        // Title
        val title = Paragraph(t("title"), titleFont)
        title.alignment = Element.ALIGN_CENTER
        document.add(title)
        
        // House info
        document.add(Paragraph("\n"))
        document.add(Paragraph("${t("house")}: ${house.houseName}", subTitleFont))
        document.add(Paragraph("${t("address")}: ${house.houseAddress}", normalFont))
        
        if (month != null) {
            document.add(Paragraph("${t("month")}: ${month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}", normalFont))
        } else {
            document.add(Paragraph(t("all_records"), normalFont))
        }
        
        document.add(Paragraph("${t("generated")}: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", normalFont))
        document.add(Paragraph("\n"))
        
        if (utilities.isEmpty()) {
            document.add(Paragraph(t("no_records"), normalFont))
        } else {
            // Create table
            val table = PdfPTable(7)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(2f, 1.5f, 1.2f, 1.2f, 1.5f, 1.5f, 1.5f))
            
            // Header
            val headerFont = Font(baseFont, 10f, Font.BOLD, Color.WHITE)
            val headerBgColor = Color(66, 133, 244)
            
            addHeaderCell(table, t("room"), headerFont, headerBgColor)
            addHeaderCell(table, t("month"), headerFont, headerBgColor)
            addHeaderCell(table, t("old_water"), headerFont, headerBgColor)
            addHeaderCell(table, t("new_water"), headerFont, headerBgColor)
            addHeaderCell(table, t("room_cost"), headerFont, headerBgColor)
            addHeaderCell(table, t("water_cost"), headerFont, headerBgColor)
            addHeaderCell(table, t("total"), headerFont, headerBgColor)
            
            // Data rows
            val cellFont = Font(baseFont, 9f, Font.NORMAL)
            val paidFont = Font(baseFont, 9f, Font.NORMAL, Color(34, 139, 34))
            val unpaidFont = Font(baseFont, 9f, Font.BOLD, Color(220, 20, 60))
            
            var grandTotal = java.math.BigDecimal.ZERO
            var paidTotal = java.math.BigDecimal.ZERO
            var unpaidTotal = java.math.BigDecimal.ZERO
            
            utilities.sortedBy { it.month }.forEach { utility ->
                val rowFont = if (utility.paid) paidFont else unpaidFont
                val bgColor = if (utility.paid) Color(240, 255, 240) else Color(255, 240, 240)
                
                addDataCell(table, utility.roomName, cellFont, bgColor)
                addDataCell(table, utility.month.format(DateTimeFormatter.ofPattern("MM/yyyy")), cellFont, bgColor)
                addDataCell(table, String.format("%.1f", utility.oldWater), cellFont, bgColor)
                addDataCell(table, String.format("%.1f", utility.newWater), cellFont, bgColor)
                addDataCell(table, formatCurrency(utility.roomCost), cellFont, bgColor)
                addDataCell(table, formatCurrency(utility.waterCost), cellFont, bgColor)
                addDataCell(table, formatCurrency(utility.totalCost), rowFont, bgColor)
                
                grandTotal = grandTotal.add(utility.totalCost)
                if (utility.paid) {
                    paidTotal = paidTotal.add(utility.totalCost)
                } else {
                    unpaidTotal = unpaidTotal.add(utility.totalCost)
                }
            }
            
            document.add(table)
            
            // Summary
            document.add(Paragraph("\n"))
            document.add(Paragraph(t("summary"), subTitleFont))
            document.add(Paragraph("${t("total_records")}: ${utilities.size}", normalFont))
            document.add(Paragraph("${t("paid")}: ${utilities.count { it.paid }} (${formatCurrency(paidTotal)})", normalFont))
            document.add(Paragraph("${t("unpaid")}: ${utilities.count { !it.paid }} (${formatCurrency(unpaidTotal)})", normalFont))
            
            val totalFont = Font(baseFont, 12f, Font.BOLD)
            document.add(Paragraph("${t("grand_total")}: ${formatCurrency(grandTotal)}", totalFont))
        }
        
        document.close()
        return outputStream.toByteArray()
    }
    
    private fun loadFont(lang: PdfLanguage): BaseFont {
        return if (lang == PdfLanguage.KH) {
            try {
                val fontResource = ClassPathResource(khmerFontPath)
                if (fontResource.exists()) {
                    BaseFont.createFont(
                        fontResource.uri.toString(),
                        BaseFont.IDENTITY_H,
                        BaseFont.EMBEDDED
                    )
                } else {
                    // Fallback to default if Khmer font not found
                    BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
                }
            } catch (e: Exception) {
                BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
            }
        } else {
            BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED)
        }
    }
    
    private fun addHeaderCell(table: PdfPTable, text: String, font: Font, bgColor: Color) {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = bgColor
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPadding(5f)
        table.addCell(cell)
    }
    
    private fun addDataCell(table: PdfPTable, text: String, font: Font, bgColor: Color) {
        val cell = PdfPCell(Phrase(text, font))
        cell.backgroundColor = bgColor
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.setPadding(4f)
        table.addCell(cell)
    }
    
    private fun formatCurrency(amount: java.math.BigDecimal): String {
        return if (amount.stripTrailingZeros().scale() > 0) {
            String.format("%,.2f", amount)
        } else {
            String.format("%,.0f", amount)
        }
    }
}
