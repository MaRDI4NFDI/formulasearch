package org.citeplag.components;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is a collection of events which are created on schedule.
 * To make this run register this scheduler as bean in ApplicationStart.java class and declare EnableScheduling.
 * @author Johannes Stegmüller
 */
public class Scheduler {
    private static final Logger LOG = LogManager.getLogger(Scheduler.class.getName());

    @Scheduled(cron = "${server.cron_update_formulae}")
    /**
     * Exports the current state in BaseX data to harvest-file xml directory once a day.
     */
    public void runDailyXMLExport() {
        System.out.println("test");
    }
}
