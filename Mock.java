Yes. I’ll show you the minimal structure so you can reproduce the warning cleanly. Nothing fancy. Just 3 pieces:

1️⃣ enable JMS
2️⃣ listener class
3️⃣ pooled connection bean

⸻

1️⃣ Enable JMS in Your Spring Boot App

In your main Spring Boot application class (or any config class), add:

@SpringBootApplication
@EnableJms
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@EnableJms tells Spring to activate JMS listeners.

⸻

2️⃣ Create a Listener Class

Create a simple component class.

Example:

@Component
public class TestListener {

    @JmsListener(destination = "TEST.QUEUE")
    public void receive(String message) {

        System.out.println("Received: " + message);

        // Force failure so session rollback happens
        throw new RuntimeException("Simulating JMS failure");
    }
}

Where to put it?

Anywhere under your Spring Boot base package, e.g.:

com.example.demo.jms.TestListener

Spring will automatically detect it.

⸻

3️⃣ Add Pooled JMS Connection Factory

Create a configuration class.

Example:

@Configuration
public class JmsConfig {

    @Bean
    public JmsPoolConnectionFactory pooledConnectionFactory(ConnectionFactory connectionFactory) {
        JmsPoolConnectionFactory pool = new JmsPoolConnectionFactory();
        pool.setConnectionFactory(connectionFactory);
        pool.setMaxConnections(1);
        pool.setMaxSessionsPerConnection(1);
        return pool;
    }
}

Yes — you can put this into any configuration class.

Typical location:

com.example.demo.config.JmsConfig


⸻

4️⃣ Add Dependencies (important)

Minimal ones:

<dependency>
    <groupId>org.messaginghub</groupId>
    <artifactId>pooled-jms</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>

pooled-jms is the library that generates:

JmsPoolSession.cleanupSession


⸻

5️⃣ Trigger the Warning

Now do one of these:

easiest

Send a message to the queue.

Your listener throws exception → session rollback → pool cleanup.

Eventually you’ll see:

WARN JmsPoolSession.cleanupSession
Caught exception trying close() when putting session back into the pool
MQBACK failed


⸻

6️⃣ Now Test Your Filter

Add your filter to logback-spring.xml.

Run again and check:

Before filter	After filter
WARN appears	WARN suppressed


⸻

7️⃣ Even Simpler (If You Only Want to Test the Filter)

You don’t even need JMS.

Just log the same message:

logger.warn("Caught exception trying close() when putting session back into the pool, will invalidate.");

If your filter works, that log will disappear.

⸻

Quick Architecture

SpringBootApplication
        │
        │ @EnableJms
        │
        ├── TestListener (@JmsListener)
        │
        └── JmsConfig (JmsPoolConnectionFactory bean)


⸻

If you want, I can also show you the fastest 15-line Spring Boot project that reproduces this warning in under 2 minutes. It’s a very good testbed for JMS logging issues.