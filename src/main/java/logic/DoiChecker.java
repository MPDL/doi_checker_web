package logic;

import java.io.BufferedReader;
import java.io.IOException;
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
import java.util.Properties;

import org.postgresql.Driver;

public class DoiChecker {

	public static Properties PROPERTIES;

	static {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream input = classLoader.getResourceAsStream("DoiChecker.properties");
			PROPERTIES = new Properties();
			PROPERTIES.load(input);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// Definition von Konstanten
	private final static String USERNAME = PROPERTIES.getProperty("DOXI_USERNAME");
	private final static String PASSWORD = PROPERTIES.getProperty("DOXI_PASSWORD");
	private final static String dbUrl = PROPERTIES.getProperty("DB_URL");
	private final static String PASSWORDEMAIL = PROPERTIES.getProperty("EMAIL_PASSWORD");
	private final static String IDEMAIL = PROPERTIES.getProperty("EMAIL_USER");
	private final static String[] TO = PROPERTIES.getProperty("EMAIL_TO").split(",");
	private final static String EMAILHOST = PROPERTIES.getProperty("EMAIL_HOST");
	private final static String FROMEMAIL = PROPERTIES.getProperty("EMAIL_FROM");
	public final static String DOXI_URL = PROPERTIES.getProperty("DOXI_URL");
	private final static String DB_PASSWORD = PROPERTIES.getProperty("DB_PASSWORD");
	private final static String DB_USER = PROPERTIES.getProperty("DB_USER");
	// public final static String TIME = PROPERTIES.getProperty("TIME");

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
		HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // Öffnen der URL
		String auth = USERNAME + ":" + PASSWORD;
		String authb64 = Base64.getEncoder().encodeToString(auth.getBytes());// Encoding in 64er System
		connection.setRequestProperty("Authorization", "Basic " + authb64);// Übergabe der Daten
		String line = null;
		InputStream input = connection.getInputStream();// Abfangen der Antwort
		BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		ArrayList<String> dois = new ArrayList();
		while ((line = reader.readLine()) != null) {
			dois.add(line);
		}
		ArrayList<String> responseCodes = new ArrayList();
		StringBuilder emailMessageBuilder = new StringBuilder();
		for (String doi : dois)/* Für jede DOI ein Durchlauf */ {
			URL url1 = new URL("https://doi.org/" + doi);
			HttpURLConnection connection1 = (HttpURLConnection) url1.openConnection();// HTTP Anfrage
			connection1.setRequestMethod("GET");
			connection1.connect();
			int code1 = connection1.getResponseCode();// HTTP Code abfragen
			responseCodes.add("DOI: " + doi + " | Response: " + code1 + "\n");// Definition der Variable responseCodes
			writeToDatabase(doi, code1);
			if (code1 != 200)/* Fehlerhafte ResponseCodes in Email */ {
				emailMessageBuilder.append("DOI not resolved:" + doi + "\t" + code1 + "\n");

			}

		}

		EmailSender.sendMail(EMAILHOST, IDEMAIL, FROMEMAIL, PASSWORDEMAIL, "Automatische Rückmeldung DOI-Checker",
				emailMessageBuilder.toString(), TO);
		// System.out.println(responseCodes);
		// System.out.println("FINISHED");

	}

	public void writeToDatabase(String doi, int responseCode) throws Exception {
		// Definierung der Methode um Daten in der postgreSQL Datenbank zu speichern
		Connection con = DriverManager.getConnection(dbUrl, DB_USER, DB_PASSWORD);

		String insertTableSQL = "INSERT INTO responseall" + "(Doi, ResponseCode) VALUES" + "(?,?)";
		PreparedStatement preparedStatement = con.prepareStatement(insertTableSQL);
		preparedStatement.setString(1, doi);// Nummer als Platzhalter
		preparedStatement.setInt(2, responseCode);
		// execute insert SQL stetement
		preparedStatement.executeUpdate();

		con.close();
	}

	public List<DoiResponse> getHistorie(String doi) throws SQLException {
		// Methode History abfragen
		List<DoiResponse> doiresplist = new ArrayList();
		Connection con = DriverManager.getConnection(dbUrl, DB_USER, DB_PASSWORD);
		String query = "SELECT * FROM responseall WHERE Doi = ?" + "ORDER BY timestamp DESC ";
		PreparedStatement select = con.prepareStatement(query);
		select.setString(1, doi); // Nummer als Platzhalter
		ResultSet rs = select.executeQuery();
		while (rs.next()) {
			String doi1 = rs.getString("Doi");
			int responseCode = rs.getInt("responsecode");
			Timestamp timestamp = rs.getTimestamp("timestamp");
			DoiResponse doiresp = new DoiResponse(doi1, responseCode, timestamp);
			// System.out.println("Alle Einträge zur Doi " + doiresp);
			doiresplist.add(doiresp);
		}
		con.close();
		return doiresplist;
	}

	public List<DoiResponse> getByResponseCode(int responseCode) throws SQLException {
		List<DoiResponse> doirespcodelist = new ArrayList();
		Connection con = DriverManager.getConnection(dbUrl, DB_USER, DB_PASSWORD);
		String query = "SELECT * FROM responseall r1 WHERE timestamp = (SELECT max(timestamp) from responseall r2 WHERE r1.doi=r2.doi) AND responsecode=? ORDER BY doi";
		;
		PreparedStatement select = con.prepareStatement(query);
		select.setInt(1, responseCode);
		ResultSet rs = select.executeQuery();
		while (rs.next()) {
			String doi1 = rs.getString("Doi");
			int responseCode1 = rs.getInt("responsecode");
			Timestamp timestamp = rs.getTimestamp("timestamp");
			DoiResponse doirespcode = new DoiResponse(doi1, responseCode1, timestamp);
			// System.out.println("Alle Einträge zur Doi " + doirespcode);
			doirespcodelist.add(doirespcode);
		}
		con.close();
		return doirespcodelist;
	}

	public static void main(String args[]) throws Exception {
		EmailSender.sendMail(EMAILHOST, IDEMAIL, FROMEMAIL, PASSWORDEMAIL, "Automatische Rückmeldung DOI-Checker",
				"Test Message", TO);

		DoiChecker myDoiChecker = new DoiChecker(DOXI_URL);
		myDoiChecker.check();

		List<DoiResponse> myList = myDoiChecker.getHistorie("");
		System.out.print("FERTIG");

	}

}
