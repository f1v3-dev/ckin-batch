package store.ckin.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.item.ParseException;

/**
 * 예외처리를 위한 로그를 기록하는 리스너 입니다.
 *
 * @author : 이가은
 * @version : 2024. 02. 29
 */
@Slf4j
public class BatchListener {

    /**
     * Step 시작 전에 실행할 step의 이름을 남깁니다.
     */
    @BeforeStep
    public void beforeStep(StepExecution execution) {
        log.info("start : {}", execution.getStepName());
    }

    /**
     * Step 시작 후에 실행된 step의 이름을 남깁니다.
     */
    @AfterStep
    public void afterStep(StepExecution execution) {
        log.info("end : {}", execution.getStepName());
    }

    /**
     * Step 실행중에 발생한 에러를 남깁니다.
     */
    @OnReadError
    public void onReadError(Exception e) {
        if (e instanceof ParseException) {
            log.error("ParseException 발생!!");
        } else {
            log.error(e.toString());
        }
    }
}
