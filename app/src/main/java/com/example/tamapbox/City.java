package com.example.tamapbox;

import com.mapbox.geojson.Point;

public class City {
    int x;
    int y;
    private Point coordinat;
    private String namaLokasi;

    public City(Point coordinat, String namaLokasi) {
        this.coordinat = coordinat;
        this.namaLokasi = namaLokasi;
    }




    // Constructs a randomly placed city
    public City(){
        this.x = (int)(Math.random()*200);
        this.y = (int)(Math.random()*200);
    }

    // Constructs a city at chosen x, y location
    public City(int x, int y){
        this.x = x;
        this.y = y;
    }




    // Gets city's x coordinate
    public int getX(){
        return this.x;
    }

    // Gets city's y coordinate
    public int getY(){
        return this.y;
    }

    // Gets the distance to given city
    public double distanceTo(City city){
        int xDistance = Math.abs(getX() - city.getX());
        int yDistance = Math.abs(getY() - city.getY());
        double distance = Math.sqrt( (xDistance*xDistance) + (yDistance*yDistance) );


        return distance;
    }

    public String getNamaLokasi() {
        return namaLokasi;
    }

    public void setNamaLokasi(String namaLokasi) {
        this.namaLokasi = namaLokasi;
    }

    @Override
    public String toString(){
        return getX()+", "+getY();
    }

    public Point getCoordinat() {
        return coordinat;
    }

    public void setCoordinat(Point coordinat) {
        this.coordinat = coordinat;
    }
}