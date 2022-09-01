package com.possible.dhis2int.scheduler;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

import com.possible.dhis2int.Properties;

import java.nio.charset.Charset;
import java.util.Date;

/*
 * The DHIS2 Scheduler is actually a DHISIntegrator client (same pattern as DHISClient in dhis package)
 */

@Service
public class DHIS2Scheduler {

	private static final Logger logger = LoggerFactory.getLogger(DHIS2Scheduler.class);

	private final Properties properties;

	private final String DIA_SUBMISSION_ENDPOINT = "/dhis-integration/submit-to-dhis";

	private final String OPENMRS_LOGIN_ENDPOINT = "/session";

	@Autowired
	DHIS2Scheduler(Properties properties) {
		this.properties = properties;
	}

	private String getAuthToken(String username, String password) {
		Charset UTF_8 = Charset.forName("UTF-8");
		String authToken = Base64Utils.encodeToString((username + ":" + password).getBytes(UTF_8));
		return authToken;

	}

	/*
	 * This method builds the DIA url (rest api endpoint and parameters)
	 */
	private String buildDiaUrl(String reportName, Integer month, Integer year, String comment) {
		StringBuilder diaRequestUrl = new StringBuilder(properties.diaRootUrl + DIA_SUBMISSION_ENDPOINT);
		diaRequestUrl.append("?name=").append(reportName).append("&month=").append(month).append("&year=").append(year)
				.append("&isImam=false&isFamily=false").append("&comment=").append(comment);
		return diaRequestUrl.toString();
	}

	/*
	 * Method to authenticate/login daemon user against OpenMRS
	 * Return an empty Authentication response (sessionid and session user) if auth
	 * fails
	 * Else it returns a valid sessionid and session username
	 */
	private AuthResponse authenticate(String openmrsUrl) {
		String authToken = getAuthToken(properties.openmrsDaemonUser, properties.openmrsDaemonPassword);

		HttpHeaders openmrsAuthHeaders = new HttpHeaders();
		openmrsAuthHeaders.add("Authorization", "BASIC " + authToken);

		ResponseEntity<String> responseEntity = new RestTemplate().exchange(openmrsUrl,
				HttpMethod.GET, new HttpEntity<String>(openmrsAuthHeaders), String.class);
		logger.info("Openmrs get session response: " + responseEntity.toString());
		logger.info("Response headers: " + responseEntity.getHeaders().toString());
		Boolean authenticated = new JSONObject(new JSONTokener(responseEntity.getBody()))
				.getBoolean("authenticated");
		AuthResponse authResponse = new AuthResponse("", "");
		if (authenticated) {
			authResponse.setSessionId(new JSONObject(new JSONTokener(responseEntity.getBody())).getString("sessionId"));
			authResponse.setSessionUser(new JSONObject(new JSONTokener(responseEntity.getBody())).getJSONObject("user")
					.getString("username"));
			logger.info("Daemon user " + properties.openmrsDaemonUser + " autheticated successfully");
		} else {
			logger.warn("Daemon user " + properties.openmrsDaemonUser
					+ " NOT autheticated. Correct the credentials on the properties file (application.yml)");
		}
		return authResponse;
	}

	/*
	 * Log out deamon user from OpenMRS
	 */
	private void logout(String openmrsUrl) {
		String authToken = getAuthToken(properties.openmrsDaemonUser, properties.openmrsDaemonPassword);

		HttpHeaders openmrsAuthHeaders = new HttpHeaders();
		openmrsAuthHeaders.add("Authorization", "BASIC " + authToken);

		ResponseEntity<String> responseEntity = new RestTemplate().exchange(openmrsUrl,
				HttpMethod.DELETE, new HttpEntity<String>(openmrsAuthHeaders), String.class);
		logger.info("Openmrs delete session response: " + responseEntity.toString());
	}

	/*
	 * This method makes a rest call to DIA which in turn submits to DHIS2 by making
	 * a
	 * rest call to the DHIS2 rest api
	 */
	private ResponseEntity<String> submitToDIA(String diaUrl, AuthResponse authResponse) {
		HttpHeaders diaAuthHeaders = new HttpHeaders();
		diaAuthHeaders.add("Cookie", "JSESSIONID=" + authResponse.getSessionId());
		diaAuthHeaders.add("Cookie", "reporting_session=" + authResponse.getSessionId());
		diaAuthHeaders.add("Cookie", "bahmni.user=" + authResponse.getSessionUser());

		ResponseEntity<String> responseEntity = new RestTemplate().exchange(diaUrl, HttpMethod.GET,
				new HttpEntity<String>(diaAuthHeaders),
				String.class);

		logger.info("Status code: " + responseEntity.getStatusCode() + "when firing query - GET: " + diaUrl);
		return responseEntity;
	}

	private Boolean isDue() {
		return true;
	}

	@Scheduled(fixedDelay = 30000)
	public void processSchedules() {
		// read from table
		// for each row, is this report due
		// if due => call submitToDHIS(x,y,z)
		logger.info("Executing schedule at :" + new Date().toString());

		// get from DB

		Integer month = 6;
		Integer year = 2020;
		String reportName = "TESTS-01 DHIS Integration App Sync Test";
		String comment = "Submitted by daemon";

		String diaUrl = buildDiaUrl(reportName, month, year, comment);
		String openmrsUrl = properties.openmrsRootUrl + OPENMRS_LOGIN_ENDPOINT;
		System.out.println("DIA url: " + diaUrl);

		AuthResponse authResponse = authenticate(openmrsUrl);

		//
		if (authResponse.getSessionId() != "") {
			submitToDIA(diaUrl, authResponse);
		}
		logout(openmrsUrl);
		// logout when done
	}
}
