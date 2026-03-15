@Component
public class LogSimulator {

    private static final Logger log =
        LoggerFactory.getLogger("org.messaginghub.pooled.jms.JmsPoolSession");

    @PostConstruct
    public void test() {
        log.warn("Ignoring exception while closing JMS Session");
    }
}