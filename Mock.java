Yes — I do see a likely problem in that filter, and you don’t necessarily need the whole logback.xml yet. The issue is visible in the snippet you shared. Let’s go through it carefully.

⸻

1️⃣ The Main Problem: message.contains(...) Is Too Strict

Your filter currently checks:

return message.contains("when putting session back into the pool, will invalidate. com.ibm.msg.client.jakarta.jms.DetailedJMSException: JMSCMQ0002: The method 'MQBACK' failed.");

But the actual log message looks like this:

Caught exception trying close() when putting session back into the pool, will invalidate.
com.ibm.msg.client.jakarta.jms.DetailedJMSException: JMSCMQ0002: The method 'MQBACK' failed

Important detail:

👉 Logback splits lines and formats them.

So the message variable typically contains only the first line of the log event, not the entire stack trace.

That means your filter probably receives something like:

Caught exception trying close() when putting session back into the pool, will invalidate.

And NOT this part:

JMSCMQ0002: The method 'MQBACK' failed

So your filter string will never match.

⸻

2️⃣ The Correct Way to Match It

Instead of matching the entire message, match a stable substring.

For example:

<expression>
return message.contains("when putting session back into the pool");
</expression>

or even safer:

<expression>
return message.contains("JmsPoolSession.cleanupSession");
</expression>

or:

<expression>
return message.contains("MQBACK failed");
</expression>


⸻

3️⃣ Why Your Current Filter Probably Doesn’t Work

Because it expects this exact string:

when putting session back into the pool, will invalidate. com.ibm.msg.client...

But the actual log output is:

line 1
line 2

And Logback filters only see line 1.

So the filter never matches → log is not suppressed.

⸻

4️⃣ The Fix I Recommend

Change your filter to something simple and stable:

<filter class="ch.qos.logback.core.filter.EvaluatorFilter">
    <evaluator>
        <expression>
            return message.contains("when putting session back into the pool");
        </expression>
    </evaluator>
    <OnMismatch>NEUTRAL</OnMismatch>
    <OnMatch>DENY</OnMatch>
</filter>

This will reliably match the warning.

⸻

5️⃣ Another Small Detail (Good News)

Your filter placement is correct.

It is inside the RollingFile appender, which is where the logs are written.

So structurally it looks fine.

⸻

6️⃣ When I Would Ask for the Full File

If after fixing the substring it still doesn’t work, then I would want to see:
	•	entire logback-spring.xml
	•	where the RollingFile appender is defined
	•	root logger configuration

Because sometimes logs are written to multiple appenders.

⸻

7️⃣ Quick Way to Test It

To confirm the filter works locally:

Temporarily use this:

return message.contains("JmsPoolSession")

If the warning disappears, you know the filter is working and you just need the correct substring.

⸻

8️⃣ My Confidence

Based on what you showed, I’m about 90% sure the issue is the overly long contains() string.

Shortening it will likely fix the suppression.

⸻

If you’d like, I can also show you a very robust Logback filter pattern used in production systems that suppresses JMS pool warnings safely without relying on fragile message text. It’s a much better long-term solution.