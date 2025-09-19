package store.ckin.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import store.ckin.batch.listener.BatchListener;

/**
 * BatchConfig
 *
 * @author : 이가은
 * @version : 2024. 02. 29
 */
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    @Bean
    public BatchListener batchListener() {
        return new BatchListener();
    }
}
