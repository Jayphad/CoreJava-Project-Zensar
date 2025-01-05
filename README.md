# Quiz Application

This is a Java-based Quiz Application that allows users to create profiles, update profiles, and take quizzes with a timer. The application interacts with a MySQL database to store user data and quiz responses.

## Features

- *User Registration and Login*: Users can create accounts, log in, and update their profiles.
- *Quiz Functionality*: 
  - Real-time countdown timer for each question.
  - Automatic submission when time runs out.
  - Tracks scores and stores user responses in the database.
- *MySQL Integration*: Stores users, quiz questions, and answers in a relational database.

## Prerequisites

- *Java*: JDK 8 or later.
- *MySQL*: Ensure MySQL is installed and running.
- *Database Setup*: Create the database and required tables using the SQL script provided below.

## Database Schema

Run the following SQL script to set up the database:

```sql
CREATE DATABASE quiz_app;

USE quiz_app;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE mcqs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    question TEXT NOT NULL,
    option_a VARCHAR(255) NOT NULL,
    option_b VARCHAR(255) NOT NULL,
    option_c VARCHAR(255) NOT NULL,
    option_d VARCHAR(255) NOT NULL,
    correct_option CHAR(1) NOT NULL
);

CREATE TABLE user_answers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    question_id INT NOT NULL,
    selected_option CHAR(1),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (question_id) REFERENCES mcqs(id)
);
