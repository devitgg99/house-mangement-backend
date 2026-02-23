package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.PdfLanguage
import com.example.pvhcenima_api.model.PdfTranslations
import com.example.pvhcenima_api.repository.HouseRepository
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
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
    private var cachedFont: Font? = null

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

        // Create image using Java2D (supports complex scripts like Khmer)
        val imgWidth = 595 * 2  // A4 width at 144 DPI
        val imgHeight = 842 * 2 // A4 height at 144 DPI
        val image = BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB)
        val g2d = image.createGraphics()

        // Enable anti-aliasing for better text rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON)

        // White background
        g2d.color = Color.WHITE
        g2d.fillRect(0, 0, imgWidth, imgHeight)

        // Load font
        val baseFont = getFont(lang)
        val scale = 2f // For high DPI
        var y = 80f * scale

        // Title
        g2d.font = baseFont.deriveFont(Font.BOLD, 24f * scale)
        g2d.color = Color(66, 133, 244)
        val title = t("title")
        val titleWidth = g2d.fontMetrics.stringWidth(title)
        g2d.drawString(title, (imgWidth - titleWidth) / 2, y.toInt())
        y += 60f * scale

        // House info
        g2d.color = Color.BLACK
        g2d.font = baseFont.deriveFont(Font.BOLD, 16f * scale)
        g2d.drawString("${t("house")}: ${house.houseName}", (50 * scale).toInt(), y.toInt())
        y += 30f * scale

        g2d.font = baseFont.deriveFont(Font.PLAIN, 13f * scale)
        g2d.drawString("${t("address")}: ${house.houseAddress}", (50 * scale).toInt(), y.toInt())
        y += 25f * scale

        val monthText = if (month != null) {
            "${t("month")}: ${month.format(DateTimeFormatter.ofPattern("MM/yyyy"))}"
        } else {
            t("all_records")
        }
        g2d.drawString(monthText, (50 * scale).toInt(), y.toInt())
        y += 25f * scale

        g2d.drawString("${t("generated")}: ${LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", (50 * scale).toInt(), y.toInt())
        y += 40f * scale

        // Table
        val colWidths = floatArrayOf(85f, 70f, 60f, 55f, 55f, 70f, 70f, 70f).map { it * scale }
        val headers = listOf(t("room"), t("floor"), t("month"), t("old_water"), t("new_water"), t("room_cost"), t("water_cost"), t("total"))
        val tableX = 50f * scale
        val rowHeight = 28f * scale

        // Header background
        g2d.color = Color(66, 133, 244)
        g2d.fillRect(tableX.toInt(), (y - rowHeight + 8 * scale).toInt(), colWidths.sum().toInt(), rowHeight.toInt())

        // Header text
        g2d.color = Color.WHITE
        g2d.font = baseFont.deriveFont(Font.BOLD, 11f * scale)
        var x = tableX + 5 * scale
        headers.forEachIndexed { index, header ->
            g2d.drawString(header, x.toInt(), y.toInt())
            x += colWidths[index]
        }
        y += rowHeight

        // Data rows
        g2d.font = baseFont.deriveFont(Font.PLAIN, 11f * scale)
        if (utilities.isEmpty()) {
            g2d.color = Color.GRAY
            g2d.drawString(t("no_records"), (tableX + 5 * scale).toInt(), y.toInt())
            y += rowHeight
        } else {
            utilities.sortedWith(compareBy({ it.floorNumber }, { it.roomName }, { it.month })).forEach { utility ->
                val bgColor = if (utility.paid) Color(240, 255, 240) else Color(255, 240, 240)
                val totalColor = if (utility.paid) Color(34, 139, 34) else Color(220, 20, 60)

                // Row background
                g2d.color = bgColor
                g2d.fillRect(tableX.toInt(), (y - rowHeight + 8 * scale).toInt(), colWidths.sum().toInt(), rowHeight.toInt())

                // Row data
                val rowData = listOf(
                    utility.roomName,
                    utility.floorName,
                    utility.month.format(DateTimeFormatter.ofPattern("MM/yyyy")),
                    String.format("%.1f", utility.oldWater),
                    String.format("%.1f", utility.newWater),
                    formatCurrency(utility.roomCost),
                    formatCurrency(utility.waterCost),
                    formatCurrency(utility.totalCost)
                )

                x = tableX + 5 * scale
                rowData.forEachIndexed { index, text ->
                    g2d.color = if (index == rowData.lastIndex) totalColor else Color.BLACK
                    g2d.drawString(text, x.toInt(), y.toInt())
                    x += colWidths[index]
                }
                y += rowHeight
            }
        }
        y += 20f * scale

        // Summary
        g2d.color = Color.BLACK
        g2d.font = baseFont.deriveFont(Font.BOLD, 16f * scale)
        g2d.drawString(t("summary"), (50 * scale).toInt(), y.toInt())
        y += 30f * scale

        g2d.font = baseFont.deriveFont(Font.PLAIN, 13f * scale)
        listOf(
            "${t("total_records")}: ${utilities.size}",
            "${t("paid")}: $paidCount (${formatCurrency(paidTotal)})",
            "${t("unpaid")}: $unpaidCount (${formatCurrency(unpaidTotal)})"
        ).forEach { line ->
            g2d.drawString(line, (50 * scale).toInt(), y.toInt())
            y += 25f * scale
        }

        // Grand total
        g2d.font = baseFont.deriveFont(Font.BOLD, 14f * scale)
        g2d.drawString("${t("grand_total")}: ${formatCurrency(grandTotal)}", (50 * scale).toInt(), y.toInt())

        g2d.dispose()

        // Convert image to PDF
        val outputStream = ByteArrayOutputStream()
        PDDocument().use { document ->
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)

            val pdImage = LosslessFactory.createFromImage(document, image)
            PDPageContentStream(document, page).use { content ->
                content.drawImage(pdImage, 0f, 0f, PDRectangle.A4.width, PDRectangle.A4.height)
            }

            document.save(outputStream)
        }

        return outputStream.toByteArray()
    }

    private fun getFont(lang: PdfLanguage): Font {
        // For Khmer, we MUST use the Khmer font
        // For English, we can use the Khmer font too (it has Latin characters)
        cachedFont?.let { return it }

        val font = try {
            val fontResource = ClassPathResource(khmerFontPath)
            if (fontResource.exists()) {
                Font.createFont(Font.TRUETYPE_FONT, fontResource.inputStream)
            } else {
                Font("SansSerif", Font.PLAIN, 12)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Font("SansSerif", Font.PLAIN, 12)
        }

        cachedFont = font
        return font
    }

    private fun formatCurrency(amount: BigDecimal): String {
        return if (amount.stripTrailingZeros().scale() > 0) {
            "$${String.format("%,.2f", amount)}"
        } else {
            "$${String.format("%,.0f", amount)}"
        }
    }
}
