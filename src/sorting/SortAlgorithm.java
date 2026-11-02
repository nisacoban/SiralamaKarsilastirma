package sorting;

/** Tüm sıralama algoritmalarının uyguladığı ortak arayüz. */
public interface SortAlgorithm {
    String name();

    /** Ortalama durum zaman karmaşıklığı (gösterim amaçlı). */
    String complexity();

    /** true ise algoritma O(n²) gibi büyük verilerde çok yavaş çalışır. */
    boolean isSlowOnLargeData();

    void sort(int[] a, Stats s);
}
