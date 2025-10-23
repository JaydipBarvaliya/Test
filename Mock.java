List<Long> times = new ArrayList<>();
for (int i = 0; i < 100; i++) {
    long start = System.nanoTime();
    restTemplate.exchange(uri, method, requestEntity, responseType);
    long end = System.nanoTime();
    times.add((end - start) / 1_000_000);
}

double avg = times.stream().mapToLong(Long::longValue).average().orElse(0);
System.out.println("Average ESL latency: " + avg + " ms");