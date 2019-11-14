package com.example.tamapbox;

public class Jalur {

    private City lokasiAwal;
    private City lokasiAkhir;
    private int jarak;

    public Jalur(City lokasiAwal, City lokasiAkhir, int jarak) {
        this.lokasiAwal = lokasiAwal;
        this.lokasiAkhir = lokasiAkhir;
        this.jarak = jarak;
    }

    public City getLokasiAwal() {
        return lokasiAwal;
    }

    public void setLokasiAwal(City lokasiAwal) {
        this.lokasiAwal = lokasiAwal;
    }

    public City getLokasiAkhir() {
        return lokasiAkhir;
    }

    public void setLokasiAkhir(City lokasiAkhir) {
        this.lokasiAkhir = lokasiAkhir;
    }

    public int getJarak() {
        return jarak;
    }

    public void setJarak(int jarak) {
        this.jarak = jarak;
    }
}
