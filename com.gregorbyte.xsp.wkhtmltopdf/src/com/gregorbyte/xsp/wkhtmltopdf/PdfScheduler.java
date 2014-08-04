package com.gregorbyte.xsp.wkhtmltopdf;

import java.util.concurrent.ConcurrentHashMap;

import lotus.domino.Session;

import org.eclipse.core.runtime.IStatus;

import com.gregorbyte.xsp.wkhtmltopdf.WkHtmlToPdfBean.PdfJob;
import com.ibm.domino.xsp.module.nsf.ThreadSessionExecutor;

public class PdfScheduler {

	private static ConcurrentHashMap<String, PdfJob> jobs = null;
	
	private static ThreadSessionExecutor<IStatus> executor = null;
	
	public PdfScheduler() {
		
		this.executor = new ThreadSessionExecutor<IStatus>() {

			@Override
			protected IStatus run(Session session) throws Exception {

				
				
				return null;
			}
			
		};
	}
	
	public static void addToQueue() {
		
	}
	
	public static void start() {
		synchronized (PdfScheduler.class) {
			// Start a job here 
		}
	}
	
	public static void cancel() {
		synchronized (PdfScheduler.class) {
			
		}		
	}
	
}
