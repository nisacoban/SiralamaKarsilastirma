package sorting;

import java.util.Random;

/** Test için kullanılabilecek veri seti türleri. */
public enum DatasetType {
    RANDOM("Rastgele"),
    SORTED("Zaten sıralı"),
    REVERSED("Ters sıralı"),
    NEARLY_SORTED("Neredeyse sıralı (%5 karışık)"),
    FEW_UNIQUE("Az farklı değer (0-9 arası tekrar eden)"),
    ALL_EQUAL("Tüm elemanlar aynı");

    private final String label;

    DatasetType(String label) {
        this.label = label;
    }

    public String shortName() {
        switch (this) {
            case RANDOM: return "Rastgele";
            case SORTED: return "Sıralı";
            case REVERSED: return "Ters";
            case NEARLY_SORTED: return "Neredeyse sıralı";
            case FEW_UNIQUE: return "Az farklı";
            default: return "Hepsi aynı";
        }
    }

    @Override
    public String toString() {
        return label;
    }

    public int[] generate(int n, long seed) {
        Random rnd = new Random(seed);
        int[] a = new int[n];
        switch (this) {
            case RANDOM:
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(n * 10 + 1);
                break;
            case SORTED:
                for (int i = 0; i < n; i++) a[i] = i;
                break;
            case REVERSED:
                for (int i = 0; i < n; i++) a[i] = n - i;
                break;
            case NEARLY_SORTED:
                for (int i = 0; i < n; i++) a[i] = i;
                int swaps = Math.max(1, n / 20);
                for (int k = 0; k < swaps; k++) {
                    int i = rnd.nextInt(n), j = rnd.nextInt(n);
                    int t = a[i]; a[i] = a[j]; a[j] = t;
                }
                break;
            case FEW_UNIQUE:
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(10);
                break;
            case ALL_EQUAL:
                java.util.Arrays.fill(a, 7);
                break;
        }
        return a;
    }
}
