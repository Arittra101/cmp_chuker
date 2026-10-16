package org.getscol.scol_chuker.plugin

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

internal actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()

internal actual fun formatClockTime(epochMillis: Long): String =
    format(epochMillis, "HH:mm:ss.SSS")

internal actual fun formatDateTime(epochMillis: Long): String =
    format(epochMillis, "yyyy-MM-dd HH:mm:ss.SSS")

private fun format(epochMillis: Long, pattern: String): String {
    val formatter = platform.Foundation.NSDateFormatter()
    // POSIX locale: "HH" must mean a 24-hour clock whatever the device's region says.
    formatter.locale = platform.Foundation.NSLocale(localeIdentifier = "en_US_POSIX")
    formatter.dateFormat = pattern
    // NSDate's designated initializer counts from 2001-01-01; NSTimeIntervalSince1970 bridges it.
    val sinceReference = epochMillis / 1000.0 - platform.Foundation.NSTimeIntervalSince1970
    return formatter.stringFromDate(NSDate(timeIntervalSinceReferenceDate = sinceReference))
}
