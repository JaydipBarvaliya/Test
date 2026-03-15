Reda, Michael

My understanding is that using the logger approach:

<logger name="org.messaginghub.pooled.jms.JmsPoolSession" level="ERROR">
    <appender-ref ref="RollingFile"/>
</logger>

would suppress all WARN logs from that class. That would remove the MQBACK warning, but it would also hide other potentially useful WARN messages from the same component.

The filter approach from the June 2024 commit seems more precise since it targets only the specific message ("when putting session back into the pool / MQBACK failed") without suppressing other WARN logs.

So it might be better to rely on fixing the filter rather than lowering the logger level, if the goal is just to suppress that particular warning.