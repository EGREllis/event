package net.ellise.fetcher;

public class Offer {
    private final String name;
    private final Double price;
    private final Double weight;
    private final boolean available;
    private final String weightUnit;

    public Offer(String name, Double price, Double weight, String weightUnit, boolean available) {
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.available = available;
        this.weightUnit = weightUnit;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public Double getWeight() {
        return weight;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public boolean isAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return String.format("{available? %1$b %2$2f\\%3$s £%4$2f %5$s}", available, weight, weightUnit, price, name);
    }
}
