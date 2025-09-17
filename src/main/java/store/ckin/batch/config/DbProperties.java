package store.ckin.batch.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DB 설정 정보를 담은 클래스입니다.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "ckin.mysql")
public class DbProperties {
    private String driver;
    private String urlDev;
    private String urlBatch;
    private String userName;
    private String password;
    private int maxIdle;
    private int minIdle;
    private int maxWaitMillis;
}
