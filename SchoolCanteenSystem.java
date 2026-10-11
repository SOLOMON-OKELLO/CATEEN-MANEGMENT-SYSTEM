import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

// --- Custom Exception ---
class InvalidOrderException extends Exception {
    public InvalidOrderException(String message) {
        super(message);
    }
}

// --- Interface ---
interface Chargeable {
    double calculateCharge();
}

// --- Abstract Class ---
abstract class Order implements Chargeable, Comparable<Order> {
    private String id;
    private String customerName;
    private double itemTotal;
    private boolean eligibleForDiscount; // 7% discount rule applicability

    public Order(String id, String customerName, double itemTotal, boolean eligibleForDiscount) throws InvalidOrderException {
        setId(id);
        setCustomerName(customerName);
        setItemTotal(itemTotal);
        this.eligibleForDiscount = eligibleForDiscount;
    }

    public String getId() { return id; }
    public void setId(String id) throws InvalidOrderException {
        if (id == null || id.isBlank()) throw new InvalidOrderException("ID cannot be blank.");
        this.id = id.trim();
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) throws InvalidOrderException {
        if (customerName == null || customerName.isBlank()) throw new InvalidOrderException("Customer name cannot be blank.");
        this.customerName = customerName.trim();
    }

    public double getItemTotal() { return itemTotal; }
    public void setItemTotal(double itemTotal) throws InvalidOrderException {
        if (itemTotal < 0) throw new InvalidOrderException("Item total cannot be negative.");
        this.itemTotal = itemTotal;
    }

    public boolean isEligibleForDiscount() { return eligibleForDiscount; }
    public void setEligibleForDiscount(boolean eligibleForDiscount) { this.eligibleForDiscount = eligibleForDiscount; }

    @Override
    public int compareTo(Order other) {
        // Sort order A = 3: Customer name alphabetically
        return this.customerName.compareToIgnoreCase(other.customerName);
    }

    public abstract String getOrderType();
}

// --- First Subclass: StudentOrder ---
class StudentOrder extends Order {
    private static final double DISCOUNT_RATE = 0.07; // 7% discount (B = 2, P = 7%)
    private static final double THRESHOLD = 20000.0;   // Applies to student orders above 20,000

    public StudentOrder(String id, String customerName, double itemTotal, boolean isLoyal) throws InvalidOrderException {
        super(id, customerName, itemTotal, isLoyal);
    }

    @Override
    public double calculateCharge() {
        double total = getItemTotal();
        // 7% discount applies to student orders above 20,000


        if (total > THRESHOLD && isEligibleForDiscount()) {
            return total - (total * DISCOUNT_RATE);
        }
        return total;
    }

    @Override
    public String getOrderType() {
        return "Student Order";
    }
}

// --- Second Subclass: StaffOrder ---
class StaffOrder extends Order {
    private static final double SERVICE_CHARGE_RATE = 0.10; // 10% service charge

    public StaffOrder(String id, String customerName, double itemTotal) throws InvalidOrderException {
        super(id, customerName, itemTotal, false);
    }

    @Override
    public double calculateCharge() {
        // Staff orders: total of items plus 10% service charge
        return getItemTotal() + (getItemTotal() * SERVICE_CHARGE_RATE);
    }

    @Override
    public String getOrderType() {
        return "Staff Order";
    }
}

// --- Robust Input Helper ---
class InputHelper {
    private static Scanner scanner = new Scanner(System.in);

    public static String getString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public static double getPositiveDouble(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                double val = Double.parseDouble(scanner.nextLine().trim());
                if (val < 0) {
                    System.out.println("Value cannot be negative. Try again.");
                    continue;
                }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please enter a valid decimal number.");
            }
        }
    }

    public static int getInt(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }
}

// --- Manager Class ---
class CanteenManager {
    private ArrayList<Order> orders = new ArrayList<>();
    private int counter = 1;

    public String generateNextId() {
        // ID format: Initials + last 3 digits (1321 -> 321) + sequence, e.g., SO321-001
        return String.format("SO321-%03d", counter++);
    }

    public void addOrder(Order order) {
        orders.add(order);
        System.out.println("Order added successfully with ID: " + order.getId());
    }

    public Order findById(String id) {
        for (Order o : orders) {
            if (o.getId().equalsIgnoreCase(id.trim())) {
                return o;
            }
        }
        return null;
    }

    public boolean removeOrder(String id) {
        Order o = findById(id);
        if (o != null) {
            orders.remove(o);
            return true;
        }
        return false;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public double getTotalCharges() {
        double sum = 0;
        for (Order o : orders) {
            sum += o.calculateCharge();
        }
        return sum;
    }

    public double getTotalDiscountsGiven() {
        double totalDiscount = 0;
        for (Order o : orders) {
            if (o instanceof StudentOrder) {
                double base = o.getItemTotal();
                if (base > 20000 && o.isEligibleForDiscount()) {
                    totalDiscount += (base * 0.07);
                }
            }
        }
        return totalDiscount;
    }
}

// --- Main Test Driver & Menu Interface ---
public class SchoolCanteenSystem {
    public static void main(String[] args) {
        // Personal Header Display
        System.out.println("==================================================");
        System.out.println(" VICTORIA UNIVERSITY KAMPALA - OOP COURSEWORK");
        System.out.println(" Student Name: Solomon Okello");
        System.out.println(" Reg Number:   VU-BCS-2511-1321-EVE");
        System.out.println(" Business:     School Canteen | Discount: 7%");
        System.out.println("==================================================");

        CanteenManager manager = new CanteenManager();
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n--- SCHOOL CANTEEN MANAGEMENT MENU ---");
            System.out.println("1. Add Student Order");
            System.out.println("2. Add Staff Order");
            System.out.println("3. List Sorted Report (Alphabetical by Name)");
            System.out.println("4. Find Record by ID");
            System.out.println("5. Remove Record");
            System.out.println("6. Show Summary (Total Charges & Discounts)");
            System.out.println("7. Exit");
            
            int choice = InputHelper.getInt("Choose an option (1-7): ");

            switch (choice) {
                case 1:
                    try {
                        String name = InputHelper.getString("Enter student name: ");
                        double total = InputHelper.getPositiveDouble("Enter item total amount (UGX): ");
                        String studentTypeInput = InputHelper.getString("Is student eligible for 7% discount rule (yes/no)? ");
                        boolean eligible = studentTypeInput.equalsIgnoreCase("yes");
                        
                        String id = manager.generateNextId();
                        StudentOrder sOrder = new StudentOrder(id, name, total, eligible);
                        manager.addOrder(sOrder);
                    } catch (InvalidOrderException e) {
                        System.out.println("Error adding student order: " + e.getMessage());
                    }
                    break;

                case 2:
                    try {
                        String name = InputHelper.getString("Enter staff name: ");
                        double total = InputHelper.getPositiveDouble("Enter item total amount (UGX): ");
                        
                        String id = manager.generateNextId();
                        StaffOrder staffOrder = new StaffOrder(id, name, total);
                        manager.addOrder(staffOrder);
                    } catch (InvalidOrderException e) {
                        System.out.println("Error adding staff order: " + e.getMessage());
                    }
                    break;

                case 3:
                    ArrayList<Order> list = manager.getOrders();
                    if (list.isEmpty()) {
                        System.out.println("No records found in the system.");
                    } else {
                        // Sort using Comparable implementation (Sort order A = 3: Customer name alphabetically)
                        Collections.sort(list);
                        System.out.println("\n--- SORTED CANTEEN REPORT (ALPHABETICAL BY NAME) ---");
                        System.out.printf("%-12s | %-18s | %-15s | %-12s | %-12s%n", "ID", "Customer Name", "Type", "Base Total", "Final Charge");
                        System.out.println("----------------------------------------------------------------------------------");
                        for (Order o : list) {
                            System.out.printf("%-12s | %-18s | %-15s | UGX %,9.0f | UGX %,9.0f%n",
                                    o.getId(), o.getCustomerName(), o.getOrderType(), o.getItemTotal(), o.calculateCharge());
                        }
                    }
                    break;

                case 4:
                    String searchId = InputHelper.getString("Enter Order ID to find: ");
                    Order found = manager.findById(searchId);
                    if (found != null) {
                        System.out.println("Record Found -> ID: " + found.getId() + ", Name: " + found.getCustomerName() + 
                                           ", Type: " + found.getOrderType() + ", Final Charge: UGX " + found.calculateCharge());
                    } else {
                        System.out.println("Order with ID " + searchId + " not found.");
                    }
                    break;

                case 5:
                    String removeId = InputHelper.getString("Enter Order ID to remove: ");
                    Order toRemove = manager.findById(removeId);
                    if (toRemove != null) {
                        System.out.println("Found: " + toRemove.getCustomerName() + " (" + toRemove.getOrderType() + ")");
                        String confirm = InputHelper.getString("Are you sure you want to remove this record? (yes/no): ");
                        if (confirm.equalsIgnoreCase("yes")) {
                            manager.removeOrder(removeId);
                            System.out.println("Record removed successfully.");
                        } else {
                            System.out.println("Removal cancelled.");
                        }
                    } else {
                        System.out.println("Order ID not found.");
                    }
                    break;

                case 6:
                    System.out.println("\n--- FINANCIAL SUMMARY ---");
                    System.out.printf("Total Charges Collected:  UGX %,.0f%n", manager.getTotalCharges());
                    System.out.printf("Total Discounts Given:    UGX %,.0f%n", manager.getTotalDiscountsGiven());
                    break;

                case 7:
                    running = false;
                    System.out.println("Exiting system. Thank you!");
                    break;

                default:
                    System.out.println("Invalid option. Please choose between 1 and 7.");
            }
        }
        sc.close();
    }
}