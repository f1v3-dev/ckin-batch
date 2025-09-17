package store.ckin.batch.coupon.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.test.context.SpringBootTest;
import store.ckin.batch.coupon.config.CouponBatchConfig;
import store.ckin.batch.scheduler.BirthScheduler;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * BirthSchedulerTest
 *
 * @author : 이가은
 * @version : 2024. 03. 13
 */
@SpringBootTest
class BirthSchedulerTest {

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private CouponBatchConfig couponBatchConfig;

    @Mock
    private Job giveBirthCouponJob;

    @InjectMocks
    private BirthScheduler birthScheduler;

    @Test
    @DisplayName("스케줄러 실행 테스트 : 성공")
    void testBirthJob() throws Exception {
        when(couponBatchConfig.giveBirthCoupon()).thenReturn(giveBirthCouponJob);
        when(jobLauncher.run(Mockito.any(Job.class), Mockito.any(JobParameters.class)))
                .thenReturn(new JobExecution(1L));

        birthScheduler.birthJob();

        verify(jobLauncher, Mockito.times(1)).run(Mockito.eq(giveBirthCouponJob), Mockito.any(JobParameters.class));
    }

    @Test
    @DisplayName("스케줄러 실행 테스트 : 실패")
    void birthJobException() throws Exception {

        when(couponBatchConfig.giveBirthCoupon()).thenReturn(giveBirthCouponJob);
        when(jobLauncher.run(any(Job.class), any(JobParameters.class)))
                .thenThrow(new RuntimeException("CKIN Exception"));

        assertThrows(Exception.class, () -> birthScheduler.birthJob());
        verify(couponBatchConfig, times(1)).giveBirthCoupon();
        verify(jobLauncher, times(1)).run(eq(giveBirthCouponJob), any(JobParameters.class));
    }
}
