import java.sql.*;
import java.util.Scanner;
import java.util.TimerTask;
import java.util.Timer;


public class Main {

    // Method to get MySQL database connection
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/QuizApp?useSSL=false", "root", "root");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error connecting to the database", e);
        }
    }

    // Validate user login
    public static boolean validateUser(String username, String password) {
        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next(); // If user found
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Update profile and password
    public static boolean updateProfile(String username, String newPassword) {
        try (Connection conn = getConnection()) {
            String query = "UPDATE users SET password = ? WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, newPassword);
                stmt.setString(2, username);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method to take quiz with timer and store answers
    public static void takeQuizWithTimer(String username) {
        long startTime = System.currentTimeMillis();
        long timeLimit = 10000; // 10 seconds in milliseconds
        int score = 0;
        int userId = getUserId(username); // Get user ID for storing answers

        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM mcqs";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                int questionNumber = 1;
                Scanner scanner = new Scanner(System.in);
                Timer timer = new Timer();

                // Create a task that updates the time remaining
                TimerTask task = new TimerTask() {
                    long remainingTime = timeLimit;

                    @Override
                    public void run() {
                        long elapsedTime = System.currentTimeMillis() - startTime;
                        remainingTime = timeLimit - elapsedTime;
                        if (remainingTime <= 0) {
                            System.out.println("\nTime's up!");
                            cancel();
                        } else {
                            System.out.print("\rTime remaining: " + (remainingTime / 1000) + " seconds   ");
                        }
                    }
                };

                timer.scheduleAtFixedRate(task, 0, 1000); // Run the timer every second

                while (rs.next()) {
                    // Display the current question and options
                    System.out.println("\nQ" + questionNumber + ": " + rs.getString("question"));
                    System.out.println("a. " + rs.getString("option_a"));
                    System.out.println("b. " + rs.getString("option_b"));
                    System.out.println("c. " + rs.getString("option_c"));
                    System.out.println("d. " + rs.getString("option_d"));

                    String answer = "";
                    boolean validAnswer = false;
                    
                    // Loop to ensure the user enters a valid answer (a/b/c/d)
                    while (!validAnswer) {
                        // Check if time is up
                        if (System.currentTimeMillis() - startTime >= timeLimit) {
                            System.out.println("\nTime's up!");
                            break;
                        }

                        // Show the input prompt after timer update
                        System.out.print("\nEnter your answer (a/b/c/d): ");
                        answer = scanner.nextLine().trim().toLowerCase();
                        
                        if (answer.equals("a") || answer.equals("b") || answer.equals("c") || answer.equals("d")) {
                            validAnswer = true;
                        } else {
                            System.out.println("Invalid input! Please enter a valid option (a/b/c/d).");
                        }
                    }

                    // Store the user's answer in the database
                    storeAnswer(userId, rs.getInt("id"), answer);

                    // Check if the answer is correct
                    if (answer.equalsIgnoreCase(rs.getString("correct_option"))) {
                        score++;
                    }
                    questionNumber++;

                    // If time's up, break out of the loop
                    if (System.currentTimeMillis() - startTime >= timeLimit) {
                        break;
                    }
                }

                // Cancel the timer after the quiz is over
                timer.cancel();
                System.out.println("\nYou scored: " + score);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // Get the user ID from the username
    public static int getUserId(String username) {
        try (Connection conn = getConnection()) {
            String query = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Return -1 if user not found
    }

    // Store the user's answer in the user_answers table
    public static void storeAnswer(int userId, int questionId, String answer) {
        try (Connection conn = getConnection()) {
            String query = "INSERT INTO user_answers (user_id, question_id, selected_option) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, questionId);
                stmt.setString(3, answer); // Insert the answer into 'selected_option'
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
   
    // Logout and close session
    public static void logout() {
        System.out.println("Logging out...");
        System.exit(0);
    }

    // Main method to run the application
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (validateUser(username, password)) {
            System.out.println("Login successful!");

            // Menu for further actions
            boolean running = true;
            while (running) {
                System.out.println("\nMenu:");
                System.out.println("1. Update Profile");
                System.out.println("2. Take Quiz");
                System.out.println("3. Logout");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();  // Consume newline

                switch (choice) {
                    case 1:
                        System.out.print("Enter new password: ");
                        String newPassword = scanner.nextLine();
                        if (updateProfile(username, newPassword)) {
                            System.out.println("Profile updated successfully!");
                        } else {
                            System.out.println("Error updating profile.");
                        }
                        break;
                    case 2:
                        takeQuizWithTimer(username);  // Use this for timed quiz
                        break;
                    case 3:
                        logout();
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } else {
            System.out.println("Invalid username or password.");
        }

        scanner.close();
    }
}
