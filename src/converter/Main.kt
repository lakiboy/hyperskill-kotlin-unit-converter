package converter

import converter.MeasurementCategory.*
import converter.MeasurementUnit.*
import java.util.*

class Converter(private val source: MeasurementUnit, private val target: MeasurementUnit) {
    fun convert(amount: Double) = when (source to target) {
        F to C -> (amount - 32) * 5 / 9
        C to F -> amount * 9 / 5 + 32
        K to C -> amount - 273.15
        C to K -> amount + 273.15
        F to K -> (amount + 459.67) * 5 / 9
        K to F -> amount * 9 / 5 - 459.67
        else -> source.standardUnits * amount / target.standardUnits
    }
}

enum class MeasurementCategory(val allowNegative: Boolean = false) {
    LENGTH,
    WEIGHT,
    TEMPERATURE(true);

    val title: String
        get() = name.toLowerCase().capitalize()
}

enum class MeasurementUnit(val category: MeasurementCategory, private val names: Array<String>, val standardUnits: Double = 1.0) {
    M(LENGTH, arrayOf("m", "meter", "meters")),
    KM(LENGTH, arrayOf("km", "kilometer", "kilometers"), 1000.0),
    CM(LENGTH, arrayOf("cm", "centimeter", "centimeters"), 0.01),
    MM(LENGTH, arrayOf("mm", "millimeter", "millimeters"), 0.001),
    MI(LENGTH, arrayOf("mi", "mile", "miles"), 1609.35),
    YD(LENGTH, arrayOf("yd", "yard", "yards"), 0.9144),
    FT(LENGTH, arrayOf("ft", "foot", "feet"), 0.3048),
    IN(LENGTH, arrayOf("in", "inch", "inches"), 0.0254),

    G(WEIGHT, arrayOf("g", "gram", "grams")),
    KG(WEIGHT, arrayOf("kg", "kilogram", "kilograms"), 1000.0),
    MG(WEIGHT, arrayOf("mg", "milligram", "milligrams"), 0.001),
    LB(WEIGHT, arrayOf("lb", "pound", "pounds"), 453.592),
    OZ(WEIGHT, arrayOf("oz", "ounce", "ounces"), 28.3495),

    C(TEMPERATURE, arrayOf("c", "degree Celsius", "degrees Celsius", "celsius", "dc")),
    F(TEMPERATURE, arrayOf("f", "degree Fahrenheit", "degrees Fahrenheit", "fahrenheit", "df")),
    K(TEMPERATURE, arrayOf("k", "Kelvin", "Kelvins"));

    private val singular: String
        get() = names[1]

    val plural: String
        get() = names[2]

    fun canConvert(to: MeasurementUnit) = category == to.category

    fun onlyPositive() = !category.allowNegative

    fun render(amount: Double) = "$amount " + if (amount != 1.0) plural else singular

    companion object {
        fun fromName(name: String) = values().first {
            // Improve it.
            name.toLowerCase() in it.names.map(String::toLowerCase)
        }
    }
}

fun convert(input: String): String {
    val regex = "^(?<number>[-\\d.]+) (?<source>[\\w ]+?) (?:\\w+) (?<target>(degrees? )?[\\w]+)$"
    val match = regex.toRegex(RegexOption.IGNORE_CASE).find(input) ?: return "Parse error\n"

    val source: MeasurementUnit?
    val target: MeasurementUnit?

    // Validate conversion.
    source = try {
        MeasurementUnit.fromName(match.groupValues[2])
    } catch (e: Exception) {
        null
    }
    target = try {
        MeasurementUnit.fromName(match.groupValues[3])
    } catch (e: Exception) {
        null
    }
    if (source == null || target == null || ! source.canConvert(target)) {
        return "Conversion from ${source?.plural ?: "???"} to ${target?.plural ?: "???"} is impossible\n"
    }

    val amount = match.groupValues[1].toDouble()

    // Validate amount.
    if (source.onlyPositive() && amount < 0) {
        return "${source.category.title} shouldn't be negative\n"
    }

    // Perform conversion.
    val result = Converter(source, target).convert(amount)

    return "${source.render(amount)} is ${target.render(result)}\n"
}

fun main() {
    val scanner = Scanner(System.`in`)
    do {
        print("Enter what you want to convert (or exit): ")
        val input = scanner.nextLine()
        if (input == "exit") break
        print(convert(input))
    } while (true)
}
