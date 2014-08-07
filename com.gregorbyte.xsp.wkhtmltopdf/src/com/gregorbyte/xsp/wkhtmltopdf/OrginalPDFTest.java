package com.gregorbyte.xsp.wkhtmltopdf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;




public class OrginalPDFTest {

	public interface WKHtmlToPdf extends Library {
		
		WKHtmlToPdf INSTANCE = (WKHtmlToPdf) Native.loadLibrary("wkhtmltox", WKHtmlToPdf.class);
		
		int wkhtmltopdf_init(int use_graphics);
		
		Pointer wkhtmltopdf_create_global_settings();
		void wkhtmltopdf_destroy_global_settings(Pointer settings);

		Pointer wkhtmltopdf_create_object_settings();
		void wkhtmltopdf_destroy_object_settings(Pointer os);

		void wkhtmltopdf_set_global_setting(Pointer settings, String name, String value);
		void wkhtmltopdf_set_object_setting(Pointer os, String name, String value);
		
		Pointer wkhtmltopdf_create_converter(Pointer settings);
		void wkhtmltopdf_destroy_converter(Pointer converter);
		
		int wkhtmltopdf_convert(Pointer converter);
		void wkhtmltopdf_add_object(Pointer converter, Pointer objectSetting, String data);
		
		String wkhtmltopdf_version();
		int wkhtmltopdf_deinit();
		
		public static class wkhtmltopdf_global_settings extends Structure implements Structure.ByReference {

			@SuppressWarnings("rawtypes")
			@Override
			protected List getFieldOrder() {
				return Arrays.asList(new String[] {});
			}};
		
	}
	
	public static void main(String[] args) {
		
		BufferedWriter writer = null;
		File cookieJar = new File("testwrite.txt");
		
		try {
			//File cookieJar = File.createTempFile("cookie", null);
			
			if (cookieJar.exists()) cookieJar.delete();
			
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cookieJar)));
			writer.write("LtpaToken=");
			writer.write("M5eCVhvjQTj6cJcwRnr00YQEJby7kBfEO8mL0v2yCXFV8W+QXlGHO/niAn6fwUCsWjUUWQXWJ6EEpC1x5CRkL0KyJd79FPMtRscql7g2M89pf11aHg3uKvYGBtcJQt0ufeTBG5r3prlHGDk36TLMa5H/0dK4Jr0n9EnzTOXnW0zLlUo7Ll4qHtn9iFy1J8zyVUPRA2q3GgNJJqPE8KlFy0rs5N2Kupe0LFd5PgAzgZRFRmpCUj9hdGkGZGXyxv8BVI2U8cTtCWKXK0vQujKCzzRQ2pDODMbTg3W2h0+bvSuIINJj5hXKVlhpONFls/46A6EwMnoQc6cYUKi6Qr+F5A==");
			writer.write(";");
			writer.write(" HttpOnly; expires=Tue, 21-Oct-2014 13:37:51 GMT; domain=sydneydev.jord.com.au; path=/;");
			writer.newLine();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { writer.close(); } catch (Exception ex) {};
		}
		
		
		WKHtmlToPdf.INSTANCE.wkhtmltopdf_init(0);
		
		String version = WKHtmlToPdf.INSTANCE.wkhtmltopdf_version();
		System.out.println(version);
		
		Pointer gs = WKHtmlToPdf.INSTANCE.wkhtmltopdf_create_global_settings();
				
		WKHtmlToPdf.INSTANCE.wkhtmltopdf_set_global_setting(gs, "out", "test.pdf");
		WKHtmlToPdf.INSTANCE.wkhtmltopdf_set_global_setting(gs, "load.cookieJar", cookieJar.getAbsolutePath());

		/*
		* Create a input object settings object that is used to store settings
		* related to a input object, note again that control of this object is parsed to
		* the converter later, which is then responsible for freeing it
		*/
		Pointer os = WKHtmlToPdf.INSTANCE.wkhtmltopdf_create_object_settings();
		/* We want to convert to convert the qstring documentation page */
		WKHtmlToPdf.INSTANCE.wkhtmltopdf_set_object_setting(os, "page", "http://sydneydev.jord.com.au:8080/JobMail/C2000.nsf/MemoPrint.xsp?documentId=E7CD0731350045A1CA257D1E0025664B");
		//WKHtmlToPdf.INSTANCE.wkhtmltopdf_set_object_setting(os, "load.cookies", "Test test");

		/* Create the actual converter object used to convert the pages */
		Pointer c = WKHtmlToPdf.INSTANCE.wkhtmltopdf_create_converter(gs);

		/* Call the progress_changed function when progress changes */
		//wkhtmltopdf_set_progress_changed_callback(c, progress_changed);

		/* Call the phase _changed function when the phase changes */
		//wkhtmltopdf_set_phase_changed_callback(c, phase_changed);

		/* Call the error function when an error occures */
		//wkhtmltopdf_set_error_callback(c, error);

		/* Call the warning function when a warning is issued */
		//wkhtmltopdf_set_warning_callback(c, warning);

		/*
		* Add the the settings object describing the qstring documentation page
		* to the list of pages to convert. Objects are converted in the order in which
		* they are added
		*/
		WKHtmlToPdf.INSTANCE.wkhtmltopdf_add_object(c, os, null);

		/* Perform the actual convertion */
		if (WKHtmlToPdf.INSTANCE.wkhtmltopdf_convert(c) != 1)
			System.err.println("Conversion failed!");

		/* Output possible http error code encountered */
		//printf("httpErrorCode: %d\n", wkhtmltopdf_http_error_code(c));

		/* Destroy the converter object since we are done with it */
		WKHtmlToPdf.INSTANCE.wkhtmltopdf_destroy_converter(c);
		
		
		WKHtmlToPdf.INSTANCE.wkhtmltopdf_deinit();
		
	}

}
