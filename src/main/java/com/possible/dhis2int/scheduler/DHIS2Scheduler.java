package com.possible.dhis2int.scheduler;

import org.json.JSONObject;
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

import com.possible.dhis2int.openmrs.OpenMRSAuthenticator;
import com.possible.dhis2int.web.RestTemplateFactory;

import java.util.Date;

@Service
public class DHIS2Scheduler {

	Logger logger = LoggerFactory.getLogger(DHIS2Scheduler.class);

	private RestTemplateFactory restTemplateFactory;

	private OpenMRSAuthenticator authenticator;

	@Autowired
	DHIS2Scheduler(RestTemplateFactory restTemplateFactory, OpenMRSAuthenticator authenticator) {
		this.restTemplateFactory = restTemplateFactory;
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

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);
		logger.info("URL: " + dhisIntegrationUrl + endpointUrl);
		logger.info("entity: " + entity.toString());
		// logger.info("responseEntity: " + responseEntity.toString());
		// logger.info("status code: " + responseEntity.getStatusCode());
		try {
			ResponseEntity<String> responseEntity = new RestTemplate().exchange(dhisIntegrationUrl + endpointUrl,
					HttpMethod.GET, entity, String.class);
			logger.info("responseEntity: " + responseEntity.toString());
		} catch (HttpClientErrorException exception) {
			logger.warn("API call failed.", exception.getStatusCode());
		}
	}
}
