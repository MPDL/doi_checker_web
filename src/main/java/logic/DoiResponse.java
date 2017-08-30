package logic;

import java.sql.Timestamp;

public class DoiResponse {

private String doi;
private int responseCode;
private Timestamp timestamp;



public DoiResponse(String doi, int responseCode, Timestamp timestamp) {
	this.doi = doi;
	this.responseCode = responseCode;
	this.timestamp = timestamp;
}

public DoiResponse(String doi1, int responseCode2) {
	// TODO Auto-generated constructor stub
}

public String getDoi() {
	return doi;
}
public void setDoi(String doi) {
	this.doi = doi;
}
public int getResponseCode() {
	return responseCode;
}
public void setResponseCode(int responseCode) {
	this.responseCode = responseCode;
}
public Timestamp getTimestamp() {
	return timestamp;
}
public void setTimestamp(Timestamp timestamp) {
	this.timestamp = timestamp;
}

public String toString()
{
	return doi + " - " + responseCode + " (" + timestamp + ")";
}
	
	
	
	
	
	
	
	
	
}
