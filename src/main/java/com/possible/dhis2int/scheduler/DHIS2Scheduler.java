package com.possible.dhis2int.scheduler;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.possible.dhis2int.Properties;
import com.possible.dhis2int.openmrs.OpenMRSAuthenticator;
import com.possible.dhis2int.web.RestTemplateFactory;

import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
public class DHIS2Scheduler {

	private static final Logger logger = LoggerFactory.getLogger(DHIS2Scheduler.class);

	private OpenMRSAuthenticator authenticator;

	private final Properties properties;

	@Autowired
	DHIS2Scheduler(Properties properties, OpenMRSAuthenticator authenticator) {
		this.properties = properties;
		this.authenticator = authenticator;
	}

	private void submitToDHIS(String programmeName, Integer year, Integer month) {
		System.out.println("Hello World!");

	}

	@Scheduled(fixedDelay = 30000)
	public void processSchedules() {
		// read from table
		// for each row, is this report due
		// if due => call submitToDHIS(x,y,z)
		logger.info("Executing schedule at :" + new Date().toString());
		String dhisIntegrationUrl = "http://localhost/dhis-integration";
		String endpointUrl = "/submit-to-dhis";
		Integer month = 6;
		Integer year = 2020;
		String reportName = "TESTS-01 DHIS Integration App Sync Test";
		Boolean isFamily = false;
		Boolean isImam = false;

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", reportName);
		jsonObject.put("year", year);
		jsonObject.put("month", month);
		jsonObject.put("isFamily", isFamily);
		jsonObject.put("isImam", isImam);

		String username = "superman";
		String password = "P@$$w0rd!";
		String toEncode = username + ":" + password;
		String encodedAuth = Base64.getEncoder().encodeToString(toEncode.getBytes());
		String openmrsLoginEndpoint = "http://localhost/openmrs/ws/rest/v1/session";

		String sessionId = "";

		// Get openmrs jsessionid
		try {
			HttpHeaders authHeaders = new HttpHeaders();
			authHeaders.add("Authorization", encodedAuth);
			ResponseEntity<String> responseEntity1 = new RestTemplate().exchange(openmrsLoginEndpoint,
					HttpMethod.GET, new HttpEntity<String>(authHeaders), String.class);
			logger.info("Openmrs get session response: " + responseEntity1.toString());
			logger.info("Response headers: " + responseEntity1.getHeaders().toString());
			sessionId = new JSONObject(new JSONTokener(responseEntity1.getBody())).getString("sessionId");
			logger.info("session id: " + sessionId);
		} catch (HttpClientErrorException exception) {
			logger.warn("Could not authenticate with OpenMRS.", exception.getStatusCode());
		}

		HttpHeaders headers = new HttpHeaders();
		headers.add("reporting_session", sessionId);
		headers.add("reporting_session", sessionId);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);

		try {
			ResponseEntity<String> responseEntity = new RestTemplate().exchange(dhisIntegrationUrl + endpointUrl,
					HttpMethod.GET, entity, String.class);
			logger.info("responseEntity: " + responseEntity.toString());
		} catch (HttpClientErrorException exception) {
			logger.warn("API call failed.", exception.getStatusCode());
		}
	}
}
