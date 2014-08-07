package com.gregorbyte.xsp.wkhtmltopdf;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public interface WKHtmlToPdf extends Library {

	WKHtmlToPdf INSTANCE = (WKHtmlToPdf) Native.loadLibrary("wkhtmltox",
			WKHtmlToPdf.class);

	int wkhtmltopdf_init(int use_graphics);

	Pointer wkhtmltopdf_create_global_settings();

	void wkhtmltopdf_destroy_global_settings(Pointer settings);

	Pointer wkhtmltopdf_create_object_settings();

	void wkhtmltopdf_destroy_object_settings(Pointer os);

	void wkhtmltopdf_set_global_setting(Pointer settings, String name,
			String value);

	void wkhtmltopdf_set_object_setting(Pointer os, String name,
			String value);

	Pointer wkhtmltopdf_create_converter(Pointer settings);

	void wkhtmltopdf_destroy_converter(Pointer converter);

	int wkhtmltopdf_convert(Pointer converter);

	void wkhtmltopdf_add_object(Pointer converter, Pointer objectSetting,
			String data);

	String wkhtmltopdf_version();

	int wkhtmltopdf_deinit();

	public static class wkhtmltopdf_global_settings extends Structure
			implements Structure.ByReference {

		@SuppressWarnings("rawtypes")
		@Override
		protected List getFieldOrder() {
			return Arrays.asList(new String[] {});
		}
	};

}