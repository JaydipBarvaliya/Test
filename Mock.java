<build>
  <plugins>

    <!-- ✅ 1. Checkstyle: Enforce Java style and naming conventions -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-checkstyle-plugin</artifactId>
      <version>3.3.1</version>
      <executions>
        <execution>
          <id>validate</id>
          <phase>validate</phase>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <configLocation>google_checks.xml</configLocation>
        <encoding>UTF-8</encoding>
        <consoleOutput>true</consoleOutput>
        <failsOnError>true</failsOnError>
        <linkXRef>false</linkXRef>
      </configuration>
    </plugin>

    <!-- ✅ 2. PMD: Detect unused imports, variables, and bad practices -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-pmd-plugin</artifactId>
      <version>3.21.2</version>
      <executions>
        <execution>
          <id>pmd-check</id>
          <phase>verify</phase>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <printFailingErrors>true</printFailingErrors>
        <failOnViolation>true</failOnViolation>
        <targetJdk>17</targetJdk>
      </configuration>
    </plugin>

    <!-- ✅ 3. Spotless: Auto-format code and remove unused imports -->
    <plugin>
      <groupId>com.diffplug.spotless</groupId>
      <artifactId>spotless-maven-plugin</artifactId>
      <version>2.43.0</version>
      <executions>
        <execution>
          <id>spotless-check</id>
          <phase>verify</phase>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
        <execution>
          <id>spotless-apply</id>
          <phase>none</phase>
          <goals>
            <goal>apply</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <java>
          <googleJavaFormat />
          <removeUnusedImports />
          <importOrder />
        </java>
      </configuration>
    </plugin>

    <!-- ✅ 4. SpotBugs: Static analysis to catch real code defects -->
    <plugin>
      <groupId>com.github.spotbugs</groupId>
      <artifactId>spotbugs-maven-plugin</artifactId>
      <version>4.8.6.2</version>
      <executions>
        <execution>
          <id>spotbugs-check</id>
          <phase>verify</phase>
          <goals>
            <goal>check</goal>
          </goals>
        </execution>
      </executions>
      <configuration>
        <effort>Max</effort>
        <threshold>Low</threshold>
        <failOnError>true</failOnError>
        <xmlOutput>true</xmlOutput>
        <xmlOutputDirectory>${project.build.directory}/spotbugs</xmlOutputDirectory>
      </configuration>
    </plugin>

  </plugins>
</build>