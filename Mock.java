List<Long> times = new ArrayList<>();
for (int i = 0; i < 50; i++) {
    long start = System.nanoTime();
    responseEntity = restTemplateObj.exchange(uri, method, requestEntity, responseContentClassType);
    long end = System.nanoTime();
    times.add((end - start) / 1_000_000);
    Thread.sleep(100); // small pause to avoid rate limiting (optional)
}

// remove top and bottom 10% outliers
Collections.sort(times);
int trim = (int) (times.size() * 0.1);
List<Long> trimmed = times.subList(trim, times.size() - trim);

double avg = trimmed.stream().mapToLong(Long::longValue).average().orElse(0);
System.out.println("Average ESL latency (10% trimmed): " + avg + " ms");