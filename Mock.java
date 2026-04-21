package com.codesignal.workhoursregister;

import java.util.*;

class WorkHoursRegisterImpl implements WorkHoursRegister {

    static class Worker {
        String position;
        int compensation;

        boolean inOffice = false;
        Integer lastEntryTime = null;
        int totalTime = 0;

        Worker(String position, int compensation) {
            this.position = position;
            this.compensation = compensation;
        }
    }

    private Map<String, Worker> workers = new HashMap<>();

    public WorkHoursRegisterImpl() {}

    @Override
    public boolean addWorker(String workerId, String position, int compensation) {
        if (workers.containsKey(workerId)) return false;

        workers.put(workerId, new Worker(position, compensation));
        return true;
    }

    @Override
    public String register(String workerId, int timestamp) {
        Worker w = workers.get(workerId);
        if (w == null) return "invalid_request";

        if (!w.inOffice) {
            w.inOffice = true;
            w.lastEntryTime = timestamp;
        } else {
            w.inOffice = false;
            w.totalTime += (timestamp - w.lastEntryTime);
            w.lastEntryTime = null;
        }

        return "registered";
    }

    @Override
    public Optional<Integer> get(String workerId) {
        Worker w = workers.get(workerId);
        if (w == null) return Optional.empty();

        return Optional.of(w.totalTime);
    }
}