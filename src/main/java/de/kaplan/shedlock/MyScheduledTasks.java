package de.kaplan.shedlock;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class MyScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(MyScheduledTasks.class);

//    // 2-minute while loop
//    @Scheduled(fixedDelay = 10000) // Runs 10 seconds after the end of each task
//    @SchedulerLock(name = "MyScheduledTask_whileTask", lockAtMostFor = "PT1M30S", lockAtLeastFor = "PT1M")
//    public void whileTask() {
//        logger.debug("While loop started: " + System.currentTimeMillis());
//
//        long startTime = System.currentTimeMillis();
//        boolean oneAndHalfMinuteLogged = false;
//
//        while ((System.currentTimeMillis() - startTime) < 120000) { // 2-minute loop
//            logger.debug("While loop is running...");
//
//            // Log message after 1.5 minutes
//            if (!oneAndHalfMinuteLogged && (System.currentTimeMillis() - startTime) >= 90000) {
//                logger.debug("1.5 minutes have passed.");
//                oneAndHalfMinuteLogged = true; // Mark to avoid repeated logging
//            }
//
//            try {
//                Thread.sleep(1000); // Simulate a 1-second wait
//            } catch (InterruptedException e) {
//                logger.error("While loop interrupted", e);
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        logger.debug("While loop ended: " + System.currentTimeMillis());
//    }

    // 2-minute for-each loop
//    @Scheduled(fixedDelay = 10000) // Runs 10 seconds after the end of each task
//    @SchedulerLock(name = "MyScheduledTask_forEachTask", lockAtMostFor = "PT1M30S", lockAtLeastFor = "PT1M")
//    public void forEachTask() {
//        logger.debug("For-each loop started: " + System.currentTimeMillis());
//
//        int[] items = {1, 2, 3, 4, 5}; // Sample data
//        long startTime = System.currentTimeMillis();
//        boolean oneAndHalfMinuteLogged = false;
//
//        for (int item : items) {
//            // Log message after 1.5 minutes
//            if (!oneAndHalfMinuteLogged && (System.currentTimeMillis() - startTime) >= 90000) {
//                logger.debug("1.5 minutes have passed.");
//                oneAndHalfMinuteLogged = true; // Mark to avoid repeated logging
//            }
//
//            logger.debug("Processing item in for-each loop: " + item);
//            try {
//                Thread.sleep(24000); // 24-second processing time per item (total of 2 minutes)
//            } catch (InterruptedException e) {
//                logger.error("For-each loop interrupted", e);
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        logger.debug("For-each loop ended: " + System.currentTimeMillis());
//    }

//    @Scheduled(fixedRate = 120000) // Her 2 dakikada bir çalışır
//    @SchedulerLock(name = "MyScheduledTask_testTask", lockAtMostFor = "PT1M30S", lockAtLeastFor = "PT1M")
//    public void testTask() {
//        logger.info("Task started: " + System.currentTimeMillis());
//
//        try {
//            // Görev 1 dakika 40 saniye sürecek şekilde simüle edildi
//            Thread.sleep(100000); // 1 dakika 40 saniyelik bekleme süresi (100000 ms)
//        } catch (InterruptedException e) {
//            logger.info("Task interrupted", e);
//            Thread.currentThread().interrupt();
//        }
//
//        logger.info("Task ended: " + System.currentTimeMillis());
//    }


    // Görev her 2 dakikada bir çalışacak
//    @Scheduled(fixedRate = 120000) // 120000 ms = 2 dakika
//    @SchedulerLock(name = "MyScheduledTask_testTask", lockAtMostFor = "PT1M30S", lockAtLeastFor = "PT1M")
//    public void testTask() {
//        logger.info("Task started: " + System.currentTimeMillis());
//
//        try {
//            // Görev 2 dakika sürecek şekilde simüle edildi
//            Thread.sleep(120000); // 120000 ms = 2 dakika bekleme süresi
//        } catch (InterruptedException e) {
//            logger.error("Task interrupted", e);
//            Thread.currentThread().interrupt();
//        }
//
//        logger.info("Task ended: " + System.currentTimeMillis());
//    }
}