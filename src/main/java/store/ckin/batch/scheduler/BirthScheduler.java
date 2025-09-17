package store.ckin.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store.ckin.batch.coupon.config.CouponBatchConfig;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 매월 1일 생일자에게 생일 쿠폰을 지급하는 스케쥴러입니다.
 *
 * @author : gaeun
 * @version : 2024. 02. 26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BirthScheduler {

    private final JobLauncher jobLauncher;
    private final CouponBatchConfig couponBatchConfig;
    /**
     * 매월 1일 생일자에게 생일 쿠폰을 지급하는 스케쥴러입니다.
     */
    @Scheduled(cron = "0 0 0 1 * *")
    public void birthJob() {
        Map<String, JobParameter> confMap = new HashMap<>();

        Date date = Calendar.getInstance().getTime();
        confMap.put("time", new JobParameter(date));
        JobParameters jobParameters = new JobParameters(confMap);

        try {
            jobLauncher.run(couponBatchConfig.giveBirthCoupon(), jobParameters);
        } catch (Exception e) {
            log.error("생일 쿠폰 지급 중 에러 발생: {}", e.getMessage());
            throw new RuntimeException();
        }
    }
}
