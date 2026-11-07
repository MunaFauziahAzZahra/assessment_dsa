/**
 * Antrian Bank dengan anti-starvation (antrian ganda + rasio).
 * Aturan: setelah RASIO nasabah VIP dilayani berturut-turut,
 * nasabah reguler terdepan (jika ada) WAJIB dilayani.
 * Queue diimplementasikan sendiri dengan array sirkular (tanpa java.util.*).
 */
class Nasabah {
    private final int id;
    private final String nama;
    private final boolean vip;

    Nasabah(int id, String nama, boolean vip) {
        this.id = id;
        this.nama = nama;
        this.vip = vip;
    }

    boolean isVip() { return vip; }

    @Override
    public String toString() {
        return (vip ? "VIP" : "REG") + "-" + id + "(" + nama + ")";
    }
}

/** Queue array sirkular dengan resize (dobel kapasitas) saat penuh. */
class AntrianSirkular {
    private Nasabah[] data;
    private int head;   // indeks elemen terdepan
    private int size;   // jumlah elemen

    AntrianSirkular(int kapasitasAwal) {
        data = new Nasabah[kapasitasAwal];
        head = 0;
        size = 0;
    }

    boolean isEmpty() { return size == 0; }
    int size() { return size; }

    /** O(1) amortized. */
    void enqueue(Nasabah n) {
        if (size == data.length) resize();
        int tail = (head + size) % data.length;
        data[tail] = n;
        size++;
    }

    /** O(1). */
    Nasabah dequeue() {
        if (isEmpty()) return null;
        Nasabah n = data[head];
        data[head] = null;
        head = (head + 1) % data.length;
        size--;
        return n;
    }

    Nasabah get(int i) { // i = posisi logis dari depan
        return data[(head + i) % data.length];
    }

    /** O(n), jarang terjadi -> amortized O(1) untuk enqueue. */
    private void resize() {
        Nasabah[] baru = new Nasabah[data.length * 2];
        for (int i = 0; i < size; i++) {
            baru[i] = data[(head + i) % data.length];
        }
        data = baru;
        head = 0;
    }
}

public class AntrianBank {
    private final AntrianSirkular vip = new AntrianSirkular(4);
    private final AntrianSirkular reguler = new AntrianSirkular(4);
    private final int rasio;          // maksimal VIP berturut-turut
    private int vipBerturut = 0;
    private int nextId = 1;

    AntrianBank(int rasio) { this.rasio = rasio; }

    void enqueueVIP(String nama) {
        vip.enqueue(new Nasabah(nextId++, nama, true));
    }

    void enqueueReguler(String nama) {
        reguler.enqueue(new Nasabah(nextId++, nama, false));
    }

    /** Dequeue dengan aturan anti-starvation. */
    Nasabah dequeue() {
        if (vip.isEmpty() && reguler.isEmpty()) return null;

        boolean layaniReguler;
        if (reguler.isEmpty()) layaniReguler = false;
        else if (vip.isEmpty()) layaniReguler = true;
        else layaniReguler = (vipBerturut >= rasio); // jatah reguler tiba

        if (layaniReguler) {
            vipBerturut = 0;
            return reguler.dequeue();
        }
        vipBerturut++;
        return vip.dequeue();
    }

    void displayQueue() {
        StringBuilder sb = new StringBuilder();
        sb.append("  VIP     [").append(vip.size()).append("]: ");
        for (int i = 0; i < vip.size(); i++) sb.append(vip.get(i)).append(" ");
        sb.append("\n  Reguler [").append(reguler.size()).append("]: ");
        for (int i = 0; i < reguler.size(); i++) sb.append(reguler.get(i)).append(" ");
        System.out.println(sb);
    }

    public static void main(String[] args) {
        AntrianBank bank = new AntrianBank(3); // 3 VIP : 1 reguler

        System.out.println("=== Skenario: reguler datang lebih dulu, lalu VIP membanjir ===");
        bank.enqueueReguler("Budi");
        bank.enqueueReguler("Citra");
        bank.enqueueReguler("Dedi");
        for (int i = 1; i <= 6; i++) bank.enqueueVIP("Vip" + i);
        bank.displayQueue();

        System.out.println("\n--- Pemanggilan tahap 1 (5 panggilan) ---");
        for (int i = 1; i <= 5; i++) {
            System.out.println("Panggilan " + i + ": " + bank.dequeue());
        }

        System.out.println("\n--- VIP terus berdatangan saat sedang melayani ---");
        for (int i = 7; i <= 12; i++) bank.enqueueVIP("Vip" + i);
        bank.displayQueue();

        System.out.println("\n--- Pemanggilan tahap 2 (sampai kosong) ---");
        int no = 6;
        Nasabah n;
        int vipBerturutCek = 0, maxVipBerturut = 0;
        while ((n = bank.dequeue()) != null) {
            System.out.println("Panggilan " + (no++) + ": " + n);
            if (n.isVip()) { vipBerturutCek++; if (vipBerturutCek > maxVipBerturut) maxVipBerturut = vipBerturutCek; }
            else vipBerturutCek = 0;
        }
        System.out.println("\nAntrian akhir:");
        bank.displayQueue();
        System.out.println("Catatan: tanpa anti-starvation, 3 reguler baru dipanggil setelah"
                + " seluruh 12 VIP selesai. Dengan rasio 3:1, reguler dilayani"
                + " setiap 3 VIP (maks VIP berturut di tahap 2 = " + maxVipBerturut + ").");
    }
}