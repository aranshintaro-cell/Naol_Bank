
import java.sql.*;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        do {
            System.out.println("\n===== NAOLBANKING SYSTEM (GUEST MENU) =====");
            System.out.println("[1] Log In");
            System.out.println("[2] Create a New Account");
            System.out.println("[3] Exit Application");
            System.out.print("Enter choice: ");

            if (!input.hasNextInt()) {
                System.out.println("Invalid input! Please enter a number.");
                input.nextLine();
                continue;
            }

            int choice = input.nextInt();
            input.nextLine(); 

            switch (choice) {
                case 1:
                    loginAccount(input);
                    break;
                case 2:
                    createAccount(input);
                    break;
                case 3:
                    System.out.println("Thank you for using the Banking Application. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please choose 1, 2, or 3.");
            }
        } while (true);
    }

    public static void loginAccount(Scanner input) {
        System.out.println("\n===== ACCOUNT LOG IN =====");
        System.out.print("Enter Account ID: ");
        String id = input.nextLine();

        System.out.print("Enter Password: ");
        String password = input.nextLine();

        String sql = "SELECT * FROM `personal data` WHERE id = ? AND password = ?";

        try (Connection conn = JavaDB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, id);
            pst.setString(2, password);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("\n>>> Login Successful! Welcome, " + rs.getString("name") + "!");
              
                userDashboard(input, id);
            } else {
                System.out.println(">>> Invalid Account ID or Password!");
            }

        } catch (SQLException e) {
            System.out.println("Error during login: " + e.getMessage());
        }
    }

   
    public static void createAccount(Scanner input) {
        System.out.println("\n===== REGISTER NEW ACCOUNT =====");
        System.out.print("Create your Account ID: ");
        String id = input.nextLine();

        System.out.println("firstname, middlename (N/A if none), Surname ");
        System.out.print("Enter Name: ");
        String name = input.nextLine();

        System.out.print("Contact Number: ");
        String contactNumber = input.nextLine();

        System.out.print("Enter Email: ");
        String email = input.nextLine();

        System.out.print("Create Password: ");
        String password = input.nextLine();

        System.out.print("Initial Deposit Amount (PHP): ");
        double initialBalance = input.nextDouble();
        input.nextLine();

        String sql = "INSERT INTO `personal data` (id, name, contact, gmail, password, balance) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = JavaDB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, id);
            pst.setString(2, name);
            pst.setString(3, contactNumber);
            pst.setString(4, email);
            pst.setString(5, password);
            pst.setDouble(6, initialBalance);

            pst.executeUpdate();
            System.out.println(">>> Account Registered Successfully! You can now log in.");

        } catch (SQLException e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
    }

    
    public static void userDashboard(Scanner input, String loggedInId) {
        int choice;
        do {
            System.out.println("\n===== USER DASHBOARD =====");
            System.out.println("[1] View Profile & Balance");
            System.out.println("[2] Deposit Money");
            System.out.println("[3] Withdraw Money");
            System.out.println("[4] Delete Account");
            System.out.println("[5] Log Out");
            System.out.print("Enter choice: ");

            if (!input.hasNextInt()) {
                System.out.println("Invalid input!");
                input.nextLine();
                continue;
            }

            choice = input.nextInt();
            input.nextLine(); 

            switch (choice) {
                case 1 -> viewProfile(loggedInId);
                case 2 -> depositMoney(input, loggedInId);
                case 3 -> withdrawMoney(input, loggedInId);
                case 4 -> {
                    if (deleteAccount(input, loggedInId)) {
                        return; 
                    }
                }
                case 5 -> {
                    System.out.println(">>> Logged out successfully. Returning to main menu...");
                    return; 
                }
                default -> System.out.println("Invalid choice. Please choose from 1 to 5.");
            }
        } while (true);
    }

    //  VIEW PROFILE 
    public static void viewProfile(String loggedInId) {
        String sql = "SELECT * FROM `personal data` WHERE id = ?";

        try (Connection conn = JavaDB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, loggedInId);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                System.out.println("\n--------------------------------");
                System.out.println("Account ID : " + rs.getString("id"));
                System.out.println("Name       : " + rs.getString("name"));
                System.out.println("Contact    : " + rs.getString("contact"));
                System.out.println("Email      : " + rs.getString("gmail"));
                System.out.println("Balance    : PHP " + rs.getDouble("balance"));
                System.out.println("--------------------------------");
            }

        } catch (SQLException e) {
            System.out.println("Error viewing profile: " + e.getMessage());
        }
    }

    //  DEPOSIT 
    public static void depositMoney(Scanner input, String loggedInId) {
        System.out.println("\n===== Deposit Money =====");
        System.out.print("Enter Deposit Amount: ");
        double amount = input.nextDouble();
        input.nextLine();

        if (amount <= 0) {
            System.out.println(">>> Invalid amount! Must be greater than 0.");
            return;
        }

        String sql = "UPDATE `personal data` SET balance = balance + ? WHERE id = ?";

        try (Connection conn = JavaDB.connect();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setDouble(1, amount);
            pst.setString(2, loggedInId);
            pst.executeUpdate();
            
            System.out.println(">>> Successfully deposited PHP " + amount);

        } catch (SQLException e) {
            System.out.println("Error depositing: " + e.getMessage());
        }
    }

    //  WITHDRAW 
    public static void withdrawMoney(Scanner input, String loggedInId) {
        System.out.println("\n===== Withdraw Money =====");
        
        String checkSql = "SELECT balance FROM `personal data` WHERE id = ?";

        try (Connection conn = JavaDB.connect();
             PreparedStatement checkPst = conn.prepareStatement(checkSql)) {

            checkPst.setString(1, loggedInId);
            ResultSet rs = checkPst.executeQuery();

            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                System.out.println("Current Balance: PHP " + currentBalance);

                System.out.print("Enter Withdrawal Amount: ");
                double amount = input.nextDouble();
                input.nextLine();

                if (amount <= 0) {
                    System.out.println(">>> Invalid amount!");
                    return;
                }

                if (amount > currentBalance) {
                    System.out.println(">>> Insufficient funds!");
                    return;
                }

                String updateSql = "UPDATE `personal data` SET balance = balance - ? WHERE id = ?";
                try (PreparedStatement updatePst = conn.prepareStatement(updateSql)) {
                    updatePst.setDouble(1, amount);
                    updatePst.setString(2, loggedInId);
                    updatePst.executeUpdate();
                    System.out.println(">>> Successfully withdrew PHP " + amount);
                }
            }

        } catch (SQLException e) {
            System.out.println("Error withdrawing: " + e.getMessage());
        }
    }

    //  DELETE ACCOUNT 
    public static boolean deleteAccount(Scanner input, String loggedInId) {
        System.out.println("\n===== Delete Account =====");
        System.out.print("Are you sure you want to delete your account? (yes/no): ");
        String confirm = input.nextLine();

        if (confirm.equalsIgnoreCase("yes")) {
            String sql = "DELETE FROM `personal data` WHERE id = ?";
            try (Connection conn = JavaDB.connect();
                 PreparedStatement pst = conn.prepareStatement(sql)) {

                pst.setString(1, loggedInId);
                pst.executeUpdate();
                System.out.println(">>> Your account has been deleted.");
                return true;

            } catch (SQLException e) {
                System.out.println("Error deleting account: " + e.getMessage());
            }
        } else {
            System.out.println(">>> Deletion cancelled.");
        }
        return false;
    }
}
