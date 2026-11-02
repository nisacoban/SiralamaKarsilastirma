package sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/** Algoritmaları bir veri seti üzerinde çalıştırıp süre ölçen sınıf. */
public class Benchmark {

    public static class Result {
        public String name;
        public String complexity;
        public double ms;          // medyan süre (milisaniye)
        public long comparisons;
        public long writes;
        public boolean correct;
    }

    private static boolean warmedUp = false;

    /** JIT derleyicisinin ilk ölçümleri bozmaması için ısınma turu. */
    public static synchronized void warmUp(List<SortAlgorithm> algs) {
        if (warmedUp) return;
        int[] base = DatasetType.RANDOM.generate(2000, 1);
        Stats s = new Stats();
        for (int round = 0; round < 15; round++) {
            for (SortAlgorithm alg : algs) {
                alg.sort(base.clone(), s);
            }
        }
        warmedUp = true;
    }

    public static List<Result> run(DatasetType type, int size, int repeats,
                                   List<SortAlgorithm> algs, Consumer<String> progress) {
        warmUp(algs);
        int[] original = type.generate(size, 42);
        int[] expected = original.clone();
        Arrays.sort(expected);

        List<Result> results = new ArrayList<>();
        for (SortAlgorithm alg : algs) {
            if (progress != null) {
                progress.accept(alg.name() + " çalışıyor... (" + type.shortName() + ", n=" + size + ")");
            }
            double[] times = new double[repeats];
            Stats stats = new Stats();
            boolean ok = true;
            for (int r = 0; r < repeats; r++) {
                int[] data = original.clone();
                stats.reset();
                long t0 = System.nanoTime();
                alg.sort(data, stats);
                long t1 = System.nanoTime();
                times[r] = (t1 - t0) / 1_000_000.0;
                ok &= Arrays.equals(data, expected);
            }
            Arrays.sort(times);
            Result res = new Result();
            res.name = alg.name();
            res.complexity = alg.complexity();
            res.ms = times[repeats / 2]; // medyan
            res.comparisons = stats.comparisons;
            res.writes = stats.writes;
            res.correct = ok;
            results.add(res);
        }
        return results;
    }
}
