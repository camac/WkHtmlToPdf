package com.gregorbyte.xsp.wkhtmltopdf;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;

import javax.faces.context.FacesContext;

import com.ibm.xsp.application.UniqueViewIdManager;
import com.ibm.xsp.library.XspContributor;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class WkHtmlToPdfBean implements Serializable {

	private static final long serialVersionUID = 7123600219200007207L;

	public WkHtmlToPdfBean() {
		
	}
	
	public void submitPdfRequest() {
		
		String viewid = UniqueViewIdManager.getUniqueViewId(FacesContext.getCurrentInstance().getViewRoot());
		
		PdfRequest pr = new PdfRequest("http://www.google.com.au");
		PdfScheduler.submit(pr);
		
		
	}
	
	public static void register() {
		
		BlockingQueue<String> queue = Activator.getQueue();
		
		try {
			queue.put("Test");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	

	public static void start() {
		
		WKHtmlToPdf whtp = null; 
		try {
			whtp = WKHtmlToPdf.INSTANCE;
			//whtp = (WKHtmlToPdf) Native.loadLibrary("wkhtmltox", WKHtmlToPdf.class);
		} catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
			return; 
		}
		
		whtp.wkhtmltopdf_init(0);

		String version = whtp.wkhtmltopdf_version();
		System.out.println(version);

		Pointer gs = whtp.wkhtmltopdf_create_global_settings();

		whtp.wkhtmltopdf_set_global_setting(gs,
				"out", "V:\\wkhtmltopdftest\\test2.pdf");
		// WKHtmlToPdf.INSTANCE.wkhtmltopdf_set_global_setting(gs,
		// "load.cookieJar", cookieJar.getAbsolutePath());

		/*
		 * Create a input object settings object that is used to
		 * store settings related to a input object, note again that
		 * control of this object is parsed to the converter later,
		 * which is then responsible for freeing it
		 */
		Pointer os = whtp.wkhtmltopdf_create_object_settings();
		/*
		 * We want to convert to convert the qstring documentation
		 * page
		 */
		whtp.wkhtmltopdf_set_object_setting(os,
				"page", "http://motherfuckingwebsite.com");
		// WKHtmlToPdf.INSTANCE.wkhtmltopdf_set_object_setting(os,
		// "load.cookies", "Test test");

		/*
		 * Create the actual converter object used to convert the
		 * pages
		 */
		Pointer c = whtp.wkhtmltopdf_create_converter(gs);

		/* Call the progress_changed function when progress changes */
		// wkhtmltopdf_set_progress_changed_callback(c,
		// progress_changed);

		/* Call the phase _changed function when the phase changes */
		// wkhtmltopdf_set_phase_changed_callback(c, phase_changed);

		/* Call the error function when an error occures */
		// wkhtmltopdf_set_error_callback(c, error);

		/* Call the warning function when a warning is issued */
		// wkhtmltopdf_set_warning_callback(c, warning);

		/*
		 * Add the the settings object describing the qstring
		 * documentation page to the list of pages to convert.
		 * Objects are converted in the order in which they are
		 * added
		 */
		whtp.wkhtmltopdf_add_object(c, os, null);

		System.out.println("about to convert");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}

		/* Perform the actual conversion */
		try {
			if (whtp.wkhtmltopdf_convert(c) != 1)
				System.err.println("Conversion failed!");
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Converted");

		/* Output possible http error code encountered */
		// printf("httpErrorCode: %d\n",
		// wkhtmltopdf_http_error_code(c));

		/* Destroy the converter object since we are done with it */
		whtp.wkhtmltopdf_destroy_converter(c);

		//whtp.wkhtmltopdf_deinit();
		System.out.println("in thread completed");
		
		
	}
	
}
