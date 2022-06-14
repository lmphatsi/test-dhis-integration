package com.possible.dhis2int.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.Schedules;

import java.util.Date;

public class DHIS2Scheduler {

    Logger logger = LoggerFactory.getLogger(DHIS2Scheduler.class);

    private void submitToDHIS(String programmeName, Integer year, Integer month){

    }

    @Scheduled(fixedDelay = 5000)
    public void processSchedules(){
        //read from table
        //for each row, is this report due
        //if due => call submitToDHIS(x,y,z)
        logger.info("Scheduler ran at :"+new Date().toString());
    }
}
