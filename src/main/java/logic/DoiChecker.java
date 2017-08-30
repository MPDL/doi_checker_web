package logic;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.postgresql.Driver;

public class DoiChecker {

	private final static String USERNAME = "mpdl_azubi";
	private final static String PASSWORD = "37L*9D'%sb4}n%";
	private final static String dbUrl = "jdbc:postgresql://localhost:5432/doi";
	private final static String PASSWORDEMAIL = "RZFUAAjT=b";
	private final static String IDEMAIL = "pubman";
	private final static String[] TO = new String [] {"mark.wagner@mpdl.mpg.de"};
	private final static String EMAILHOST = "mail.mpisoc.mpg.de";
	private final static String FROMEMAIL = "pubman@mpdl.mpg.de";
	public final static String DOXI_URL = "https://test.doi.mpdl.mpg.de/doxi/rest/doi";
	
	private String url;

	public DoiChecker(String givenUrl) {
		try {
			DriverManager.registerDriver(new Driver());
		} catch (SQLException e) {
			e.printStackTrace();
		}

		this.url = givenUrl;
	}

	public void check() throws Exception {
		URL url = new URL(this.url);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		String auth = USERNAME + ":" + PASSWORD;
		String authb64 = Base64.getEncoder().encodeToString(auth.getBytes());
		connection.setRequestProperty("Authorization", "Basic " + authb64);
		String line = null;
		InputStream input = connection.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		ArrayList<String> dois = new ArrayList();
		while ((line = reader.readLine()) != null) {
			dois.add(line);
		}
		ArrayList<String> responseCodes = new ArrayList();
		StringBuilder emailMessageBuilder = new StringBuilder();
		for (String doi : dois) {
			URL url1 = new URL("https://doi.org/" + doi);
			HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();
			connection1.setRequestMethod("GET");
			connection1.connect();
			int code1 = connection1.getResponseCode();
			responseCodes.add("DOI: " + doi + " | Response: " + code1 + "\n");
			writeToDatabase(doi, code1);
			if (code1 != 200) {
			emailMessageBuilder.append("DOI not resolved:"+doi+"\t"+code1+"\n");
			}
		}
		
		EmailSender.sendMail(EMAILHOST,IDEMAIL,FROMEMAIL, PASSWORDEMAIL,"Automatische Rückmeldung DOI-Checker",emailMessageBuilder.toString() , TO);
		//System.out.println(responseCodes);
		//System.out.println("FINISHED");

	}

	public void writeToDatabase(String doi, int responseCode) throws Exception {

		Connection con = DriverManager.getConnection(dbUrl, "postgres", "");

		String insertTableSQL = "INSERT INTO responseall" + "(Doi, ResponseCode) VALUES" + "(?,?)";
		PreparedStatement preparedStatement = con.prepareStatement(insertTableSQL);
		preparedStatement.setString(1, doi);
		preparedStatement.setInt(2, responseCode);
		// execute insert SQL stetement
		preparedStatement.executeUpdate();

		con.close();
	}

	public List<DoiResponse> getHistorie(String doi) throws SQLException {
		List<DoiResponse> doiresplist = new ArrayList();
		Connection con = DriverManager.getConnection(dbUrl, "postgres", "");
		String query = "SELECT * FROM responseAll WHERE Doi = ? ORDER BY timestamp DESC;";
		PreparedStatement select = con.prepareStatement(query);
		select.setString(1, doi);
		ResultSet rs = select.executeQuery();
		while (rs.next()) {
			String doi1 = rs.getString("Doi");
			int responseCode = rs.getInt("responsecode");
			Timestamp timestamp = rs.getTimestamp("timestamp");
			DoiResponse doiresp = new DoiResponse(doi1, responseCode, timestamp);
			//System.out.println("Alle Einträge zur Doi " + doiresp);
			doiresplist.add(doiresp);
		}
		con.close();
		return doiresplist;
	}

	public List<DoiResponse> getByResponseCode(int responseCode) throws SQLException {
		List<DoiResponse> doirespcodelist = new ArrayList();
		Connection con = DriverManager.getConnection(dbUrl, "postgres", "");
		String query = "SELECT * FROM responseAll WHERE responsecode = ? ORDER BY Doi ASC";
		PreparedStatement select = con.prepareStatement(query);
		select.setInt(1, responseCode);
		ResultSet rs = select.executeQuery();
		while (rs.next()) {
			String doi1 = rs.getString("Doi");
			int responseCode1 = rs.getInt("responsecode");
			Timestamp timestamp = rs.getTimestamp("timestamp");
			DoiResponse doirespcode = new DoiResponse(doi1, responseCode1, timestamp);
			//System.out.println("Alle Einträge zur Doi " + doirespcode);
			doirespcodelist.add(doirespcode);
		}
		con.close();
		return doirespcodelist;
	}

	public static void main(String args[]) throws Exception {
		EmailSender.sendMail(EMAILHOST,IDEMAIL,FROMEMAIL, PASSWORDEMAIL,"Automatische Rückmeldung DOI-Checker", "Test Message", TO);
		
		DoiChecker myDoiChecker = new DoiChecker(DOXI_URL);
		myDoiChecker.check();

		List<DoiResponse> myList = myDoiChecker.getHistorie("");
		System.out.print("FERTIG");

	}

}
