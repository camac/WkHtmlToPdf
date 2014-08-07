package com.gregorbyte.xsp.wkhtmltopdf;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	private static BlockingQueue<String> queue = null;
	
	static BundleContext getContext() {
		return context;
	}

	public static BlockingQueue<String> getQueue() {		
		return queue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		System.out.println("Started WkHtmlToPdf bundle");
		Activator.queue = new ArrayBlockingQueue<String>(50);
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		//WKHtmlToPdf.INSTANCE.wkhtmltopdf_deinit();
	}

}
