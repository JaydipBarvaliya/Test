@Configuration
public class JmsConfig {

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        return new ActiveMQConnectionFactory("vm://embedded?broker.persistent=false");
    }

    @Bean
    public ConnectionFactory connectionFactory(ActiveMQConnectionFactory activeMQConnectionFactory) {

        JmsPoolConnectionFactory pool = new JmsPoolConnectionFactory();
        pool.setConnectionFactory(activeMQConnectionFactory);
        pool.setMaxConnections(5);

        return pool;
    }
}