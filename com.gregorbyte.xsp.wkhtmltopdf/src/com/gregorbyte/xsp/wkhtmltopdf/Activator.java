package com.gregorbyte.xsp.wkhtmltopdf;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.gregorbyte.xsp.wkhtmltopdf.job.PdfRequest;
import com.gregorbyte.xsp.wkhtmltopdf.job.PdfScheduler;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static PdfScheduler pdfScheduler = new PdfScheduler();	

	private MyThread myThread;
	
	public static BundleContext getContext() {
		return context;
	}

	public static PdfScheduler getPdfScheduler() {
		return pdfScheduler;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Activator.pdfScheduler = new PdfScheduler();
		
		System.out.println("Started WkHtmlToPdf bundle");
		
		myThread = new MyThread(Activator.pdfScheduler);
		myThread.start();
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		//WKHtmlToPdf.INSTANCE.wkhtmltopdf_deinit();
		
		Activator.pdfScheduler.getQueue().put(new PdfRequest("stop", "stop"));
		
		myThread.join();
		System.out.println("Stopped WkHtmlToPdf bundle");
		
	}

}
