@Override
public Optional<Integer> calcSalary(String workerId, int start, int end) {

    Worker w = workers.get(workerId);
    if (w == null) return Optional.empty();

    int total = 0;

    for (Session s : w.sessions) {

        int overlapStart = Math.max(start, s.start);
        int overlapEnd = Math.min(end, s.end);

        if (overlapStart < overlapEnd) {
            total += (overlapEnd - overlapStart) * s.compensation;
        }
    }

    return Optional.of(total);
}