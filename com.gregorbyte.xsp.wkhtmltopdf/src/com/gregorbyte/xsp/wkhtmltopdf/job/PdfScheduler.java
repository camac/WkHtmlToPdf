package com.gregorbyte.xsp.wkhtmltopdf.job;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.gregorbyte.xsp.wkhtmltopdf.taj.JobScheduler;
import com.ibm.commons.Platform;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.std.ObjectObject;
import com.ibm.jscript.types.FBSNull;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;

public class PdfScheduler {

	private PdfJob runningJob;
	private BlockingQueue<PdfRequest> queue = new ArrayBlockingQueue<PdfRequest>(50);
	
	public BlockingQueue<PdfRequest> getQueue() {
		return this.queue;
	}
	
	public void cancel() {
		synchronized (PdfScheduler.class) {
			if (runningJob != null) {
				runningJob.cancel = true;
			}
		}
	}

	public int getNumberOfJobsInQueue() {
		return queue.size();
	}
	
	public void testSubmit(PdfRequest request) {
		queue.add(request);
	}
	
	public void submit(PdfRequest request) {
		
		// if There is no running job - Start it
		// Else add to the Queue
		
		synchronized (PdfScheduler.class) {
			if (runningJob == null) {
				runningJob = new PdfJob("PDF Something", request);
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

	public void start() {
		synchronized (PdfScheduler.class) {
			if (runningJob == null) {
				runningJob = new PdfJob("PDF Something", null);
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

	public FBSValue getJobProgressJavaScript() {
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
	};

}
