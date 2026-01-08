package com.example.pvhcenima_api.model

enum class PdfLanguage(val code: String) {
    EN("en"),
    KH("kh");

    companion object {
        fun fromCode(code: String?): PdfLanguage =
            entries.find { it.code.equals(code, ignoreCase = true) } ?: EN
    }
}

// Translation keys for PDF
object PdfTranslations {
    
    private val translations = mapOf(
        "title" to mapOf(
            PdfLanguage.EN to "Utility Report",
            PdfLanguage.KH to "របាយការណ៍ប្រើប្រាស់"
        ),
        "house" to mapOf(
            PdfLanguage.EN to "House",
            PdfLanguage.KH to "ផ្ទះ"
        ),
        "address" to mapOf(
            PdfLanguage.EN to "Address",
            PdfLanguage.KH to "អាសយដ្ឋាន"
        ),
        "month" to mapOf(
            PdfLanguage.EN to "Month",
            PdfLanguage.KH to "ខែ"
        ),
        "all_records" to mapOf(
            PdfLanguage.EN to "All Records",
            PdfLanguage.KH to "កំណត់ត្រាទាំងអស់"
        ),
        "generated" to mapOf(
            PdfLanguage.EN to "Generated",
            PdfLanguage.KH to "បង្កើតនៅថ្ងៃ"
        ),
        "no_records" to mapOf(
            PdfLanguage.EN to "No utility records found.",
            PdfLanguage.KH to "រកមិនឃើញកំណត់ត្រាប្រើប្រាស់។"
        ),
        "room" to mapOf(
            PdfLanguage.EN to "Room",
            PdfLanguage.KH to "បន្ទប់"
        ),
        "old_water" to mapOf(
            PdfLanguage.EN to "Old Water",
            PdfLanguage.KH to "ទឹកចាស់"
        ),
        "new_water" to mapOf(
            PdfLanguage.EN to "New Water",
            PdfLanguage.KH to "ទឹកថ្មី"
        ),
        "room_cost" to mapOf(
            PdfLanguage.EN to "Room Cost",
            PdfLanguage.KH to "តម្លៃបន្ទប់"
        ),
        "water_cost" to mapOf(
            PdfLanguage.EN to "Water Cost",
            PdfLanguage.KH to "តម្លៃទឹក"
        ),
        "total" to mapOf(
            PdfLanguage.EN to "Total",
            PdfLanguage.KH to "សរុប"
        ),
        "summary" to mapOf(
            PdfLanguage.EN to "Summary",
            PdfLanguage.KH to "សង្ខេប"
        ),
        "total_records" to mapOf(
            PdfLanguage.EN to "Total Records",
            PdfLanguage.KH to "កំណត់ត្រាសរុប"
        ),
        "paid" to mapOf(
            PdfLanguage.EN to "Paid",
            PdfLanguage.KH to "បានបង់"
        ),
        "unpaid" to mapOf(
            PdfLanguage.EN to "Unpaid",
            PdfLanguage.KH to "មិនទាន់បង់"
        ),
        "grand_total" to mapOf(
            PdfLanguage.EN to "Grand Total",
            PdfLanguage.KH to "សរុបរួម"
        )
    )

    fun get(key: String, lang: PdfLanguage): String =
        translations[key]?.get(lang) ?: translations[key]?.get(PdfLanguage.EN) ?: key
}

