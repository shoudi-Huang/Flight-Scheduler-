# Flight Scheduler

## Project Overview
The **Flight Scheduler** is a Java-based application designed to help airlines manage flight schedules, plan timetables, and calculate routes between cities. The application allows users to add, modify, and remove flights and locations, book passengers, and find optimal travel routes with up to three stopovers. The project emphasizes object-oriented design principles, including encapsulation, modularity, and separation of concerns.

## Key Features
- **Flight Management**:
  - Add, remove, and reset flights.
  - Book passengers and dynamically adjust ticket prices based on demand and capacity.
  - Import and export flight data using CSV files.
- **Location Management**:
  - Add and view locations with unique names, coordinates, and demand coefficients.
  - Import and export location data using CSV files.
- **Travel Planning**:
  - Find optimal routes between two locations with up to three stopovers.
  - Sort routes by cost, duration, stopovers, layover time, or flight time.
- **Interactive Terminal Interface**:
  - Users can interact with the application via a command-line interface.
  - Commands are case-insensitive and provide real-time feedback.

## Technical Details
- **Programming Language**: Java
- **Core Classes**:
  - `FlightScheduler`: Main entry point and container for flight and location data.
  - `Flight`: Represents a flight with attributes like ID, departure time, source, destination, capacity, and ticket price.
  - `Location`: Represents a location with attributes like name, coordinates, and demand coefficient.
- **Algorithms**:
  - **Haversine Formula**: Used to calculate the distance between two locations.
  - **Dynamic Pricing**: Ticket prices adjust based on demand, capacity, and flight distance.
  - **Route Optimization**: Supports Depth-First Search (DFS) and Breadth-First Search (BFS) for finding optimal routes.
- **Error Handling**:
  - Comprehensive error messages for invalid inputs, scheduling conflicts, and file operations.

## Example Commands
- **Flight Management**:
  ```bash
  FLIGHT ADD Monday 18:00 Sydney Melbourne 120
  FLIGHT 0 BOOK 20
  FLIGHT 0 REMOVE
- **Location Management**:
  ```bash
  LOCATION ADD Sydney -33.847927 150.651786 0.4
  LOCATION IMPORT locations.csv
- **Travel Planning**:
  ```bash
  TRAVEL Sydney London cost
  TRAVEL Sydney London duration

## Project Structure
- FlightScheduler.java: Main class and entry point.
- Flight.java: Manages flight data and operations.
- Location.java: Manages location data and operations.
- tests/: Directory for unit and end-to-end test cases.

## Key Highlights
- Object-Oriented Design: Practice encapsulation, inheritance, and polymorphism.
- Algorithm Implementation: Implement DFS, BFS, and dynamic pricing algorithms.
- File Handling: Read and write CSV files for data import/export.
- Error Handling: Develop robust error messages and validation logic.

## How to Run
1. Compile and run the program:
```bash
javac FlightScheduler.java
java FlightScheduler
```
2. Use the interactive terminal to manage flights, locations, and travel routes.



