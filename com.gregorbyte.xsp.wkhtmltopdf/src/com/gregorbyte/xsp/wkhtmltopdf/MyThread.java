package com.gregorbyte.xsp.wkhtmltopdf;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.Status;

import com.gregorbyte.xsp.wkhtmltopdf.jna.WKHtmlToPdf;
import com.gregorbyte.xsp.wkhtmltopdf.job.PdfRequest;
import com.gregorbyte.xsp.wkhtmltopdf.job.PdfScheduler;
import com.sun.jna.Pointer;

public class MyThread extends Thread {

	private volatile boolean active = true;
	private PdfScheduler pdfScheduler; 
	
	public MyThread(PdfScheduler pdfScheduler) {
		this.pdfScheduler = pdfScheduler;
	}
	
	public void run() {
		while (active) {
			
			PdfRequest request = null;
			try {
				// Maybe do Poll instead?
				request = pdfScheduler.getQueue().take();

				if (request.getViewId().equals("stop")) {
					active = false;
					System.out.println("Request to Stop received");
				} else {

					System.out.println("Thanks for the Request ("+ request.getViewId() + "): " + request.getUrl());
					
					try {
						pdfSomething(request);
					} catch (UnsatisfiedLinkError e) {
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				
				try {
					Thread.sleep(1000);					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}			
			
		}
	}
	
	public void stopThread() {
		active = false;
	}
	
	private void pdfSomething(PdfRequest request) throws UnsatisfiedLinkError, IOException {
		
		WKHtmlToPdf whtp = WKHtmlToPdf.INSTANCE;

		File temp = File.createTempFile("wku", ".pdf");
		System.out.println(temp.getAbsolutePath());
		
		// This Code must only be called by One Thread at a time
		synchronized (whtp) {
			
			whtp.wkhtmltopdf_init(0);

			String version = whtp.wkhtmltopdf_version();
			System.out.println(version);

			Pointer gs = whtp.wkhtmltopdf_create_global_settings();

			whtp.wkhtmltopdf_set_global_setting(gs, "out",
					temp.getAbsolutePath());

			// whtp.wkhtmltopdf_set_global_setting(gs,
			// "load.cookieJar", cookieJar.getAbsolutePath());

			/*
			 * Create a input object settings object that is used to
			 * store settings related to a input object, note again
			 * that control of this object is parsed to the
			 * converter later, which is then responsible for
			 * freeing it
			 */
			Pointer os = whtp.wkhtmltopdf_create_object_settings();
			/*
			 * We want to convert to convert the qstring
			 * documentation page
			 */
			if (request != null) {
				synchronized (PdfScheduler.class) {
					whtp.wkhtmltopdf_set_object_setting(os, "page",
							request.getUrl());
				}
			} else {
				synchronized (PdfScheduler.class) {
					whtp.wkhtmltopdf_set_object_setting(os, "page",
							"http://en.wikipedia.org/wiki/Cat");
				}
			}

			/*
			 * Create the actual converter object used to convert
			 * the pages
			 */
			Pointer c = whtp.wkhtmltopdf_create_converter(gs);

			/*
			 * Call the progress_changed function when progress
			 * changes
			 */
			// wkhtmltopdf_set_progress_changed_callback(c,
			// progress_changed);

			/*
			 * Call the phase _changed function when the phase
			 * changes
			 */
			// wkhtmltopdf_set_phase_changed_callback(c,
			// phase_changed);

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
				/* Perform the actual conversion */
				if (whtp.wkhtmltopdf_convert(c) != 1)
					System.err.println("Conversion failed!");
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("Converted");

			/* Output possible http error code encountered */
			// printf("httpErrorCode: %d\n",
			// wkhtmltopdf_http_error_code(c));

			/*
			 * Destroy the converter object since we are done with
			 * it
			 */
			whtp.wkhtmltopdf_destroy_converter(c);
		}

		System.out.println("Job completed");
		
	}
	
}
