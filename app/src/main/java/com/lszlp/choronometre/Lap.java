package com.lszlp.choronometre;

public class Lap {

    public String unit;
    public int lapsayisi;
    public String lap;
    public String message;


    public Lap(String unit,String lap,int lapsayisi,String message) {
        this.unit = unit;
        this.lapsayisi = lapsayisi;
        this.lap = lap;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
