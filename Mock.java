<appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">

    <filter class="ch.qos.logback.core.filter.EvaluatorFilter">
        <evaluator>
            <expression>
                logger.equals("org.messaginghub.pooled.jms.JmsPoolSession")
                && message.contains("Ignoring exception while closing JMS Session")
            </expression>
        </evaluator>
        <OnMatch>DENY</OnMatch>
        <OnMismatch>NEUTRAL</OnMismatch>
    </filter>

    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger - %msg%n</pattern>
    </encoder>

</appender>