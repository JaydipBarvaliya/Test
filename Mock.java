<dependencyManagement>
    <dependencies>
        <!-- Ensure Spring Boot manages its versions properly -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>3.5.0</version> <!-- Match your Spring Boot version -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>