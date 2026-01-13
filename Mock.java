@Configuration
public class WebClientConfig {

    @Bean
    WebClient fileNetWebClient(
            @Value("${filenet.batchdoc.url}") String baseUrl) {

        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}