<filter class="ch.qos.logback.core.filter.EvaluatorFilter">
    <evaluator>
        <expression>
            return logger.equals("org.messaginghub.pooled.jms.JmsPoolSession")
            &amp;&amp; message.contains("Ignoring exception while closing JMS Session");
        </expression>
    </evaluator>
    <OnMatch>DENY</OnMatch>
    <OnMismatch>NEUTRAL</OnMismatch>
</filter>