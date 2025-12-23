pm.test("Fetch project name and normalized pom path", function () {
    const jsonData = pm.response.json();

    const projectName = jsonData[0].name;
    const rawPomPath = jsonData[0].pomPath;

    // Replace backslashes with forward slashes
    const normalizedPomPath = rawPomPath.replace(/\\/g, "/");

    pm.environment.set("project-name", projectName);
    pm.environment.set("pom-path", normalizedPomPath);
});