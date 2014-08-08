/*
 * © Copyright IBM Corp. 2012
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package com.gregorbyte.xsp.wkhtmltopdf.taj;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Date;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

import com.ibm.commons.Platform;
import com.ibm.commons.util.StringUtil;
import com.ibm.domino.xsp.module.nsf.ThreadSessionExecutor;
import com.ibm.jscript.InterpretException;
import com.ibm.jscript.std.ObjectObject;
import com.ibm.jscript.types.FBSNull;
import com.ibm.jscript.types.FBSNumber;
import com.ibm.jscript.types.FBSString;
import com.ibm.jscript.types.FBSUtility;
import com.ibm.jscript.types.FBSValue;

public class JobScheduler {

	public static void cancel() {
		synchronized (JobScheduler.class) {
			if (runningJob != null) {
				runningJob.cancel = true;
			}
		}
	}

	public static void start() {
		synchronized (JobScheduler.class) {
			if (runningJob == null) {
				runningJob = new DemoJob("XPages Demo Database");
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
					o.put("taskTitle", FBSUtility.wrap(runningJob.task));
					o.put("taskCompletion", FBSUtility
							.wrap(runningJob.taskCompletion));
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

		private boolean cancel;

		private String title;
		private ThreadSessionExecutor<IStatus> executor;

		private String task;
		private int taskCompletion;
		private int completion;

		public DemoJob(String title) {
			super(title);
			this.title = title;

			this.executor = new ThreadSessionExecutor<IStatus>() {
				@Override
				protected IStatus run(Session session) throws NotesException {

					System.out.println("Job started");
					System.out.println("   >> Session created: "
							+ session.getUserName() + ", Effective User:"
							+ session.getEffectiveUserName());
					int numberOfTasks = 5;
					main: for (int ts = 0; ts < numberOfTasks; ts++) {
						if (cancel) {
							break main;
						}
						int baseCompletion = (ts * 100) / numberOfTasks;
						synchronized (JobScheduler.class) {
							task = StringUtil.format("Task #{0}: ", ts);
							taskCompletion = 0;
							completion = baseCompletion;
						}

						int numberOfSubTasks = (int) (Math.random() * 20);
						for (int p = 0; p < numberOfSubTasks; p++) {
							if (cancel) {
								break main;
							}
							synchronized (JobScheduler.class) {
								taskCompletion = muldiv(p, 100,
										numberOfSubTasks);
								completion = baseCompletion
										+ (taskCompletion / numberOfTasks);
							}
							try {
								System.out.println("   >> " + task + ", "
										+ taskCompletion + " [" + completion
										+ "]");
								Thread.sleep((long) (Math.random() * 300));
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							}
						}
						Database db = session.getDatabase(null,
								"ThreadsJobsPlugin.nsf");
						if (db != null) {
							if (!db.isOpen())
								db.open();
							if (db.isOpen()) {
								System.out.println("   >> Database opened: "
										+ db.getTitle());
								Document doc = db.createDocument();
								try {
									doc.replaceItemValue("Form", "JobTest");
									doc.replaceItemValue("DateTime", session
											.createDateTime(new Date()));
									doc.save();
								} finally {
									doc.recycle();
								}
							}
						}
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
	};
}
