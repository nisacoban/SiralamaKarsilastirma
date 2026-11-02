package sorting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Projedeki tüm sıralama algoritmaları. */
public final class SortAlgorithms {

    private SortAlgorithms() {}

    public static List<SortAlgorithm> all() {
        List<SortAlgorithm> list = new ArrayList<>();
        list.add(new BubbleSort());
        list.add(new SelectionSort());
        list.add(new InsertionSort());
        list.add(new ShellSort());
        list.add(new MergeSort());
        list.add(new QuickSort());
        list.add(new HeapSort());
        list.add(new JavaArraysSort());
        return list;
    }

    private static void swap(int[] a, int i, int j, Stats s) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
        s.writes += 2;
    }

    // ------------------------------------------------------------ Bubble
    static class BubbleSort implements SortAlgorithm {
        public String name() { return "Bubble Sort"; }
        public String complexity() { return "O(n²)"; }
        public boolean isSlowOnLargeData() { return true; }

        public void sort(int[] a, Stats s) {
            int n = a.length;
            for (int i = 0; i < n - 1; i++) {
                boolean swapped = false;
                for (int j = 0; j < n - 1 - i; j++) {
                    s.comparisons++;
                    if (a[j] > a[j + 1]) {
                        swap(a, j, j + 1, s);
                        swapped = true;
                    }
                }
                if (!swapped) break; // zaten sıralıysa erken çık
            }
        }
    }

    // --------------------------------------------------------- Selection
    static class SelectionSort implements SortAlgorithm {
        public String name() { return "Selection Sort"; }
        public String complexity() { return "O(n²)"; }
        public boolean isSlowOnLargeData() { return true; }

        public void sort(int[] a, Stats s) {
            int n = a.length;
            for (int i = 0; i < n - 1; i++) {
                int min = i;
                for (int j = i + 1; j < n; j++) {
                    s.comparisons++;
                    if (a[j] < a[min]) min = j;
                }
                if (min != i) swap(a, i, min, s);
            }
        }
    }

    // --------------------------------------------------------- Insertion
    static class InsertionSort implements SortAlgorithm {
        public String name() { return "Insertion Sort"; }
        public String complexity() { return "O(n²)"; }
        public boolean isSlowOnLargeData() { return true; }

        public void sort(int[] a, Stats s) {
            for (int i = 1; i < a.length; i++) {
                int key = a[i];
                int j = i - 1;
                while (j >= 0) {
                    s.comparisons++;
                    if (a[j] > key) {
                        a[j + 1] = a[j];
                        s.writes++;
                        j--;
                    } else {
                        break;
                    }
                }
                a[j + 1] = key;
                s.writes++;
            }
        }
    }

    // ------------------------------------------------------------- Shell
    static class ShellSort implements SortAlgorithm {
        public String name() { return "Shell Sort"; }
        public String complexity() { return "O(n^1.3)"; }
        public boolean isSlowOnLargeData() { return false; }

        public void sort(int[] a, Stats s) {
            int n = a.length;
            int h = 1;
            while (h < n / 3) h = 3 * h + 1; // Knuth dizisi: 1, 4, 13, 40, ...
            while (h >= 1) {
                for (int i = h; i < n; i++) {
                    int key = a[i];
                    int j = i;
                    while (j >= h) {
                        s.comparisons++;
                        if (a[j - h] > key) {
                            a[j] = a[j - h];
                            s.writes++;
                            j -= h;
                        } else {
                            break;
                        }
                    }
                    a[j] = key;
                    s.writes++;
                }
                h /= 3;
            }
        }
    }

    // ------------------------------------------------------------- Merge
    static class MergeSort implements SortAlgorithm {
        public String name() { return "Merge Sort"; }
        public String complexity() { return "O(n log n)"; }
        public boolean isSlowOnLargeData() { return false; }

        public void sort(int[] a, Stats s) {
            if (a.length < 2) return;
            int[] tmp = new int[a.length];
            mergeSort(a, tmp, 0, a.length - 1, s);
        }

        private void mergeSort(int[] a, int[] tmp, int lo, int hi, Stats s) {
            if (lo >= hi) return;
            int mid = (lo + hi) >>> 1;
            mergeSort(a, tmp, lo, mid, s);
            mergeSort(a, tmp, mid + 1, hi, s);
            merge(a, tmp, lo, mid, hi, s);
        }

        private void merge(int[] a, int[] tmp, int lo, int mid, int hi, Stats s) {
            System.arraycopy(a, lo, tmp, lo, hi - lo + 1);
            s.writes += hi - lo + 1;
            int i = lo, j = mid + 1;
            for (int k = lo; k <= hi; k++) {
                if (i > mid) {
                    a[k] = tmp[j++];
                } else if (j > hi) {
                    a[k] = tmp[i++];
                } else {
                    s.comparisons++;
                    if (tmp[j] < tmp[i]) a[k] = tmp[j++];
                    else a[k] = tmp[i++];
                }
                s.writes++;
            }
        }
    }

    // ------------------------------------------------------------- Quick
    /** Hoare bölümlemesi + üç değerin ortancası (median-of-three) pivotu. */
    static class QuickSort implements SortAlgorithm {
        public String name() { return "Quick Sort"; }
        public String complexity() { return "O(n log n)"; }
        public boolean isSlowOnLargeData() { return false; }

        public void sort(int[] a, Stats s) {
            if (a.length < 2) return;
            quickSort(a, 0, a.length - 1, s);
        }

        private void quickSort(int[] a, int lo, int hi, Stats s) {
            // Küçük parçaya özyineleme, büyük parçada döngü: yığın taşmasını önler
            while (lo < hi) {
                int p = partition(a, lo, hi, s);
                if (p - lo < hi - p) {
                    quickSort(a, lo, p, s);
                    lo = p + 1;
                } else {
                    quickSort(a, p + 1, hi, s);
                    hi = p;
                }
            }
        }

        private int partition(int[] a, int lo, int hi, Stats s) {
            int mid = (lo + hi) >>> 1;
            int x = a[lo], y = a[mid], z = a[hi];
            int pivot = Math.max(Math.min(x, y), Math.min(Math.max(x, y), z));
            int i = lo - 1, j = hi + 1;
            while (true) {
                do { i++; s.comparisons++; } while (a[i] < pivot);
                do { j--; s.comparisons++; } while (a[j] > pivot);
                if (i >= j) return j;
                swap(a, i, j, s);
            }
        }
    }

    // -------------------------------------------------------------- Heap
    static class HeapSort implements SortAlgorithm {
        public String name() { return "Heap Sort"; }
        public String complexity() { return "O(n log n)"; }
        public boolean isSlowOnLargeData() { return false; }

        public void sort(int[] a, Stats s) {
            int n = a.length;
            for (int i = n / 2 - 1; i >= 0; i--) siftDown(a, i, n, s);
            for (int end = n - 1; end > 0; end--) {
                swap(a, 0, end, s);
                siftDown(a, 0, end, s);
            }
        }

        private void siftDown(int[] a, int i, int n, Stats s) {
            while (true) {
                int left = 2 * i + 1, right = left + 1, largest = i;
                if (left < n) {
                    s.comparisons++;
                    if (a[left] > a[largest]) largest = left;
                }
                if (right < n) {
                    s.comparisons++;
                    if (a[right] > a[largest]) largest = right;
                }
                if (largest == i) return;
                swap(a, i, largest, s);
                i = largest;
            }
        }
    }

    // ------------------------------------------------- Java Arrays.sort
    /** Karşılaştırma için Java'nın hazır Dual-Pivot Quicksort'u (sayaç tutulmaz). */
    static class JavaArraysSort implements SortAlgorithm {
        public String name() { return "Arrays.sort (Java)"; }
        public String complexity() { return "O(n log n)"; }
        public boolean isSlowOnLargeData() { return false; }

        public void sort(int[] a, Stats s) {
            Arrays.sort(a);
        }
    }
}
