package sorting;

/** Bir sıralama çalışması sırasında sayılan işlem sayıları. */
public class Stats {
    public long comparisons; // eleman karşılaştırma sayısı
    public long writes;      // diziye yapılan yazma (atama) sayısı

    public void reset() {
        comparisons = 0;
        writes = 0;
    }
}
