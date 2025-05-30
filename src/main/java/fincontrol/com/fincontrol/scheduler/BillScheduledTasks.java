package fincontrol.com.fincontrol.scheduler;

import fincontrol.com.fincontrol.service.BillService; // Changed import
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BillScheduledTasks { // Renamed class

    private static final Logger logger = LoggerFactory.getLogger(BillScheduledTasks.class);

    private final BillService billService; // Changed service type

    public BillScheduledTasks(BillService billService) { // Changed constructor parameter
        this.billService = billService;
    }

    // Runs every day at 1:00 AM to mark bills as overdue
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkAndMarkOverdueBills() {
        logger.info("Executing scheduled task: Check and Update Overdue Bills");
        try {
            billService.processOverdueBillsJob();
        } catch (Exception e) {
            logger.error("Error executing overdue bills job: ", e);
        }
    }

    // Runs every day at 2:00 AM to process automatic payments
    @Scheduled(cron = "0 0 2 * * ?")
    public void processAutomaticPayments() {
        logger.info("Executing scheduled task: Process Automatic Payments");
        try {
            billService.processAutomaticPaymentsJob();
        } catch (Exception e) {
            logger.error("Error executing automatic payments job: ", e);
        }
    }
}
