/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package critical_path_algorithm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Micha
 */
public class Main {

    public static int maxDur;
    public static String format = "%1$-10s %2$-5s %3$-5s %4$-5s %5$-5s %6$-5s %7$-10s\n";
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        HashSet<Task> allTasks = new HashSet<>();
        Task E = new Task("E", 20); // står alene, så sparer en dag.
        Task G = new Task("G", 5, E);
        Task D = new Task("D", 10, E);
        Task C = new Task("C", 5, D, G);
        Task H = new Task("H", 15, E);
        Task F = new Task("F", 15, G);
        Task B = new Task("B", 20, C);
        Task A = new Task("A", 10, B, F, H); // står alene, så sparer en dag.
        allTasks.add(A);
        allTasks.add(B);
        allTasks.add(F);
        allTasks.add(H);
        allTasks.add(C);
        allTasks.add(D);
        allTasks.add(G);
        allTasks.add(E);
        Task[] result = criticalPathAlgo(allTasks);
        print(result);
    }

    public static class Task {

        public String name;

        public int earlyStart;

        public int earlyFinish;

        public int latestStart;

        public int latestFinish;

        public int dur;

        public int criticalPath;

        public HashSet<Task> dependencies = new HashSet<>();

        public Task(String name, int dur, Task... dependencies) {
            this.name = name;
            this.dur = dur;
            this.dependencies.addAll(Arrays.asList(dependencies));
            this.earlyFinish = -1;
        }

        public void setLatest() {
            latestStart = maxDur - criticalPath;
            latestFinish = latestStart + dur;
        }

        public String[] toStringArray() {
            String criticalCond = earlyStart == latestStart ? "Yes" : "No";
            String[] toString = {name, earlyStart +1 + "", earlyFinish + "", latestStart +1 + "", latestFinish + "",
                latestStart - earlyStart + "", criticalCond};
            return toString;
        }

        public boolean isDependent(Task t) {
            // t direct dep?
            if (dependencies.contains(t)) {
                return true;
            }
            // t indirect dep?
            for (Task dep : dependencies) {
                if (dep.isDependent(t)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    public static void calcEarly(HashSet<Task> initials) {
        for (Task initial : initials) {
            initial.earlyStart = 0;
            initial.earlyFinish = initial.dur;
            setEarly(initial);
        }
    }

    public static void setEarly(Task initial) {
        int completionTime = initial.earlyFinish;
        for (Task t : initial.dependencies) {
            if (completionTime >= t.earlyStart) {
                t.earlyStart = completionTime;
                t.earlyFinish = completionTime + t.dur;
            }
            setEarly(t);
        }
    }

    public static HashSet<Task> initials(Set<Task> tasks) {
        HashSet<Task> remaining = new HashSet<>(tasks);
        for (Task t : tasks) {
            for (Task td : t.dependencies) {
                remaining.remove(td);
            }
        }

        System.out.print("Initial nodes: ");
        for (Task t : remaining) {
            System.out.print(t.name + " ");
        }
        System.out.print("\n\n");
        return remaining;
    }

    public static void maxDur(Set<Task> tasks) {
        int max = -1;
        for (Task t : tasks) {
            if (t.criticalPath > max) {
                max = t.criticalPath;
            }
        }
        maxDur = max;
        System.out.println("Critical path length (cost): " + maxDur);
        for (Task t : tasks) {
            t.setLatest();
        }
    }

    public static Task[] criticalPathAlgo(Set<Task> tasks) {
        HashSet<Task> completedCrit = new HashSet<>();
        HashSet<Task> remainingCrit = new HashSet<>(tasks);

        while (!remainingCrit.isEmpty()) {
            boolean progress = false;

            for (Iterator<Task> it = remainingCrit.iterator(); it.hasNext();) {
                Task task = it.next();
                if (completedCrit.containsAll(task.dependencies)) {
                    int critical = 0;
                    for (Task t : task.dependencies) {
                        if (t.criticalPath > critical) {
                            critical = t.criticalPath;
                        }
                    }
                    task.criticalPath = critical + task.dur;
                    completedCrit.add(task);
                    it.remove();
                    progress = true;
                }
            }
            if (!progress) {
                throw new RuntimeException("Cyclic dependency, algorithm stopped");
            }
        }
        // get the cost
        maxDur(tasks);
        HashSet<Task> initialNodes = initials(tasks);
        calcEarly(initialNodes);

        // get the tasks
        Task[] ret = completedCrit.toArray(new Task[0]);
        // create a priority list
        Arrays.sort(ret, (Task o1, Task o2) -> o1.name.compareTo(o2.name));

        return ret;
    }

    public static void print(Task[] tasks) {
        System.out.format(format, "Task", "ES", "EF", "LS", "LF", "Floats", "Critical?");
        for (Task t : tasks) {
            System.out.format(format, (Object[]) t.toStringArray());
        }
    }   
}
