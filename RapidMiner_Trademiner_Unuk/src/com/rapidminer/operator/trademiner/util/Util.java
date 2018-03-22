package com.rapidminer.operator.trademiner.util;

import java.util.Date;

/* This is a system to proof a concept, not a commercial product, then the assertions were spreaded along the code
 /* to validate the results all the time, as this is a long mining process.
 */
import org.junit.Assert;

public class Util {

	static {

		java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("GMT"));
		
		Date d0 = new Date(); // 0 time
		d0.setTime(0);
		Assert.assertEquals("Invalid convertion from milli to minutes", 0L, convertToMinutes(d0.getTime()));
		Date date = new Date(70, 0, 2, 0, 0, 0); // One day after Thu Jan 01 01:00:00 GMT 1970
		long millisecondsSince1970 = date.getTime();
		long expectedMinutes = 24 * 60;

		Assert.assertEquals("milliseconds one day after 1970", (long)(24 * 60 * 60 * 1000), millisecondsSince1970);
		Assert.assertEquals("Invalid convertion from milli to minutes", expectedMinutes, convertToMinutes(millisecondsSince1970));
	}

	public static long convertToMinutes(long milliseconds) {
		long converted = milliseconds / 60000;
		return converted;

	}

	public static void main(String args[]) {
	}

}
