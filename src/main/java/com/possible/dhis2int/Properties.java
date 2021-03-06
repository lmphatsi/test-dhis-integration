package com.possible.dhis2int;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Properties {

	@Value("${bahmni.login.url}")
	public String bahmniLoginUrl;

	@Value("${openmrs.root.url}")
	public String openmrsRootUrl;

	@Value("${reports.json}")
	public String reportsJson;

	@Value("${reports.url}")
	public String reportsUrl;

	@Value("${dhis.config.directory}")
	public String dhisConfigDirectory;

	@Value("${dhis.url}")
	public String dhisUrl;

	@Value("${dia.root.url}")
	public String diaRootUrl;

	@Value("${openmrs.db.url}")
	public String openmrsDBUrl;

	@Value("${openelis.db.url}")
	public String openelisDBUrl;

	@Value("${dhis.password}")
	public String dhisPassword;

	@Value("${dhis.user}")
	public String dhisUser;

	@Value("${daemon.password}")
	public String openmrsDaemonPassword;

	@Value("${daemon.user}")
	public String openmrsDaemonUser;

	@Value("${log4j.config.file}")
	public String log4jConfigFile;

	@Value("${submission.audit.folder}")
	public String submissionAuditFolder;

}
