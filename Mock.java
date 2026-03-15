Good question. To trigger the listener you must send a message to the queue. The easiest way in Spring Boot is to use JmsTemplate.

Below is the simplest way to do it.

⸻

1️⃣ Add a Small Sender Component

Create a class like this:

@Component
public class TestSender {

    private final JmsTemplate jmsTemplate;

    public TestSender(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage() {
        jmsTemplate.convertAndSend("TEST.QUEUE", "hello message");
    }
}

This sends a message to:

TEST.QUEUE

Which will trigger your:

@JmsListener(destination = "TEST.QUEUE")

listener.

⸻

2️⃣ Trigger It Automatically on Startup (Simplest)

Add this to your main app:

@Bean
CommandLineRunner run(TestSender sender) {
    return args -> {
        sender.sendMessage();
    };
}

Now when the app starts:

1️⃣ Spring starts
2️⃣ message is sent to queue
3️⃣ @JmsListener receives it
4️⃣ exception occurs
5️⃣ session cleanup happens
6️⃣ you get the WARN log

⸻

3️⃣ Your Listener (Reminder)

@Component
public class TestListener {

    @JmsListener(destination = "TEST.QUEUE")
    public void receive(String message) {

        System.out.println("Received: " + message);

        throw new RuntimeException("Simulating JMS failure");
    }
}

This forces rollback, which causes the pool cleanup warning.

⸻

4️⃣ What Happens Internally

Sender → Queue → Listener
                    │
                    │ exception thrown
                    ↓
Spring rollback
                    ↓
Session returned to pool
                    ↓
JmsPoolSession.cleanupSession
                    ↓
WARN log generated


⸻

5️⃣ If You Don’t Have a Real MQ

Use embedded ActiveMQ.

Add dependency:

<dependency>
    <groupId>org.apache.activemq</groupId>
    <artifactId>activemq-broker</artifactId>
</dependency>

Spring Boot will start a local broker automatically.

No MQ setup needed.

⸻

6️⃣ Expected Log

Eventually you’ll see something like:

WARN JmsPoolSession.cleanupSession
Caught exception trying close() when putting session back into the pool

Now you can test your Logback filter.

⸻

Quick Summary

To reproduce the warning:

1️⃣ Enable JMS
2️⃣ Add @JmsListener
3️⃣ Throw exception inside listener
4️⃣ Send message using JmsTemplate
5️⃣ Session rollback → pool cleanup → warning

⸻

If you want, I can also show you a much easier trick (10 lines only) to simulate this without JMS, MQ, ActiveMQ, or any broker just to test the Logback filter. It’s much faster for debugging.