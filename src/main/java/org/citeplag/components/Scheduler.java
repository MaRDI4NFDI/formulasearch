package org.citeplag.components;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.citeplag.basex.Client;
import org.citeplag.beans.BaseXGenericResponse;
import org.citeplag.config.BaseXConfig;
import org.citeplag.controller.BaseXController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * This is a collection of events which are created on schedule.
 * To make this run register this scheduler as bean in ApplicationStart.java class and declare EnableScheduling.
 * @author Johannes Stegmüller
 */
public class Scheduler {
    private static final Logger LOG = LogManager.getLogger(Scheduler.class.getName());

    @Autowired
    private BaseXConfig baseXConfig;

    @Autowired
    private BaseXController baseXController;

    @Scheduled(cron = "${server.cron_update_formulae}")
    /**
     * Exports the current state in BaseX data to harvest-file xml directory once a day.
     */
    public void runDailyXMLExport() {
        // Starting Base-X.
        if (!baseXController.startServerIfNecessary()) {
            LOG.warn("Return null for request, because BaseX server is not running.");
        }
        LOG.info("Running the daily xml export of basex for formulaearch");
        BaseXGenericResponse response = Client.doExport(baseXConfig.getHarvestPath());
        if (response.getCode() != 0) {
            LOG.error("Error during daily XML export " + response.getMessage());
        } else {
            LOG.info("Running the daily xml export to " + baseXConfig.getHarvestPath() + " was successful");
        }
    }
}
