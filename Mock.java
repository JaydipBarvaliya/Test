import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LogbackTestRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(LogbackTestRunner.class);

    @Override
    public void run(String... args) throws Exception {
        for (int i = 0; i < 5000; i++) {
            log.info("Rolling test line number: {}", i);
            Thread.sleep(10); // optional, just to slow it down
        }
    }
}