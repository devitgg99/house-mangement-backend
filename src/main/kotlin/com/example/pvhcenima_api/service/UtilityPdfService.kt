package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.PdfLanguage
import com.example.pvhcenima_api.model.PdfTranslations
import com.example.pvhcenima_api.repository.HouseRepository
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class UtilityPdfService(
    private val utilityService: UtilityService,
    private val houseRepository: HouseRepository
) {
    private val khmerFontPath = "fonts/NotoSansKhmer-Regular.ttf"

    fun generateUtilityReport(houseId: UUID, month: LocalDate?, lang: PdfLanguage = PdfLanguage.EN): ByteArray {
        val house = houseRepository.findById(houseId)
            .orElseThrow { IllegalArgumentException("House not found") }

        val utilities = utilityService.getUtilitiesByHouse(houseId, month)
        val t = { key: String -> PdfTranslations.get(key, lang) }

        // Calculate totals
        var grandTotal = BigDecimal.ZERO
        var paidTotal = BigDecimal.ZERO
        var unpaidTotal = BigDecimal.ZERO
        var paidCount = 0
        var unpaidCount = 0

        utilities.forEach { utility ->
            grandTotal = grandTotal.add(utility.totalCost)
            if (utility.paid) {
                paidTotal = paidTotal.add(utility.totalCost)
                paidCount++
            } else {
                unpaidTotal = unpaidTotal.add(utility.totalCost)
                unpaidCount++
            }
        }

        val outputStream = ByteArrayOutputStream()
        PDDocument().use { document ->
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)

            // Load font
            val font = if (lang == PdfLanguage.KH) {
                loadKhmerFont(document) ?: PDType0Font.load(document, 
                    ClassPathResource("fonts/NotoSansKhmer-Regular.ttf").inputStream)
            } else {
                PDType0Font.load(document, 
                    ClassPathResource("fonts/NotoSansKhmer-Regular.ttf").inputStream)
            }

            PDPageContentStream(document, page).use { content ->
                val pageWidth = page.mediaBox.width
                val pageHeight = page.mediaBox.height
                var y = pageHeight - 50f

                // Title
                content.setFont(font, 18f)
                content.setNonStrokingColor(Color(66, 133, 244))
                val titleWidth = font.getStringWidth(t("title")) / 1000 * 18
                content.beginText()
                content.newLineAtOffset((pageWidth - titleWidth) / 2, y)
                content.showText(t("title"))
                content.endText()
                y -= 40f

                // House info
                content.setNonStrokingColor(Color.BLACK)
                content.setFont(font, 14f)
                content.beginText()
                content.newLineAtOffset(50f, y)
                content.showText("${t("house")}: ${house.houseName}")
                content.endText()
                y -= 20f

                content.setFont(font, 11f)
                content.beginText()
                content.newLineAtOffset(50f, y)
                content.showText("${t("address")}: ${house.houseAddress}")
                content.endText()
                y -= 18f

                val monthText = if (month != null) {
                    "${t("month")}: ${month.format(DateTimeFormatter.ofPattern("MM/yyyy"))}"
                } else {
                    t("all_records")
                }
                content.beginText()
                content.newLineAtOffset(50f, y)
                content.showText(monthText)
                content.endText()
                y -= 18f

                content.beginText()
                content.newLineAtOffset(50f, y)
                content.showText("${t("generated")}: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                content.endText()
                y -= 30f

                // Table header
                val colWidths = floatArrayOf(90f, 60f, 55f, 55f, 70f, 70f, 70f)
                val headers = listOf(t("room"), t("month"), t("old_water"), t("new_water"), t("room_cost"), t("water_cost"), t("total"))
                
                // Draw header background
                content.setNonStrokingColor(Color(66, 133, 244))
                content.addRect(50f, y - 5f, colWidths.sum(), 22f)
                content.fill()

                // Draw header text
                content.setNonStrokingColor(Color.WHITE)
                content.setFont(font, 9f)
                var x = 55f
                headers.forEachIndexed { index, header ->
                    content.beginText()
                    content.newLineAtOffset(x, y + 3f)
                    content.showText(header)
                    content.endText()
                    x += colWidths[index]
                }
                y -= 25f

                // Draw data rows
                content.setFont(font, 9f)
                if (utilities.isEmpty()) {
                    content.setNonStrokingColor(Color.GRAY)
                    content.beginText()
                    content.newLineAtOffset(50f, y)
                    content.showText(t("no_records"))
                    content.endText()
                    y -= 20f
                } else {
                    utilities.sortedBy { it.month }.forEach { utility ->
                        val bgColor = if (utility.paid) Color(240, 255, 240) else Color(255, 240, 240)
                        val textColor = if (utility.paid) Color(34, 139, 34) else Color(220, 20, 60)

                        // Row background
                        content.setNonStrokingColor(bgColor)
                        content.addRect(50f, y - 5f, colWidths.sum(), 18f)
                        content.fill()

                        // Row data
                        content.setNonStrokingColor(Color.BLACK)
                        x = 55f
                        val rowData = listOf(
                            utility.roomName,
                            utility.month.format(DateTimeFormatter.ofPattern("MM/yyyy")),
                            String.format("%.1f", utility.oldWater),
                            String.format("%.1f", utility.newWater),
                            formatCurrency(utility.roomCost),
                            formatCurrency(utility.waterCost),
                            formatCurrency(utility.totalCost)
                        )

                        rowData.forEachIndexed { index, text ->
                            if (index == rowData.lastIndex) {
                                content.setNonStrokingColor(textColor)
                            }
                            content.beginText()
                            content.newLineAtOffset(x, y)
                            content.showText(text)
                            content.endText()
                            x += colWidths[index]
                        }
                        content.setNonStrokingColor(Color.BLACK)
                        y -= 18f
                    }
                }
                y -= 15f

                // Summary
                content.setFont(font, 14f)
                content.beginText()
                content.newLineAtOffset(50f, y)
                content.showText(t("summary"))
                content.endText()
                y -= 20f

                content.setFont(font, 11f)
                val summaryLines = listOf(
                    "${t("total_records")}: ${utilities.size}",
                    "${t("paid")}: $paidCount (${formatCurrency(paidTotal)})",
                    "${t("unpaid")}: $unpaidCount (${formatCurrency(unpaidTotal)})"
                )
                summaryLines.forEach { line ->
                    content.beginText()
                    content.newLineAtOffset(50f, y)
                    content.showText(line)
                    content.endText()
                    y -= 18f
                }

                // Grand total
                content.setFont(font, 12f)
                content.beginText()
                content.newLineAtOffset(50f, y)
                content.showText("${t("grand_total")}: ${formatCurrency(grandTotal)}")
                content.endText()
            }

            document.save(outputStream)
        }

        return outputStream.toByteArray()
    }

    private fun loadKhmerFont(document: PDDocument): PDType0Font? {
        return try {
            val fontResource = ClassPathResource(khmerFontPath)
            if (fontResource.exists()) {
                PDType0Font.load(document, fontResource.inputStream)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun formatCurrency(amount: BigDecimal): String {
        return if (amount.stripTrailingZeros().scale() > 0) {
            String.format("%,.2f", amount)
        } else {
            String.format("%,.0f", amount)
        }
    }
}
