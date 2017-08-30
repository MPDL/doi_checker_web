package logic;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CheckDOIjob implements Job {
	public CheckDOIjob() {
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("EXECUTE JOB!!!");
	
		DoiChecker checker = new DoiChecker(DoiChecker.DOXI_URL);
		try {
			checker.check();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
