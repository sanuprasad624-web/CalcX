package com.example.engine

data class UnitCategory(
    val id: String,
    val name: String,
    val iconName: String,
    val units: List<UnitItem>
)

data class UnitItem(
    val id: String,
    val name: String,
    val symbol: String,
    val toBaseMultiplier: Double = 1.0,
    val isAffine: Boolean = false // for temperature
)

object UnitConversionEngine {

    val categories: List<UnitCategory> = listOf(
        UnitCategory(
            id = "length",
            name = "Length",
            iconName = "straighten",
            units = listOf(
                UnitItem("m", "Meter", "m", 1.0),
                UnitItem("km", "Kilometer", "km", 1000.0),
                UnitItem("cm", "Centimeter", "cm", 0.01),
                UnitItem("mm", "Millimeter", "mm", 0.001),
                UnitItem("um", "Micrometer", "µm", 1e-6),
                UnitItem("nm", "Nanometer", "nm", 1e-9),
                UnitItem("inch", "Inch", "in", 0.0254),
                UnitItem("ft", "Foot", "ft", 0.3048),
                UnitItem("yd", "Yard", "yd", 0.9144),
                UnitItem("mi", "Mile", "mi", 1609.344),
                UnitItem("nmi", "Nautical Mile", "nmi", 1852.0),
                UnitItem("ly", "Light Year", "ly", 9.46073e15),
                UnitItem("au", "Astronomical Unit", "AU", 1.4959787e11)
            )
        ),
        UnitCategory(
            id = "mass",
            name = "Mass",
            iconName = "scale",
            units = listOf(
                UnitItem("kg", "Kilogram", "kg", 1.0),
                UnitItem("g", "Gram", "g", 0.001),
                UnitItem("mg", "Milligram", "mg", 1e-6),
                UnitItem("ug", "Microgram", "µg", 1e-9),
                UnitItem("ton", "Metric Ton", "t", 1000.0),
                UnitItem("lb", "Pound", "lb", 0.45359237),
                UnitItem("oz", "Ounce", "oz", 0.028349523125),
                UnitItem("ct", "Carat", "ct", 0.0002),
                UnitItem("u", "Atomic Mass Unit", "u", 1.660539e-27)
            )
        ),
        UnitCategory(
            id = "temperature",
            name = "Temperature",
            iconName = "thermostat",
            units = listOf(
                UnitItem("c", "Celsius", "°C", isAffine = true),
                UnitItem("f", "Fahrenheit", "°F", isAffine = true),
                UnitItem("k", "Kelvin", "K", isAffine = true),
                UnitItem("r", "Rankine", "°R", isAffine = true)
            )
        ),
        UnitCategory(
            id = "area",
            name = "Area",
            iconName = "aspect_ratio",
            units = listOf(
                UnitItem("m2", "Square Meter", "m²", 1.0),
                UnitItem("km2", "Square Kilometer", "km²", 1e6),
                UnitItem("cm2", "Square Centimeter", "cm²", 1e-4),
                UnitItem("ha", "Hectare", "ha", 10000.0),
                UnitItem("acre", "Acre", "ac", 4046.8564224),
                UnitItem("ft2", "Square Foot", "ft²", 0.09290304),
                UnitItem("yd2", "Square Yard", "yd²", 0.83612736),
                UnitItem("mi2", "Square Mile", "mi²", 2.589988e6)
            )
        ),
        UnitCategory(
            id = "volume",
            name = "Volume",
            iconName = "opacity",
            units = listOf(
                UnitItem("l", "Liter", "L", 1.0),
                UnitItem("ml", "Milliliter", "mL", 0.001),
                UnitItem("m3", "Cubic Meter", "m³", 1000.0),
                UnitItem("gal_us", "Gallon (US)", "gal", 3.785411784),
                UnitItem("qt_us", "Quart (US)", "qt", 0.946352946),
                UnitItem("pt_us", "Pint (US)", "pt", 0.473176473),
                UnitItem("cup", "Cup (US)", "cup", 0.236588),
                UnitItem("floz", "Fluid Ounce (US)", "fl oz", 0.0295735),
                UnitItem("ft3", "Cubic Foot", "ft³", 28.3168)
            )
        ),
        UnitCategory(
            id = "speed",
            name = "Speed",
            iconName = "speed",
            units = listOf(
                UnitItem("mps", "Meter per Second", "m/s", 1.0),
                UnitItem("kmh", "Kilometer per Hour", "km/h", 1.0 / 3.6),
                UnitItem("mph", "Mile per Hour", "mph", 0.44704),
                UnitItem("knot", "Knot", "kn", 0.514444),
                UnitItem("fps", "Foot per Second", "ft/s", 0.3048),
                UnitItem("c", "Speed of Light", "c", 299792458.0)
            )
        ),
        UnitCategory(
            id = "time",
            name = "Time",
            iconName = "schedule",
            units = listOf(
                UnitItem("s", "Second", "s", 1.0),
                UnitItem("ms", "Millisecond", "ms", 0.001),
                UnitItem("us", "Microsecond", "µs", 1e-6),
                UnitItem("ns", "Nanosecond", "ns", 1e-9),
                UnitItem("min", "Minute", "min", 60.0),
                UnitItem("h", "Hour", "h", 3600.0),
                UnitItem("d", "Day", "d", 86400.0),
                UnitItem("wk", "Week", "wk", 604800.0),
                UnitItem("yr", "Year (365d)", "yr", 31536000.0)
            )
        ),
        UnitCategory(
            id = "pressure",
            name = "Pressure",
            iconName = "compress",
            units = listOf(
                UnitItem("pa", "Pascal", "Pa", 1.0),
                UnitItem("kpa", "Kilopascal", "kPa", 1000.0),
                UnitItem("bar", "Bar", "bar", 100000.0),
                UnitItem("atm", "Atmosphere", "atm", 101325.0),
                UnitItem("psi", "Pounds per Sq Inch", "psi", 6894.757),
                UnitItem("torr", "Torr / mmHg", "mmHg", 133.322)
            )
        ),
        UnitCategory(
            id = "energy",
            name = "Energy",
            iconName = "bolt",
            units = listOf(
                UnitItem("j", "Joule", "J", 1.0),
                UnitItem("kj", "Kilojoule", "kJ", 1000.0),
                UnitItem("cal", "Calorie", "cal", 4.184),
                UnitItem("kcal", "Kilocalorie", "kcal", 4184.0),
                UnitItem("wh", "Watt-Hour", "Wh", 3600.0),
                UnitItem("kwh", "Kilowatt-Hour", "kWh", 3.6e6),
                UnitItem("ev", "Electronvolt", "eV", 1.602176634e-19),
                UnitItem("btu", "BTU", "BTU", 1055.06)
            )
        ),
        UnitCategory(
            id = "power",
            name = "Power",
            iconName = "offline_bolt",
            units = listOf(
                UnitItem("w", "Watt", "W", 1.0),
                UnitItem("kw", "Kilowatt", "kW", 1000.0),
                UnitItem("mw", "Megawatt", "MW", 1e6),
                UnitItem("hp", "Horsepower", "hp", 745.69987),
                UnitItem("ft_lbf_s", "Ft-lb/s", "ft-lb/s", 1.355818)
            )
        ),
        UnitCategory(
            id = "force",
            name = "Force",
            iconName = "fitness_center",
            units = listOf(
                UnitItem("n", "Newton", "N", 1.0),
                UnitItem("kn", "Kilonewton", "kN", 1000.0),
                UnitItem("dyn", "Dyne", "dyn", 1e-5),
                UnitItem("lbf", "Pound-force", "lbf", 4.448222),
                UnitItem("kgf", "Kilogram-force", "kgf", 9.80665)
            )
        ),
        UnitCategory(
            id = "electric",
            name = "Electricity",
            iconName = "electrical_services",
            units = listOf(
                UnitItem("v", "Volt", "V", 1.0),
                UnitItem("mv", "Millivolt", "mV", 0.001),
                UnitItem("kv", "Kilovolt", "kV", 1000.0),
                UnitItem("a", "Ampere", "A", 1.0),
                UnitItem("ma", "Milliampere", "mA", 0.001),
                UnitItem("ohm", "Ohm", "Ω", 1.0),
                UnitItem("kohm", "Kilohm", "kΩ", 1000.0),
                UnitItem("mohm", "Megaohm", "MΩ", 1e6)
            )
        ),
        UnitCategory(
            id = "angle",
            name = "Angle",
            iconName = "change_history",
            units = listOf(
                UnitItem("deg", "Degree", "°", 1.0),
                UnitItem("rad", "Radian", "rad", 180.0 / Math.PI),
                UnitItem("grad", "Gradian", "grad", 0.9),
                UnitItem("arcmin", "Arcminute", "arcmin", 1.0 / 60.0),
                UnitItem("arcsec", "Arcsecond", "arcsec", 1.0 / 3600.0)
            )
        ),
        UnitCategory(
            id = "data",
            name = "Data Storage",
            iconName = "storage",
            units = listOf(
                UnitItem("b", "Byte", "B", 1.0),
                UnitItem("bit", "Bit", "bit", 0.125),
                UnitItem("kb", "Kilobyte (1024B)", "KB", 1024.0),
                UnitItem("mb", "Megabyte", "MB", 1048576.0),
                UnitItem("gb", "Gigabyte", "GB", 1073741824.0),
                UnitItem("tb", "Terabyte", "TB", 1099511627776.0)
            )
        )
    )

    fun convert(value: Double, fromUnit: UnitItem, toUnit: UnitItem): Double {
        if (fromUnit.id == toUnit.id) return value

        if (fromUnit.isAffine || toUnit.isAffine) {
            // Temperature conversions
            val kelvin = when (fromUnit.id) {
                "c" -> value + 273.15
                "f" -> (value - 32.0) * 5.0 / 9.0 + 273.15
                "k" -> value
                "r" -> value * 5.0 / 9.0
                else -> value
            }
            return when (toUnit.id) {
                "c" -> kelvin - 273.15
                "f" -> (kelvin - 273.15) * 9.0 / 5.0 + 32.0
                "k" -> kelvin
                "r" -> kelvin * 9.0 / 5.0
                else -> kelvin
            }
        }

        val baseValue = value * fromUnit.toBaseMultiplier
        return baseValue / toUnit.toBaseMultiplier
    }
}
