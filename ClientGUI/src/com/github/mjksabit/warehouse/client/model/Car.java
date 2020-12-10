package com.github.mjksabit.warehouse.client.model;

public class Car {
    private String    registrationNumber;
    private int       yearMade;
    private final String[]  colors = new String[3];
    private String    make;
    private String    model;
    private int       price;
    private byte[]    image;

    public Car(String registrationNumber, String make, String model, int yearMade, int price, String... colors) {
        this.registrationNumber = registrationNumber;
        this.yearMade = yearMade;
        this.make = make;
        this.model = model;
        this.price = price;
        for (int i = 0; i < 3 && i<colors.length; i++) {
            this.colors[i] = colors[i];
        }
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public int getYearMade() {
        return yearMade;
    }

    public void setYearMade(int yearMade) {
        this.yearMade = yearMade;
    }

    public String[] getColors() {
        return colors;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setColors(String... colors) {
        for (int i = 0; i < 3; i++) {
            this.colors[i] = i<colors.length ? colors[i] : null;
        }
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
