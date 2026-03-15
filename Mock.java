@Bean
public ConnectionFactory mqConnectionFactory() {
    MQConnectionFactory factory = new MQConnectionFactory();
    factory.setHostName("localhost");
    factory.setPort(1414);
    factory.setQueueManager("QM1");
    factory.setChannel("DEV.APP.SVRCONN");
    return factory;
}



@Bean
public JmsPoolConnectionFactory pooledConnectionFactory(ConnectionFactory mqConnectionFactory) {

    JmsPoolConnectionFactory pool = new JmsPoolConnectionFactory();
    pool.setConnectionFactory(mqConnectionFactory);
    pool.setMaxConnections(5);

    return pool;
}