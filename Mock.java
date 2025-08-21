pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);

    var jsonData = pm.response.json();

    // Check if eventId exists before using it
    if (jsonData && jsonData.eventId) {
        console.log("Event ID: " + jsonData.eventId);
        pm.environment.set("eventId", jsonData.eventId);
    } else {
        console.log("eventId is undefined, not setting environment variable");
    }
});