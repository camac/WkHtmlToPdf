package com.gregorbyte.xsp.wkhtmltopdf.job;

import java.io.File;
import java.io.IOException;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.gregorbyte.xsp.wkhtmltopdf.jna.WKHtmlToPdf;
import com.ibm.domino.xsp.module.nsf.ThreadSessionExecutor;
import com.sun.jna.Pointer;

final class PdfJob extends Job {

	boolean cancel; // Does nothing at the moment

	String title;
	private ThreadSessionExecutor<IStatus> executor;

	String task;
	int taskCompletion;
	int completion;
	private PdfRequest request;

	public PdfJob(String title, PdfRequest prequest) {
		super(title);
		this.title = title;
		this.request = prequest;

		this.executor = new ThreadSessionExecutor<IStatus>() {
			@Override
			protected IStatus run(Session session) throws NotesException {

				synchronized (PdfScheduler.class) {
					task = "Setup";
				}
				WKHtmlToPdf whtp = null;
				try {
					whtp = WKHtmlToPdf.INSTANCE;
				} catch (UnsatisfiedLinkError e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}

				// This Code must only be called by One Thread at a time
				synchronized (whtp) {

					whtp.wkhtmltopdf_init(0);

					String version = whtp.wkhtmltopdf_version();
					System.out.println(version);

					Pointer gs = whtp.wkhtmltopdf_create_global_settings();

					File mydir = new File("V:\\wkhtmltopdftest");

					File temp;
					try {
						temp = File.createTempFile("wku", ".pdf", mydir);

						System.out.println(temp.getAbsolutePath());

						whtp.wkhtmltopdf_set_global_setting(gs, "out",
								temp.getAbsolutePath());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						whtp.wkhtmltopdf_set_global_setting(gs, "out",
								"V:\\wkhtmltopdftest\\uhoh.pdf");
					}

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
									"http://www.jord.com.au");
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

					synchronized (PdfScheduler.class) {
						task = "Setup";
					}

					try {
						/* Perform the actual conversion */
						if (whtp.wkhtmltopdf_convert(c) != 1)
							System.err.println("Conversion failed!");
					} catch (Exception e) {
						e.printStackTrace();
					}

					System.out.println("Converted");

					synchronized (PdfScheduler.class) {
						task = "Finishing";
					}

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

				return Status.OK_STATUS;

			}
		};

	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			return executor.run();
		} catch (Exception ex) {
			return Status.CANCEL_STATUS;
		}
	}

	protected int muldiv(int a, int b, int div) {
		return (int) ((long) a * (long) b / (long) div);
	}
}