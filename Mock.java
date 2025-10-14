<rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
    <fileNamePattern>${LOG_PATH}/aesig_api.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
    <maxFileSize>${FILE_SIZE}</maxFileSize>
    <maxHistory>${MAX_FILE_HISTORY}</maxHistory>
    <totalSizeCap>${TOTAL_SIZE_CAP}</totalSizeCap>
    <cleanHistoryOnStart>true</cleanHistoryOnStart>
</rollingPolicy>