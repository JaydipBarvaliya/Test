@Configuration
public class JmsConfig {

    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory("vm://embedded?broker.persistent=false");
    }

    @Bean
    public JmsPoolConnectionFactory pooledConnectionFactory(ConnectionFactory connectionFactory) {

        JmsPoolConnectionFactory pool = new JmsPoolConnectionFactory();
        pool.setConnectionFactory(connectionFactory);
        pool.setMaxConnections(5);

        return pool;
    }
}