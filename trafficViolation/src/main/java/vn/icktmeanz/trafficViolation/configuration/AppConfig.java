package vn.icktmeanz.trafficViolation.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        // Khởi tạo HttpClient tiêu chuẩn của Java với các cấu hình Timeout
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)) // Timeout kết nối (5 giây)
                .build();

        // Đưa HttpClient vào Factory mới của Spring 6
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);

        // Thiết lập Read Timeout chờ AI xử lý (60 giây)
        factory.setReadTimeout(60000);

        return new RestTemplate(factory);
    }
}
