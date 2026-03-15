Good idea. If you want to simulate the exact warning locally and test filters, keep it simple. You only need JMS + pooled-JMS + IBM MQ client (or any JMS broker) and then force a session rollback/connection issue.

Below is the minimal setup.

⸻

1️⃣ Dependencies (Maven)

Add these:

<dependency>
    <groupId>org.messaginghub</groupId>
    <artifactId>pooled-jms</artifactId>
    <version>1.2.0</version>
</dependency>

<dependency>
    <groupId>com.ibm.mq</groupId>
    <artifactId>mq-jms-spring-boot-starter</artifactId>
    <version>2.7.2</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>

Key one that generates the warning:

pooled-jms


⸻

2️⃣ Enable JMS Listener

Create a listener that uses pooled sessions.

@JmsListener(destination = "TEST.QUEUE")
public void receive(String msg) {
    System.out.println("Received: " + msg);

    throw new RuntimeException("Simulating failure");
}

This forces session rollback.

⸻

3️⃣ Configure Pooled JMS

Example config:

@Bean
public JmsPoolConnectionFactory pooledConnectionFactory(ConnectionFactory cf) {
    JmsPoolConnectionFactory pool = new JmsPoolConnectionFactory();
    pool.setConnectionFactory(cf);
    pool.setMaxConnections(1);
    pool.setMaxSessionsPerConnection(1);
    return pool;
}

Using small pool size makes the problem appear faster.

⸻

4️⃣ Trigger the Warning

Now force a pool cleanup issue.

Simple ways:

Option A (easiest)

Kill the broker while the app is running.

Result:

WARN JmsPoolSession.cleanupSession
MQBACK failed


⸻

Option B

Throw exception in listener (as above) repeatedly.

Spring will:

rollback session
return session to pool
pool invalidates session

This often triggers:

Caught exception trying close() when putting session back into the pool


⸻

Option C (fastest simulation)

Stop network access to the MQ port.

Example:

iptables -A OUTPUT -p tcp --dport 1414 -j DROP


⸻

5️⃣ Now Test Your Filter

Add filter to logback-spring.xml.

Example:

<filter class="ch.qos.logback.core.filter.EvaluatorFilter">
    <evaluator>
        <expression>
            return message.contains("when putting session back into the pool");
        </expression>
    </evaluator>
    <OnMismatch>NEUTRAL</OnMismatch>
    <OnMatch>DENY</OnMatch>
</filter>

Restart app and verify:

Before filter	After filter
WARN printed	WARN suppressed


⸻

6️⃣ Even Faster Way (Without MQ)

If you just want to test filter, you can log the same message manually.

logger.warn("Caught exception trying close() when putting session back into the pool, will invalidate.");

That’s enough to validate filter logic.

⸻

Quick Summary

To reproduce Michael’s warning:

1️⃣ Add pooled-jms
2️⃣ Create JMS listener
3️⃣ Throw exception or kill broker
4️⃣ Session cleanup warning appears
5️⃣ Test Logback filter

⸻

If you want, I can also show you a much easier trick engineers use to simulate this warning in under 30 seconds without MQ, ActiveMQ, or JMS at all. It’s very useful when testing log filters.