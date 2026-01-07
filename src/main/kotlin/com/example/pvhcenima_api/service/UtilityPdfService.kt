package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.response.UtilityResponse
import com.example.pvhcenima_api.repository.HouseRepository
import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
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

    fun generateUtilityReport(houseId: UUID, month: LocalDate?): ByteArray {
        val house = houseRepository.findById(houseId)
            .orElseThrow { IllegalArgumentException("House not found") }
        
        val utilities = utilityService.getUtilitiesByHouse(houseId, month)
        
        val outputStream = ByteArrayOutputStream()
        val document = Document(PageSize.A4)
        PdfWriter.getInstance(document, outputStream)
        
        document.open()
        
        // Title
        val titleFont = Font(Font.HELVETICA, 18f, Font.BOLD)
        val title = Paragraph("Utility Report", titleFont)
        title.alignment = Element.ALIGN_CENTER
        document.add(title)
        
        // House info
        val subTitleFont = Font(Font.HELVETICA, 14f, Font.BOLD)
        val normalFont = Font(Font.HELVETICA, 11f, Font.NORMAL)
        
        document.add(Paragraph("\n"))
        document.add(Paragraph("House: ${house.houseName}", subTitleFont))
        document.add(Paragraph("Address: ${house.houseAddress}", normalFont))
        
        if (month != null) {
            document.add(Paragraph("Month: ${month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}", normalFont))
        } else {
            document.add(Paragraph("All Records", normalFont))
        }
        
        document.add(Paragraph("Generated: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", normalFont))
        document.add(Paragraph("\n"))
        
        if (utilities.isEmpty()) {
            document.add(Paragraph("No utility records found.", normalFont))
        } else {
            // Create table
            val table = PdfPTable(7)
            table.widthPercentage = 100f
            table.setWidths(floatArrayOf(2f, 1.5f, 1.2f, 1.2f, 1.5f, 1.5f, 1.5f))
            
            // Header
            val headerFont = Font(Font.HELVETICA, 10f, Font.BOLD, Color.WHITE)
            val headerBgColor = Color(66, 133, 244)
            
            addHeaderCell(table, "Room", headerFont, headerBgColor)
            addHeaderCell(table, "Month", headerFont, headerBgColor)
            addHeaderCell(table, "Old Water", headerFont, headerBgColor)
            addHeaderCell(table, "New Water", headerFont, headerBgColor)
            addHeaderCell(table, "Room Cost", headerFont, headerBgColor)
            addHeaderCell(table, "Water Cost", headerFont, headerBgColor)
            addHeaderCell(table, "Total", headerFont, headerBgColor)
            
            // Data rows
            val cellFont = Font(Font.HELVETICA, 9f, Font.NORMAL)
            val paidFont = Font(Font.HELVETICA, 9f, Font.NORMAL, Color(34, 139, 34))
            val unpaidFont = Font(Font.HELVETICA, 9f, Font.BOLD, Color(220, 20, 60))
            
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
            document.add(Paragraph("Summary", subTitleFont))
            document.add(Paragraph("Total Records: ${utilities.size}", normalFont))
            document.add(Paragraph("Paid: ${utilities.count { it.paid }} (${formatCurrency(paidTotal)})", normalFont))
            document.add(Paragraph("Unpaid: ${utilities.count { !it.paid }} (${formatCurrency(unpaidTotal)})", normalFont))
            
            val totalFont = Font(Font.HELVETICA, 12f, Font.BOLD)
            document.add(Paragraph("Grand Total: ${formatCurrency(grandTotal)}", totalFont))
        }
        
        document.close()
        return outputStream.toByteArray()
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
        return String.format("%,.0f", amount)
    }
}

