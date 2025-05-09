package com.example.batch.main.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SalesAnalysisJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job bestMenuTaskletJob;
    private final Job marketPurchaseAnalysisJob;
    private final Job topPurchasedInventoryJob;

    @Scheduled(cron = "0 */1 * * * *")  // 5분마다 실행
    public void runAllJobs() {
        runJob(bestMenuTaskletJob, "BestMenuTaskletJob");
        runJob(marketPurchaseAnalysisJob, "MarketPurchaseAnalysisJob");
        runJob(topPurchasedInventoryJob, "TopPurchasedInventoryJob");
    }

    private void runJob(Job job, String jobName) {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())  // 중복 방지용 파라미터
                    .toJobParameters();

            log.info("🕒 {} 시작", jobName);
            jobLauncher.run(job, params);
            log.info("✅ {} 완료", jobName);
        } catch (Exception e) {
            log.error("❌ {} 실패: {}", jobName, e.getMessage(), e);
        }
    }
}