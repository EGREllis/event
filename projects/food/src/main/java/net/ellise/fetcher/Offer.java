package net.ellise.fetcher;

public class Offer {
    private final String name;
    private final Double price;
    private final Double weight;
    private final boolean available;

    public Offer(String name, Double price, Double weight, boolean available) {
        this.name = name;
        this.price = price;
        this.weight = weight;
        this.available = available;
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

    public boolean isAvailable() {
        return available;
    }

    @Override
    public String toString() {
        return String.format("{available? %1$b %2$2f\\G £%3$2f %4$s}", available, weight, price, name);
    }
}
