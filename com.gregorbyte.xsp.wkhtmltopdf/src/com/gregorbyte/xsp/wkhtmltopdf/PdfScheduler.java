package com.gregorbyte.xsp.wkhtmltopdf;

import java.io.File;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.BlockingQueue;

import lotus.domino.NotesException;
import lotus.domino.Session;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.ibm.commons.Platform;
import com.ibm.domino.xsp.module.nsf.ThreadSessionExecutor;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.std.ObjectObject;
import com.ibm.jscript.types.FBSNull;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;
import com.sun.jna.Pointer;

public class PdfScheduler {

	public static void cancel() {
		synchronized (PdfScheduler.class) {
			if (runningJob != null) {
				runningJob.cancel = true;
			}
		}
	}

	public static void submit(PdfRequest request) {
		synchronized (PdfScheduler.class) {
			if (runningJob == null) {
				runningJob = new DemoJob("PDF Something",request);
				runningJob.addJobChangeListener(new JobChangeAdapter() {
					public void done(IJobChangeEvent event) {
						runningJob = null;
					}
				});
				AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						runningJob.schedule();
						return null;
					}
				});
			}
		}		
	}
	
	public static void start() {
		synchronized (PdfScheduler.class) {
			if (runningJob == null) {
				runningJob = new DemoJob("PDF Something", null);
				runningJob.addJobChangeListener(new JobChangeAdapter() {
					public void done(IJobChangeEvent event) {
						runningJob = null;
					}
				});
				AccessController.doPrivileged(new PrivilegedAction<Object>() {
					public Object run() {
						runningJob.schedule();
						return null;
					}
				});
			}
		}
	}

	public static FBSValue getJobProgressJavaScript() {
		synchronized (JobScheduler.class) {
			try {
				if (runningJob != null) {
					ObjectObject o = new ObjectObject();
					o.put("title", FBSUtility.wrap(runningJob.title));
					o.put("taskTitle",
							FBSUtility.wrap(runningJob.task + " balls"));
					o.put("taskCompletion",
							FBSUtility.wrap(runningJob.taskCompletion));
					o.put("completion", FBSUtility.wrap(runningJob.completion));
					return o;
				}
				ObjectObject o = new ObjectObject();
				o.put("title", FBSString.emptyString);
				o.put("taskTitle", FBSString.emptyString);
				o.put("taskCompletion", FBSNumber.Zero);
				o.put("completion", FBSNumber.Zero);
				return o;
			} catch (InterpretException ex) {
				Platform.getInstance().log(ex);
			}
		}
		return FBSNull.nullValue;
	}

	private static DemoJob runningJob;

	private static final class DemoJob extends Job {

		private boolean cancel; // Does nothing at the moment

		private String title;
		private ThreadSessionExecutor<IStatus> executor;

		private String task;
		private int taskCompletion;
		private int completion;
		private PdfRequest request;

		public DemoJob(String title, PdfRequest prequest) {
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
					
					whtp.wkhtmltopdf_init(0);

					String version = whtp.wkhtmltopdf_version();
					System.out.println(version);

					Pointer gs = whtp.wkhtmltopdf_create_global_settings();

					File mydir = new File("V:\\wkhtmltopdftest");
					
					File temp;
					try {
						temp = File.createTempFile("wku", ".pdf", mydir);
						
						System.out.println(temp.getAbsolutePath());
						
						whtp.wkhtmltopdf_set_global_setting(gs,
								"out", temp.getAbsolutePath());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						whtp.wkhtmltopdf_set_global_setting(gs,
								"out", "V:\\wkhtmltopdftest\\uhoh.pdf");
					}
					
					// whtp.wkhtmltopdf_set_global_setting(gs,
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
					if (request != null) {
						synchronized (PdfScheduler.class) {
							whtp.wkhtmltopdf_set_object_setting(os,
									"page", request.getUrl() );
						}
					} else {
						synchronized (PdfScheduler.class) {
							whtp.wkhtmltopdf_set_object_setting(os,
									"page", "http://www.jord.com.au" );
						}						
					}

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

					/* Destroy the converter object since we are done with it */
					whtp.wkhtmltopdf_destroy_converter(c);

					//whtp.wkhtmltopdf_deinit();

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
	};

}
