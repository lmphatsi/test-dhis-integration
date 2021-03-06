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

	private String buildDiaUrl(String reportName, Integer month, Integer year, String comment) {
		StringBuilder diaRequestUrl = new StringBuilder(properties.diaRootUrl + DIA_SUBMISSION_ENDPOINT);
		diaRequestUrl.append("?name=").append(reportName).append("&month=").append(month).append("&year=").append(year)
				.append("&isImam=false&isFamily=false").append("&comment=").append(comment);
		return diaRequestUrl.toString();
	}

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

	private void logout(String openmrsUrl) {
		String authToken = getAuthToken(properties.openmrsDaemonUser, properties.openmrsDaemonPassword);

		HttpHeaders openmrsAuthHeaders = new HttpHeaders();
		openmrsAuthHeaders.add("Authorization", "BASIC " + authToken);

		ResponseEntity<String> responseEntity = new RestTemplate().exchange(openmrsUrl,
				HttpMethod.DELETE, new HttpEntity<String>(openmrsAuthHeaders), String.class);
		logger.info("Openmrs delete session response: " + responseEntity.toString());
	}

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

	@Scheduled(fixedDelay = 30000)
	public void processSchedules() {
		// read from table
		// for each row, is this report due
		// if due => call submitToDHIS(x,y,z)
		logger.info("Executing schedule at :" + new Date().toString());

		// check if dhis-instance is up

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

		/*
		 * JSONObject jsonObject = new JSONObject();
		 * jsonObject.put("name", reportName);
		 * jsonObject.put("year", year);
		 * jsonObject.put("month", month);
		 * jsonObject.put("isFamily", isFamily);
		 * jsonObject.put("isImam", isImam);
		 */

		// String authToken = getAuthToken(properties.openmrsDaemonUser,
		// properties.openmrsDaemonPassword);

		// String openmrsLoginEndpoint = "http://localhost/openmrs/ws/rest/v1/session";
		// String openmrsWhoAmIEndpoint =
		// "http://localhost/openmrs/ws/rest/v1/bahmnicore/whoami";

		/*
		 * String sessionId = "";
		 * String sessionUser = "";
		 * System.out.println("Auth token: " + authToken);
		 * // Get openmrs jsessionid
		 * try {
		 * HttpHeaders openmrsAuthHeaders = new HttpHeaders();
		 * openmrsAuthHeaders.add("Authorization", "BASIC " + authToken);
		 * ResponseEntity<String> responseEntity1 = new
		 * RestTemplate().exchange(openmrsUrl,
		 * HttpMethod.GET, new HttpEntity<String>(openmrsAuthHeaders), String.class);
		 * logger.info("Openmrs get session response: " + responseEntity1.toString());
		 * logger.info("Response headers: " + responseEntity1.getHeaders().toString());
		 * Boolean authenticated = new JSONObject(new
		 * JSONTokener(responseEntity1.getBody()))
		 * .getBoolean("authenticated");
		 * if (authenticated) {
		 * sessionId = new JSONObject(new
		 * JSONTokener(responseEntity1.getBody())).getString("sessionId");
		 * sessionUser = new JSONObject(new
		 * JSONTokener(responseEntity1.getBody())).getJSONObject("user")
		 * .getString("username");
		 * }
		 * // sessionId = new JSONObject(new
		 * // JSONTokener(responseEntity1.getBody())).getString("sessionId");
		 * // sessionUser = new JSONObject(new
		 * // JSONTokener(responseEntity1.getBody())).getJSONObject("user").getString(
		 * "username");
		 * 
		 * // authHeaders.add("Cookie", "JSESSIONID=" + sessionId);
		 * // responseEntity1 = new RestTemplate().exchange(openmrsWhoAmIEndpoint,
		 * // HttpMethod.GET,new HttpEntity<String>(authHeaders), String.class);
		 * // logger.info("session id: " + sessionId);
		 * // -- logger.info("Response headers: " +
		 * // responseEntity1.getHeaders().toString());
		 * } catch (HttpClientErrorException exception) {
		 * logger.warn("API call to OpenMRS failed.", exception.getStatusCode());
		 * }
		 * 
		 * // System.out.println("Session ID " + sessionId);
		 * // HttpHeaders diaAuthHeaders = new HttpHeaders();
		 * // headers.add("Cookie", "JSESSIONID=" + sessionId);
		 * // diaAuthHeaders.add("Cookie", "reporting_session=" + sessionId);
		 * // diaAuthHeaders.add("Cookie", "bahmni.user=" + sessionUser);
		 * // headers.setContentType(MediaType.APPLICATION_JSON);
		 * // HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(),
		 * headers);
		 * 
		 * try {
		 * HttpHeaders diaAuthHeaders = new HttpHeaders();
		 * diaAuthHeaders.add("Cookie", "JSESSIONID=" + sessionId);
		 * diaAuthHeaders.add("Cookie", "reporting_session=" + sessionId);
		 * diaAuthHeaders.add("Cookie", "bahmni.user=" + sessionUser);
		 * // HttpEntity<String> entity1 = new HttpEntity<>("body", diaAuthHeaders);
		 * 
		 * ResponseEntity<String> responseEntity = new RestTemplate().exchange(diaUrl,
		 * HttpMethod.GET,
		 * new HttpEntity<String>(diaAuthHeaders),
		 * String.class);
		 * logger.info("responseEntity: " + responseEntity.toString());
		 * 
		 * /*
		 * ResponseEntity<String> responseEntity = new
		 * RestTemplate().exchange(dhisIntegrationUrl + endpointUrl,
		 * HttpMethod.POST, entity, String.class);
		 * logger.info("responseEntity: " + responseEntity.toString());
		 */

		/*
		 * Map<String, Integer> vars = new HashMap<String, Integer>();
		 * vars.put("year", 2016);
		 * vars.put("month", 1);
		 * String result = new RestTemplate().getForObject(urlEndpoint, String.class,
		 * vars);
		 * logger.info("result: " + result);
		 */

		/*
		 * } catch (HttpClientErrorException exception) {
		 * logger.warn("API call to DIA failed.", exception.getStatusCode());
		 * }
		 */
	}
}
