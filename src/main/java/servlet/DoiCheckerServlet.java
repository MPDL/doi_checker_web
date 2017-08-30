package servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import logic.CheckDOIjob;
import logic.DoiChecker;
import logic.DoiResponse;

/**
 * Servlet implementation class DoiCheckerServlet
 */
public class DoiCheckerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public Scheduler doiCheckerScheduler;

	/**
	 * Default constructor.
	 */
	public DoiCheckerServlet() {
		try {
			JobDetail job1 = JobBuilder.newJob(CheckDOIjob.class).withIdentity("job1", "group1").build();

			Trigger trigger = TriggerBuilder.newTrigger().withIdentity("doiCheckerTrigger", "group1")
					.startAt(DateBuilder.todayAt(9, 10, 0)) // first fire time 15:00:00 tomorrow
					.withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(24) // interval is actually
																									// set at 24 hours'
																									// worth of
																									// milliseconds
							// .withIntervalInSeconds(5)
							.repeatForever())
					.build();

			doiCheckerScheduler = new StdSchedulerFactory().getScheduler();
			doiCheckerScheduler.start();
			doiCheckerScheduler.scheduleJob(job1, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public void destroy() {
		try {
			doiCheckerScheduler.shutdown();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("Get method called");
		response.getWriter().append("Hallo! Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			String doi = request.getParameter("DOI");
			DoiChecker myDoiChecker = new DoiChecker(DoiChecker.DOXI_URL);
			List<DoiResponse> myList = myDoiChecker.getHistorie(doi);

			response.getWriter().write(myList.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
