import java.util.*;

interface Shippable {
    String getName();
    double getWeight();
}

abstract class Product {
    protected String name;
    protected double price;
    protected int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void reduceQuantity(int amount) { quantity -= amount; }
    public abstract boolean isExpired();
    public abstract boolean requiresShipping();
}

class PerishableProduct extends Product implements Shippable {
    private double weight;
    private boolean expired;

    public PerishableProduct(String name, double price, int quantity, double weight, boolean expired) {
        super(name, price, quantity);
        this.weight = weight;
        this.expired = expired;
    }

    public boolean isExpired() { return expired; }
    public boolean requiresShipping() { return true; }
    public double getWeight() { return weight; }
}

class NonPerishableProduct extends Product implements Shippable {
    private double weight;
    private boolean needsShipping;

    public NonPerishableProduct(String name, double price, int quantity, double weight, boolean needsShipping) {
        super(name, price, quantity);
        this.weight = weight;
        this.needsShipping = needsShipping;
    }

    public boolean isExpired() { return false; }
    public boolean requiresShipping() { return needsShipping; }
    public double getWeight() { return weight; }
}

class DigitalProduct extends Product {
    public DigitalProduct(String name, double price, int quantity) {
        super(name, price, quantity);
    }

    public boolean isExpired() { return false; }
    public boolean requiresShipping() { return false; }
}

class CartItem {
    Product product;
    int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }
}

class Cart {
    List<CartItem> items = new ArrayList<>();

    public void add(Product product, int quantity) {
        if (quantity <= product.getQuantity()) {
            items.add(new CartItem(product, quantity));
        } else {
            System.out.println("Quantity exceeds available stock.");
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }
}

class Customer {
    private String name;
    private double balance;

    public Customer(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public String getName() { return name; }
    public double getBalance() { return balance; }
    public void deduct(double amount) { balance -= amount; }
}

class ShippingService {
    public static void ship(List<Shippable> items) {
        System.out.println("** Shipment notice **");
        double totalWeight = 0;
        for (Shippable item : items) {
            System.out.println(item.getName() + "      " + item.getWeight() * 1000 + "g");
            totalWeight += item.getWeight();
        }
        System.out.println("Total package weight " + totalWeight + "kg\n");
    }
}

class CheckoutService {
    public static void checkout(Customer customer, Cart cart) {
        if (cart.isEmpty()) {
            System.out.println("Cart is empty.");
            return;
        }

        double subtotal = 0;
        double shipping = 0;
        List<Shippable> shippables = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            Product p = item.product;
            if (p.isExpired()) {
                System.out.println("Product " + p.getName() + " is expired.");
                return;
            }
            if (p.getQuantity() < item.quantity) {
                System.out.println("Not enough quantity for " + p.getName());
                return;
            }

            subtotal += item.quantity * p.getPrice();

            if (p.requiresShipping() && p instanceof Shippable) {
                for (int i = 0; i < item.quantity; i++) {
                    shippables.add((Shippable) p);
                    shipping += 15;
                }
            }
        }

        double total = subtotal + shipping;

        if (customer.getBalance() < total) {
            System.out.println("Insufficient balance.");
            return;
        }

        for (CartItem item : cart.getItems()) {
            item.product.reduceQuantity(item.quantity);
        }

        customer.deduct(total);

        if (!shippables.isEmpty()) {
            ShippingService.ship(shippables);
        }

        System.out.println("** Checkout receipt **");
        for (CartItem item : cart.getItems()) {
            System.out.println(item.quantity + "x " + item.product.getName() + "     " + (item.quantity * item.product.getPrice()));
        }
        System.out.println("----------------------");
        System.out.println("Subtotal         " + subtotal);
        System.out.println("Shipping         " + shipping);
        System.out.println("Amount           " + total);
        System.out.println("Remaining balance: " + customer.getBalance());
    }
}

public class Main {
    public static void main(String[] args) {
        Product cheese = new PerishableProduct("Cheese", 100, 5, 0.2, false);
        Product biscuits = new PerishableProduct("Biscuits", 150, 3, 0.7, false);
        Product tv = new NonPerishableProduct("TV", 300, 4, 10, true);
        Product scratchCard = new DigitalProduct("Scratch Card", 50, 10);

        Customer customer = new Customer("Fares", 500);

        Cart cart = new Cart();
        cart.add(cheese, 2);
        cart.add(biscuits, 1);
        cart.add(scratchCard, 1);

        CheckoutService.checkout(customer, cart);
    }
}
