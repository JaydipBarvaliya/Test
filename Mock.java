@Override
public List<String> topNWorkers(int n, String position) {

    List<WorkerEntry> list = new ArrayList<>();

    for (Map.Entry<String, Worker> entry : workers.entrySet()) {
        Worker w = entry.getValue();

        if (w.position.equals(position)) {
            list.add(new WorkerEntry(entry.getKey(), w.totalTime));
        }
    }

    // sort:
    // 1. totalTime DESC
    // 2. workerId ASC
    Collections.sort(list, (a, b) -> {
        if (b.time != a.time) {
            return b.time - a.time;
        }
        return a.id.compareTo(b.id);
    });

    List<String> result = new ArrayList<>();

    for (int i = 0; i < Math.min(n, list.size()); i++) {
        WorkerEntry e = list.get(i);
        result.add(e.id + "(" + e.time + ")");
    }

    return result;
}