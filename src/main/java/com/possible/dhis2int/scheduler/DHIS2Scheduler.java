package com.possible.dhis2int.scheduler;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.possible.dhis2int.web.RestTemplateFactory;

import java.util.Date;

@Component
public class DHIS2Scheduler {

	Logger logger = LoggerFactory.getLogger(DHIS2Scheduler.class);

	private RestTemplateFactory restTemplateFactory;

	DHIS2Scheduler(RestTemplateFactory restTemplateFactory) {
		this.restTemplateFactory = restTemplateFactory;
	}

	private void submitToDHIS(String programmeName, Integer year, Integer month) {
		System.out.println("Hello World!");

	}

	@Scheduled(fixedDelay = 5000)
	public void processSchedules() {
		// read from table
		// for each row, is this report due
		// if due => call submitToDHIS(x,y,z)
		logger.info("Executing schedule at :" + new Date().toString());
		String dhisIntegrationUrl = "http://localhost:8040/dhis-integration";
		String endpointUrl = "/submit-to-dhis";
		String month = "06";
		String year = "2021";
		String reportName = "TESTS-01 DHIS Integration App Sync Test";

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("name", reportName);
		jsonObject.put("year", year);
		jsonObject.put("month", month);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), headers);
		ResponseEntity<String> responseEntity = restTemplateFactory.getRestTemplate().exchange(
				dhisIntegrationUrl + endpointUrl, HttpMethod.POST, entity,
				String.class);
		logger.info("responseEntity: " + responseEntity.toString());
		logger.info("status code: " + responseEntity.getStatusCode());
	}
}
