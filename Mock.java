@Bean
public ConnectionFactory connectionFactory() {
    return new ActiveMQConnectionFactory("vm://embedded?broker.persistent=false");
}

