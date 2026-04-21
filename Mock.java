@Override
public int getGrantBonus(int start, int end) {

    // check if exact grant exists
    boolean exists = false;
    for (int[] g : grants) {
        if (g[0] == start && g[1] == end) {
            exists = true;
            break;
        }
    }

    if (!exists) return 0;

    int bonus = 0;

    for (Worker w : workers.values()) {
        for (Session s : w.sessions) {

            // must be fully inside THIS grant
            if (s.start >= start && s.end <= end) {

                int duration = s.end - s.start;

                // 🔥 bonus = extra 1x (not 2x)
                bonus += duration * s.compensation;
            }
        }
    }

    return bonus;
}