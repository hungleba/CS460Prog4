import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  
import java.util.Scanner;
public class Prog4 {
    /*---------------------------------------------------------------------
        |  Method endProgram(Connection dbconn, Scanner inputReader)
        |
        |  Purpose: Making final stabilization step to make sure
        |       the program ends without any memory loss.
        |
        |  Pre-condition:  None
        |
        |  Post-condition: Print out the end line for program UI, disconnect
        |       from the Oracle database, and close STDOUT reader.
        |
        |  Parameters:
        |      dbconn -- a Connection object, use for Oracle database connection
        |      inputReader -- a Java Scanner object, use to read input from users
        |
        |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void endProgram(Connection dbconn, Scanner inputReader) {
        System.out.println("----------------End of the program----------------");
        inputReader.close();
        try {
            dbconn.close();
        } catch (SQLException e) {
            System.out.println("ERR: Cannot close database connection!");
        }
        System.exit(0);
    }

    /*---------------------------------------------------------------------
        |  Method promptLogin(String[] args)
        |
        |  Purpose: Get account information from the user and 
        |       connect to Oracle database
        |
        |  Pre-condition: given account and password must be valid
        |
        |  Post-condition: None
        |
        |  Parameters:
        |      args -- account username and password passed via command-line arguments
        |
        |  Returns: a Connection object used to connect to Oracle database
    *-------------------------------------------------------------------*/
    private static Connection promptLogin(String[] args) {
        final String oracleURL =   // Magic lectura -> aloe access spell
                        "jdbc:oracle:thin:@aloe.cs.arizona.edu:1521:oracle";

        String username = null,    // Oracle DBMS username
               password = null;    // Oracle DBMS password

        if (args.length == 2) {    // get username/password from cmd line args
            username = args[0];
            password = args[1];
        } else {
            System.out.println("\nUsage:  java Prog3 <username> <password>\n"
                             + "    where <username> is your Oracle DBMS"
                             + " username,\n    and <password> is your Oracle"
                             + " password (not your system password).\n");
            System.exit(-1);
        }

            // load the (Oracle) JDBC driver by initializing its base
            // class, 'oracle.jdbc.OracleDriver'.

        try {
                Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
                System.err.println("*** ClassNotFoundException:  "
                    + "Error loading Oracle JDBC driver.  \n"
                    + "\tPerhaps the driver is not on the Classpath?");
                System.exit(-1);
        }
        
            // make and return a database connection to the user's
            // Oracle database

        Connection dbconn = null;
        try {
                dbconn = DriverManager.getConnection
                               (oracleURL,username,password);
        } catch (SQLException e) {
                System.err.println("*** SQLException:  "
                    + "Could not open JDBC connection.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
        }
        return dbconn;
    }

    private static void recordInsertionCustomer(Connection dbconn, Scanner inputReader) {
        // Get unique ID for new customer from sequence
        Integer customerId = null;
        String query = "SELECT CUSTOMER_SEQ.NEXTVAL FROM DUAL";
        Statement stmt = null;
        ResultSet answer = null;
        try {
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(query);
            if (answer != null) {
                answer.next();
                customerId = answer.getInt("NEXTVAL");
            }
            stmt.close();  
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not get unique ID for new customer a00.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        // Get customer name
        System.out.print("Customer name: ");
        String customerName = inputReader.nextLine().trim();
        // Get customer DOB
        System.out.print("Customer DOB (yyyy/mm/dd): ");
        String DOB = inputReader.nextLine().trim();
        // Get customer address
        System.out.print("Customer address: ");
        String customerAddress = inputReader.nextLine().trim();
        // Get customer benefits
        System.out.print("Is this customer a frequent flyer? (0 or 1): ");
        Integer frequentFlyer = null;
        String input = inputReader.nextLine().trim();
        try {
            frequentFlyer = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        if (frequentFlyer != 0 && frequentFlyer != 1) {
            System.out.println("ERR: Invalid benefit value");
            return;
        }
        System.out.print("Is this customer a student? (0 or 1): ");
        Integer student = null;
        input = inputReader.nextLine().trim();
        try {
            student = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        if (student != 0 && student != 1) {
            System.out.println("ERR: Invalid benefit value");
            return;
        }
        System.out.print("Is this customer a handicapped person? (0 or 1): ");
        Integer handicap = null;
        input = inputReader.nextLine().trim();
        try {
            handicap = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        if (handicap != 0 && handicap != 1) {
            System.out.println("ERR: Invalid benefit value");
            return;
        }
        // Perform customer insertion
        query = "INSERT INTO CUSTOMER "
            + "(CusId, Name, DOB, Address, FrequentFlyer, Student, Handicap)"
            + " VALUES (%s, '%s', TO_DATE('%s', 'yyyy/mm/dd'), '%s', %s, %s, %s)";
        final String finalQuery = String.format(query, customerId, customerName, DOB, customerAddress, frequentFlyer, student, handicap);
        try {
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(finalQuery);
            stmt.close();  
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not add new customer, please double check DOB. a01");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
    }

    private static void recordInsertionFlight(Connection dbconn, Scanner inputReader) {
        // Get unique ID for new flight from sequence
        Integer flightId = null;
        String query = "SELECT FLIGHT_SEQ.NEXTVAL FROM DUAL";
        Statement stmt = null;
        ResultSet answer = null;
        try {
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(query);
            if (answer != null) {
                answer.next();
                flightId = answer.getInt("NEXTVAL");
            }
            stmt.close();  
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not get unique ID for new flight. a02");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        // Get airlineId
        Integer airlineId = null;
        System.out.print("Airline ID: ");
        String input = inputReader.nextLine().trim();
        try {
            airlineId = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // Get pilotId
        Integer pilotId = null;
        System.out.print("Pilot ID: ");
        input = inputReader.nextLine().trim();
        try {
            pilotId = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        if (getAirlineOfEmployee(dbconn, pilotId) != airlineId) {
            System.out.println("ERR: pilot not belong to the airline");
            return;
        }
        // Get crewId
        Integer crewId = null;
        System.out.print("Crew ID: ");
        input = inputReader.nextLine().trim();
        try {
            crewId = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        if (getAirlineOfEmployee(dbconn, crewId) != airlineId) {
            System.out.println("ERR: crew not belong to the airline");
            return;
        }
        // Get groundStaffId
        Integer groundStaffId = null;
        System.out.print("Ground staff ID: ");
        input = inputReader.nextLine().trim();
        try {
            groundStaffId = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        if (getAirlineOfEmployee(dbconn, groundStaffId) != airlineId) {
            System.out.println("ERR: ground staff not belong to the airline");
            return;
        }
        if (pilotId == crewId || pilotId == groundStaffId || crewId == groundStaffId) {
            System.out.println("It needs three different employees to operate the flight!");
            return;
        }
        // Get boardGate
        Integer boardGate = null;
        System.out.print("Board Gate: ");
        input = inputReader.nextLine().trim();
        try {
            boardGate = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // Get duration
        System.out.print("Duration of flight (hh:mm): ");
        String duration = inputReader.nextLine().trim();
        // Get boardTime
        System.out.print("BoardTime (yyyy/mm/dd hh:mm): ");
        String boardTime = inputReader.nextLine().trim();
        // Get departTime
        System.out.print("DepartTime (yyyy/mm/dd hh:mm): ");
        String departTime = inputReader.nextLine().trim();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        LocalDateTime boardTimeFormatted = LocalDateTime.parse(boardTime, formatter);
        LocalDateTime departTimeFormatted = LocalDateTime.parse(departTime, formatter);
        long diffInMins = java.time.Duration.between(boardTimeFormatted, departTimeFormatted).toMinutes();
        if (diffInMins <= 0) {
            System.out.println("ERR: Departure time must be after Board time!");
            return;
        } 
        if (boardTimeFormatted.getYear() != departTimeFormatted.getYear() 
        || boardTimeFormatted.getMonthValue() != departTimeFormatted.getMonthValue()   
        || boardTimeFormatted.getDayOfMonth() != departTimeFormatted.getDayOfMonth()) {
            System.out.println("ERR: Departure time and Board time must have the same date!");
            return;
        }
        LocalTime durationFormatted = LocalTime.parse(duration);
        if (durationFormatted.getHour() < 1 || durationFormatted.getHour() > 5) {
            System.out.println("ERR: Duration must be in range 1-5 hours");
            return;
        }
        Integer carry = 0;
        if (departTimeFormatted.getMinute() + durationFormatted.getMinute() >= 60) {
            carry = 1;
        }
        if (departTimeFormatted.getHour() + durationFormatted.getHour() + carry >= 24) {
            System.out.println("ERR: Departure time and Landing time must have the same date!");
            return;
        }
        // Get depAirport
        System.out.print("Departure Airport (3 characters): ");
        String depAirport = inputReader.nextLine().trim();
        if (depAirport.length() != 3) {
            System.out.println("ERR: Invalid airport abbreviation");
            return;
        }
        // Get arrAirport
        System.out.print("Arrival Airport (3 characters): ");
        String arrAirport = inputReader.nextLine().trim();
        if (arrAirport.length() != 3) {
            System.out.println("ERR: Invalid airport abbreviation");
            return;
        }
        // Perform insertion
        query = "INSERT INTO FLIGHT "
            + "(FlightId, AirlineId, PilotId, CrewId, GroundStaffId, BoardGate, BoardTime, DepartTime, Duration, DepAirport, ArrAirport)"
            + " VALUES (%s, %s, %s, %s, %s, %s, TO_DATE('%s', 'yyyy/mm/dd hh24:mi'), TO_DATE('%s', 'yyyy/mm/dd hh24:mi'), TO_DSINTERVAL('+00 %s:00'), '%s', '%s')";
        final String finalQuery = String.format(query, flightId, airlineId, pilotId, crewId, groundStaffId, boardGate, 
            boardTime, departTime, duration, depAirport, arrAirport);
        try {
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(finalQuery);
            stmt.close();  
        } catch (SQLException e) {
                System.err.println("*** SQLException:  "
                    + "Could not add new flight. a03");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
        }
        System.out.println("Flight "+flightId+" added!");
    }

    private static void recordInsertionCustomerFlightHistory(Connection dbconn, Scanner inputReader) {
        // Necessary fields to perform insertion
        Integer flightId = null, cusID = null, luggage = null, order = null;

        System.out.println("Please enter customer ID to add a flight history to: ");
        // Prompt for cusID
        String input = inputReader.nextLine().trim();
        try {
            cusID = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }

        System.out.println("Please enter flightID that the customer flew on: ");
        // Prompt for flightId
        Integer flightID = null;
        input = inputReader.nextLine().trim();
        try {
            flightID = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }

        // Prompt for checked bag count
        System.out.print("Please enter number of checked bag for customerID " + cusID 
                        + " checked in for flight " + flightID + ": ");
        input = inputReader.nextLine().trim();
        try {
            luggage = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        if (getStudentStatus(dbconn, cusID) == 1) {
            if (luggage > 3) {
                System.out.println("ERR: Exceeding number of luggages for students (3)");
                return;
            }
        } else if (getStudentStatus(dbconn, cusID) == 0) {
            if (luggage > 2) {
                System.out.println("ERR: Exceeding number of luggages for non-students (2)");
                return;
            }
        }
        // Prompt for how many times the passenger ordered a beverage or snack 
        System.out.print("Please enter how many times the customerID " + cusID 
                        + " ordered drink/snack on flight " + flightID + ": ");
        input = inputReader.nextLine().trim();
        try {
            order = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        if (noOverLap(dbconn, cusID, flightID)){
            insertValidHistory(dbconn, cusID, flightID, luggage, order);
        }
        else {
            System.out.println("There is an overlapping flight for the customer." +
                               " Cannot finish the history insertion");
        }
        // get a list of flights that have time overlapped with the inserted flight 
        // ArrayList<String> conflict_flight = conflictFlight(flightID);    
        // check if the customer of the inserted history has any other flight overlapping with the inserted flight
          
    }

    /*---------------------------------------------------------------------
        |  Method insertValidHistory(Integer cusID, Integer flightID,
        |                            Integer luggage, Integer order)
        |
        |  Purpose: performing insertion of a valid change into history table
        |
        |  Pre-condition: history table must exist 
        |
        |  Post-condition: None
        |
        |  Parameters:
        |      flightID -- identifier of the target flight of which we are looking for
        |                   overlapping flights.
        |      cusID    -- identifier of the target customer to check history for 
        |                   overlapping flight
        |      luggage  -- integer representing the number of checked bag the customer has on this flight
        |      order    -- total number of times the passenger order a drink or snack on the flight
        |
        |  Returns: true if the update/insertion of the flight is safe
        |           false if the update/insertion of the flight results in time conflict in 
        |           the customer's flight history
    *-------------------------------------------------------------------*/
    private static void insertValidHistory(Connection dbconn, Integer cusID, Integer flightID, Integer luggage, Integer order){
        String query = "INSERT INTO history VALUES (" + cusID + ", " + flightID + ", " + luggage + ", " + order +")";
        Statement stmt = null;
        ResultSet result = null;
        // execute the query 
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            stmt.close();  
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not insert a new flight history. a04");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
    }
    /*---------------------------------------------------------------------
        |  Method noOverLap(Integer cusID, Integer flightID)
        |
        |  Purpose: check whether or not the customer will have time conflict
        |           with the updated/newly inserted flight
        |
        |  Pre-condition: the input flight must exists in the FLIGHT table
        |                 and the customer must exists in the CUSTOMER table
        |
        |  Post-condition: None
        |
        |  Parameters:
        |      flightID -- identifier of the target flight of which we are looking for
        |                   overlapping flights.
        |      cusID    -- identifier of the target customer to check history for 
        |                   overlapping flight
        |
        |  Returns: true if the update/insertion of the flight is safe
        |           false if the update/insertion of the flight results in time conflict in 
        |           the customer's flight history
    *-------------------------------------------------------------------*/
    private static boolean noOverLap(Connection dbconn, Integer cusID, Integer flightID){
        ArrayList<Integer> overlap = conflictFlight(dbconn, flightID);
        // query for other flights of given customer who has the update/new flight in history (exclusive)
        String query =  "SELECT flightID FROM history WHERE cusID = " +cusID;
        Statement stmt = null;
        ResultSet result = null;
        ArrayList<Integer> cus_his = new ArrayList<>();
        // execute the query 
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            if (result != null) {
                // check if any of the flight in the history is the overlapping flight
                while(result.next()){
                    if (overlap.contains(result.getInt("flightID")) && result.getInt("flightID") != flightID ){
                        stmt.close();
                        return false;
                    }
                }
                return true;
            }
            stmt.close();
            return true;  
            
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not get unique ID for new flight. a05");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        return true;
    }

    /*---------------------------------------------------------------------
        |  Method conflictFlight(Integer flightID)
        |
        |  Purpose: get a list of all flight that has time conflict
        |           with the input flight (identified by flightID)
        |
        |  Pre-condition: the input flight must exists in the FLIGHT tables 
        |
        |  Post-condition: None
        |
        |  Parameters:
        |      flightID -- identifier of the target flight of which we are looking for
        |                   overlapping flights.
        |
        |  Returns: an ArrayList of Integer representing flightID of flights 
        |           which has time overlap with the target flight
    *-------------------------------------------------------------------*/
    private static ArrayList<Integer> conflictFlight(Connection dbconn, Integer flightID){
        String query =  "SELECT flightID FROM flight WHERE flightID NOT IN " +
                        "(SELECT flightID FROM flight " +
                            "WHERE flight.DepartTime >= (SELECT DepartTime + Duration " +
                                                        "FROM   flight WHERE flightID = " + flightID + ") " +
                            "OR (flight.departTime + flight.duration) <= (SELECT departTime " +
                                                                        "FROM   flight " + 
                                                                        "WHERE flightID = " + flightID + "))";
        Statement stmt = null;
        ResultSet result = null;
        ArrayList<Integer> ret = new ArrayList<>();
        // execute the query 
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            if (result != null) {
                // add all overlapping flightID excluding the compared flightID to the list
                while(result.next()){
                    if (result.getInt("flightID") != flightID){
                        ret.add(result.getInt("flightID"));
                    }
                }
            }
            stmt.close();  
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not get unique ID for new flight. a06");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        return ret;
    }

    private static void recordInsertion(Connection dbconn, Scanner inputReader) {
        String input = "";
        Integer userInput = 0;
        while (userInput != -1) {
            System.out.println("Which data would you like to add? (Enter -1 to exit):");
            System.out.println("    1. Customer");
            System.out.println("    2. Flight");
            System.out.println("    3. Customer's Flight History");
            input = inputReader.nextLine().trim();
            try {
                userInput = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("ERR: please enter an integer");
                userInput = 0;
                continue;
            }
            if (userInput == -1) {
                break;
            }
            if (userInput == 1) {
                recordInsertionCustomer(dbconn, inputReader);
            } else if (userInput == 2) {
                recordInsertionFlight(dbconn, inputReader);
            } else if (userInput == 3) {
                recordInsertionCustomerFlightHistory(dbconn, inputReader);
            }
        }
    }

    private static void recordDeletionCustomer(Connection dbconn, Scanner inputReader) {
        // Necessary fields for deletion
        Integer customerId = null;
        // Get customerId
        System.out.print("Customer Id to delete: ");
        String input = inputReader.nextLine().trim();
        try {
            customerId = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // Print delete menu
        System.out.println("Which attribute from this customer do you want to delete?");
        System.out.println("    1. All (this customer and his/her flight history will be deleted)");
        System.out.println("    2. DOB");
        System.out.println("    3. Address");
        input = inputReader.nextLine().trim();
        Integer customerChoice = null;
        try {
            customerChoice = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        if (customerChoice == 1) {
            // Delete related flight history table
            Statement stmt = null;
            ResultSet answer = null;
            String query = "DELETE FROM HISTORY WHERE CusId = %s";
            final String finalQuery2 = String.format(query, customerId);
            try {
                stmt = dbconn.createStatement();
                answer = stmt.executeQuery(finalQuery2);
                stmt.close();  
            } catch (SQLException e) {
                    System.err.println("*** SQLException:  "
                        + "Could not delete customer flight history. a07");
                    System.err.println("\tMessage:   " + e.getMessage());
                    System.err.println("\tSQLState:  " + e.getSQLState());
                    System.err.println("\tErrorCode: " + e.getErrorCode());
                    System.exit(-1);
            }
            // Perform deletion
            query = "DELETE FROM CUSTOMER WHERE CusId = %s";
            final String finalQuery1 = String.format(query, customerId);
            try {
                stmt = dbconn.createStatement();
                answer = stmt.executeQuery(finalQuery1);
                stmt.close();  
            } catch (SQLException e) {
                    System.err.println("*** SQLException:  "
                        + "Could not delete customer. a08");
                    System.err.println("\tMessage:   " + e.getMessage());
                    System.err.println("\tSQLState:  " + e.getSQLState());
                    System.err.println("\tErrorCode: " + e.getErrorCode());
                    System.exit(-1);
            }
            System.out.println("Customer and his/her flight history are deleted!");
        } else {
            ArrayList<String> choices = new ArrayList<String>();
            choices.add("DOB");
            choices.add("Address");
            Statement stmt = null;
            ResultSet answer = null;
            String query = "UPDATE CUSTOMER SET " + choices.get(customerChoice-2)+" = NULL WHERE CusId = "+customerId;
            try {
                stmt = dbconn.createStatement();
                answer = stmt.executeQuery(query);
                stmt.close();  
            } catch (SQLException e) {
                System.err.println("*** SQLException:  "
                    + "Could not delete customer attribute. a09");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
            }
            System.out.println("Attribute "+choices.get(customerChoice-2)+" deleted successfully!");
        }
    }

    private static void recordDeletionFlight(Connection dbconn, Scanner inputReader) {
        // Get flightId
        Integer flightId = null;
        System.out.println("*Note: All attributes of a flight record are essentials, so we can only delete the whole flight record");
        System.out.print("Flight Id to delete: ");
        String input = inputReader.nextLine().trim();
        try {
            flightId = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // Delete related flight from customer history table
        Statement stmt = null;
        ResultSet answer = null;
        String query = "DELETE FROM HISTORY WHERE FlightId = %s";
        final String finalQuery2 = String.format(query, flightId);
        try {
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(finalQuery2);
            stmt.close();  
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not delete related flights from customer's flight history. a10");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        // Perform deletion
        query = "DELETE FROM FLIGHT WHERE FlightId = %s";
        final String finalQuery1 = String.format(query, flightId);
        try {
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(finalQuery1);
            stmt.close();  
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not delete provided flight, please double check flightID. a11");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        
        System.out.println("Flight " + flightId + " and its related history records are deleted!");
    }

    private static void recordDeletionCustomerFlightHistory(Connection dbconn, Scanner inputReader) {
        // Get customer ID
        Integer customerId = null;
        System.out.print("Which customer's history to delete? Customer ID: ");
        String input = inputReader.nextLine().trim();
        try {
            customerId = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // Get flight ID
        Integer flightId = null;
        System.out.print("Which flight do you want to delete from this customer? Flight ID (Enter 0 to delete all): ");
        input = inputReader.nextLine().trim();
        try {
            flightId = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // Perform deletion
        Statement stmt = null;
        ResultSet answer = null;
        String query = "DELETE FROM HISTORY WHERE CusId = "+customerId;
        if (!flightId.equals(0)) {
            query += " AND FlightId = " + flightId;
        }
        try {
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(query);
            stmt.close();  
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not delete from customer's flight history. a12");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
    }

    private static void recordDeletion(Connection dbconn, Scanner inputReader) {
        String input = "";
        Integer userInput = 0;
        while (userInput != -1) {
            System.out.println("Which data would you like to delete? (Enter -1 to exit):");
            System.out.println("    1. Customer");
            System.out.println("    2. Flight");
            System.out.println("    3. Customer's Flight History");
            input = inputReader.nextLine().trim();
            try {
                userInput = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("ERR: please enter an integer");
                userInput = 0;
                continue;
            }
            if (userInput == -1) {
                break;
            }
            if (userInput == 1) {
                recordDeletionCustomer(dbconn, inputReader);
            } else if (userInput == 2) {
                recordDeletionFlight(dbconn, inputReader);
            } else if (userInput == 3) {
                recordDeletionCustomerFlightHistory(dbconn, inputReader);
            }
        }
    }

    private static void recordUpdateCustomer(Connection dbconn, Scanner inputReader) {
        Integer customerId = null;
        // Get customerId
        System.out.print("Customer Id to update: ");
        String input = inputReader.nextLine().trim();
        try {
            customerId = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // Print delete menu
        System.out.println("Which attribute from this customer do you want to update?");
        System.out.println("    1. Name");
        System.out.println("    2. DOB");
        System.out.println("    3. Address");
        System.out.println("    4. FrequentFlyer");
        System.out.println("    5. Student");
        System.out.println("    6. Handicap");
        input = inputReader.nextLine().trim();
        Integer customerChoice = null;
        try {
            customerChoice = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        ArrayList<String> choices = new ArrayList<String>();
        choices.add("Name");
        choices.add("DOB");
        choices.add("Address");
        choices.add("FrequentFlyer");
        choices.add("Student");
        choices.add("Handicap");
        // Get user's update value
        if (customerChoice == 1 || customerChoice == 2 || customerChoice == 3) {
            String query = null;
            String updateValue = null;
            if (customerChoice == 2) {
                // Get DOB
                System.out.print("Customer DOB (yyyy/mm/dd): ");
                updateValue = inputReader.nextLine().trim();
                query = "UPDATE CUSTOMER SET %s = TO_DATE('%s', 'yyyy/mm/dd') WHERE CusId = %s";
            } else {
                System.out.print("Update to: ");
                updateValue = inputReader.nextLine().trim();
                query = "UPDATE CUSTOMER SET %s = '%s' WHERE CusId = %s";
            }
            // Perform update
            Statement stmt = null;
            ResultSet answer = null;
            final String finalQuery = String.format(query, choices.get(customerChoice-1), updateValue, customerId);
            try {
                stmt = dbconn.createStatement();
                answer = stmt.executeQuery(finalQuery);
                stmt.close();  
            } catch (SQLException e) {
                System.err.println("*** SQLException:  "
                    + "Could not update attribute. a13");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
            }
        } else {
            Integer updateValue = null;
            System.out.print("Update to (0 or 1 only): ");
            input = inputReader.nextLine().trim();
            try {
                updateValue = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("ERR: please enter an integer");
                return;
            }
            if (updateValue != 0 && updateValue != 1) {
                System.out.println("ERR: Invalid benefit value");
                return;
            }
            // Perform update
            Statement stmt = null;
            ResultSet answer = null;
            String query = "UPDATE CUSTOMER SET %s = %s WHERE CusId = %s";
            final String finalQuery = String.format(query, choices.get(customerChoice-1), updateValue, customerId);
            try {
                stmt = dbconn.createStatement();
                answer = stmt.executeQuery(finalQuery);
                stmt.close();  
            } catch (SQLException e) {
                System.err.println("*** SQLException:  "
                    + "Could not update attribute in customer. a14");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
            }
        }
        System.out.println("Update "+choices.get(customerChoice-1)+" for customer "+customerId+" successfully");
    }

    private static void recordUpdate(Connection dbconn, Scanner inputReader) {
        String input = "";
        Integer userInput = 0;
        while (userInput != -1) {
            System.out.println("Which data would you like to update? (Enter -1 to exit):");
            System.out.println("    1. Customer");
            System.out.println("    2. Flight");
            System.out.println("    3. Customer's flight history");
            input = inputReader.nextLine().trim();
            try {
                userInput = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("ERR: please enter an integer");
                userInput = 0;
                continue;
            }
            if (userInput == -1) {
                break;
            }
            if (userInput == 1) {
                recordUpdateCustomer(dbconn, inputReader);
            } else if (userInput == 2) {
                recordUpdateFlight(dbconn, inputReader);
            } else if (userInput == 3) {
                recordUpdateCustomerFlightHistory(dbconn, inputReader);
            }
        }
    }


    private static void recordUpdateCustomerFlightHistory(Connection dbconn, Scanner inputReader){
        System.out.println("Enter the cusID of the passenger to change flight history of: ");
        String input = inputReader.nextLine().trim();
        String query = null;
        // get the cusID and flightID pair to be updated
        Integer cusID = null;
        Integer oldFlightID = null;
        try {
            cusID = Integer.parseInt(input);
            System.out.println("Enter the flightID you want to update: ");
            input = inputReader.nextLine().trim();
            oldFlightID = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        
        System.out.println("Select a field of the history to make update: ");
        System.out.println("1. Update flightID ");
        System.out.println("2. Update checked bag count");
        System.out.println("3. Update beverage/snack order count");
        Integer userChoice = null;
        
        input = inputReader.nextLine().trim();
        // get the user option of which details about the tuple to change
        try {
            userChoice = Integer.parseInt(input); 
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
    
        if (userChoice == 1){
            System.out.println("Enter the new flightID you want to update the history to: ");
            input = inputReader.nextLine().trim();
            Integer newFlightID = null;
            try {
                newFlightID = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("ERR: please enter an integer");
                return;
            }
            if (!noOverLap(dbconn, cusID, newFlightID)){
                System.out.println("The update results in overlap flight(s) for the customer with ID of " + cusID);
                System.out.println("The update will be abprted");
                return;
            }
            query = "UPDATE history SET flightID = " + newFlightID + "Where cusID = " + cusID + " AND flightID = " + oldFlightID; 
        } else if (userChoice == 2){
            System.out.println("Enter the updated checked bag count: ");
            input = inputReader.nextLine().trim();
            Integer newCount = null;
            try {
                newCount = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("ERR: please enter an integer");
                return;
            }
            if (getStudentStatus(dbconn, cusID) == 1) {
                if (newCount > 3) {
                    System.out.println("ERR: Exceeding number of luggages for students (3)");
                    return;
                }
            } else if (getStudentStatus(dbconn, cusID) == 0) {
                if (newCount > 2) {
                    System.out.println("ERR: Exceeding number of luggages for non-students (2)");
                    return;
                }
            }
            if (newCount > 0){
                query = "UPDATE history SET LuggageCount = " + newCount + "Where cusID = " + cusID + " AND flightID = " + oldFlightID; 
            } else {
                System.out.println("ERR: please enter an valid positive integer");
                return;
            }
        }
        else if (userChoice == 3){
            System.out.println("Enter the updated beverage/snack order count: ");
            input = inputReader.nextLine().trim();
            Integer newCount = null;
            try {
                newCount = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("ERR: please enter an integer");
                return;
        }
            if (newCount > 0){
                query = "UPDATE history SET FoodOrderCount = " + newCount + "Where cusID = " + cusID + " AND flightID = " + oldFlightID; 
            } else {
                System.out.println("ERR: please enter an valid positive integer");
                return;
            }
        }
        else {
            System.out.println("You selected an option other than the ones available (1-3)");
            return;
        }
        Statement stmt = null;
        ResultSet result = null;
        // execute the query 
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            stmt.close();
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not update flight history. a15");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }

    }

    private static void recordUpdateFlight(Connection dbconn, Scanner inputReader){
        System.out.println("Please input an integer for the flightID of the flight you would like to update details on: ");
        String input = inputReader.nextLine().trim();
        Integer flightID = null;
        String query = null;
        try {
            flightID = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        String option1 = "pilotID";
        String option2 = "crewID";
        String option3 = "groundStaffID";
        String option4 = "boardGate";
        String option51 = "boardTime";
        String option52 = "departTime";
        String option53 = "duration";
        String option6 = "depAirport";
        String option7 = "arrAirport";

        String chosenOption = null;
        System.out.println("Please select which detail you would like to change about the flight: ");
        System.out.println("1.  PilotID");
        System.out.println("2.  CrewID");
        System.out.println("3.  GroundStaffID");
        System.out.println("4.  BoardGate");
        System.out.println("5.  BoardTime and DepartTime and Duration");        // all three has to be updated together
        System.out.println("6.  DepAirport");
        System.out.println("7.  ArrAirport");
        System.out.println("Please select an option (1-7): ");
        // get user input
        input = inputReader.nextLine().trim();
        Integer userSelection = Integer.parseInt(input);

        if (userSelection == 1)    chosenOption = option1;
        else if (userSelection == 2)    chosenOption = option2;
        else if (userSelection == 3)    chosenOption = option3;
        else if (userSelection == 4)    chosenOption = option4;
        else if (userSelection == 5)    chosenOption = option51;
        else if (userSelection == 6)    chosenOption = option6;
        else if (userSelection == 7)    chosenOption = option7;

        if (userSelection == 1 || userSelection == 2 || userSelection == 3 || userSelection == 4){
            System.out.println("Please enter an integer for the selected details: ");
            Integer update_val = Integer.parseInt(inputReader.nextLine().trim());
            if (getAirlineOfEmployee(dbconn, update_val) == getAirlineOfFlight(dbconn, flightID)){
                query = "UPDATE FLIGHT SET " + chosenOption + " = " +  update_val + "WHERE flightID = "+flightID;
            } else {
                System.out.println("The new employee does not belong to the airline operating this flight!");
                return;
            }
        } else if (userSelection == 5){
            // Get duration
            System.out.print("Duration of flight (hh:mm): ");
            String duration = inputReader.nextLine().trim();
            // Get boardTime
            System.out.print("BoardTime (yyyy/mm/dd hh:mm): ");
            String boardTime = inputReader.nextLine().trim();
            // Get departTime
            System.out.print("DepartTime (yyyy/mm/dd hh:mm): ");
            String departTime = inputReader.nextLine().trim();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
            LocalDateTime boardTimeFormatted = LocalDateTime.parse(boardTime, formatter);
            LocalDateTime departTimeFormatted = LocalDateTime.parse(departTime, formatter);
            long diffInMins = java.time.Duration.between(boardTimeFormatted, departTimeFormatted).toMinutes();
            if (diffInMins <= 0) {
                System.out.println("ERR: Departure time must be after Board time!");
                return;
            } 
            if (boardTimeFormatted.getYear() != departTimeFormatted.getYear() 
                || boardTimeFormatted.getMonthValue() != departTimeFormatted.getMonthValue()   
                || boardTimeFormatted.getDayOfMonth() != departTimeFormatted.getDayOfMonth()) {
                System.out.println("ERR: Departure time and Board time must have the same date!");
                return;
            }
            LocalTime durationFormatted = LocalTime.parse(duration);
            if (durationFormatted.getHour() < 1 || durationFormatted.getHour() > 5) {
                System.out.println("ERR: Duration must be in range 1-5 hours");
                return;
            }
            Integer carry = 0;
            if (departTimeFormatted.getMinute() + durationFormatted.getMinute() >= 60) {
                carry = 1;
            }
            if (departTimeFormatted.getHour() + durationFormatted.getHour() + carry >= 24) {
                System.out.println("ERR: Departure time and Landing time must have the same date!");
                return;
            }
            if (!validChangeForAllPassenger(dbconn, flightID)){
                System.out.println("Update in the flight results in conflict in the flying history."+
                                   " Update can not be performed.");
                return;
            }
            query = "UPDATE FLIGHT SET " + option51 + " = " +  "TO_DATE('" + boardTime + "', 'yyyy/mm/dd hh24:mi'), " 
                                         + option52 + " = " +  "TO_DATE('" + departTime + "', 'yyyy/mm/dd hh24:mi'), "  
                                         + option53 + " = " +  "TO_DSINTERVAL('+00 " + duration + ":00') WHERE flightID = "+flightID;
        }
        else if (userSelection == 6 || userSelection == 7){
            System.out.println("Please enter an 3-letter abbreviation for the airport: ");
            String newAirport = inputReader.nextLine().trim();
            query = "UPDATE FLIGHT SET " + chosenOption + " = " +  newAirport + "WHERE flightID = " +flightID;
        }
        else {
            System.out.println("You did not select a valid option (1-7)");
            return;
        }

        Statement stmt = null;
        ResultSet result = null;
        ArrayList<Integer> cus_his = new ArrayList<>();
        // execute the query 
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            stmt.close();
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not update flight. a16");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
    }
    
            // go to history, get a list of all passenger with the flightID, perform checking on each of them 
            // looping thru all customer with that flight
            //      noOverLap = false -> print cannot perform update -> return
    private static boolean validChangeForAllPassenger(Connection dbconn, Integer flightID){
        // get a list of customer who has the updated flight in their history
        String query = "SELECT DISTINCT cusID FROM history WHERE flightID = " + flightID;
        Statement stmt = null;
        ResultSet result = null;
        // execute the query 
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            if (result != null) {
                // no other flight rather than the target flight -> no time clash
                if (!result.next()){
                    stmt.close();
                    return true;
                }
                else {
                    // if there is overlap with the first customer, return false
                    if (!noOverLap(dbconn, result.getInt("cusID"), flightID)){
                        stmt.close();
                        return false;
                    }
                }
                // check if any of the flight in the history is the overlapping flight
                while(result.next()){
                    if (!noOverLap(dbconn, result.getInt("cusID"), flightID)){
                        stmt.close();
                        return false;
                    }
                }
            }
            // if code reaches here, no overLap in existing customer is found, then the update is valid
            stmt.close();
            return true;  
            
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not get unique ID for new flight. a17");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        return true;
    }

    private static int getAirlineOfEmployee(Connection dbconn, Integer employeeId) {
        String query = "SELECT AirlineId FROM EMPLOYEE WHERE employeeId = " + employeeId;
        Statement stmt = null;
        ResultSet result = null;
        // execute the query 
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            if (result.next() == false) {
                return -1;
            } else {
                return result.getInt("AirlineId");
            }
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not get airline for the provided employee id. a18");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        return -1;
    }

    private static int getAirlineOfFlight(Connection dbconn, Integer flightId) {
        String query = "SELECT AirlineId FROM EMPLOYEE WHERE flightId = " + flightId;
        Statement stmt = null;
        ResultSet result = null;
        // execute the query 
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            if (result.next() == false) {
                return -1;
            } else {
                return result.getInt("AirlineId");
            }
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not get airline for the provided flight id. a19");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        return -1;
    }

    private static int getStudentStatus(Connection dbconn, Integer customerId) {
        String query = "SELECT student FROM CUSTOMER WHERE cusid = " + customerId;
        Statement stmt = null;
        ResultSet result = null;
        // execute the query 
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            if (result.next() == false) {
                return -1;
            } else {
                return result.getInt("student");
            }
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not get student status from the customer.a20");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        return -1;
    }

    /*---------------------------------------------------------------------
        |  Method performQuery(Connection dbconn, Scanner inputReader)
        |
        |  Purpose: elet the user choose from 5 queries to perform 
        |
        |  Pre-condition:  tables inclduing data for the query already exist 
        |                   in the database. 
        |
        |  Post-condition: 
        |
        |  Parameters:
        |      dbconn -- a Connection object, use for Oracle database connection
        |      inputReader -- a Java Scanner object, use to read input from users
        |
        |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void performQuery(Connection dbconn, Scanner inputReader) {
        System.out.println("----------------QUERY----------------");
        Integer userInput = 0;
        while (userInput != -1){
            System.out.println("--------------------QUERY OPTIONS--------------------");
            System.out.println("\t1. Display list of distinct passenger names who flew all 4 airlines in 2021.");
            System.out.println("\t2. For input airlines and a date in Mar 2021, display list of passenger and their checked bag count.");
            System.out.println("\t3. For input date in June 2021, display schedules of fligth in ascending order of boarding time.");
            System.out.println("\t4. For three categories (Student, Frequent Flyer and Handicap) of United Airlines, display passengers who:");
            System.out.println("\t\ta. Traveled only once in the month of March.");
            System.out.println("\t\tb. Traveled with exactly one checked in bag anytime in the months of June and July.");
            System.out.println("\t\tc. Ordered snacks/beverages on at least on one flight.");
            System.out.println("\t5. For an input airline, display all the total number of passengers in each category that flew in 2021.");
            System.out.print("Please choose from one of the following queries to perform (Enter -1 to exit):");
            String input = inputReader.nextLine().trim();
            try {
                userInput = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("ERR: please enter an integer");
                userInput = 0;
                continue;
            }
            if (userInput == 1) queryOne(dbconn);
            if (userInput == 2) queryTwo(dbconn, inputReader);
            if (userInput == 3) queryThree(dbconn, inputReader);
            //if (userInput == 4) queryFour(dbconn);
            //if (userInput == 5) queryFive(dbconn, inputReader);
        }
    }

    /*---------------------------------------------------------------------
        |  Method queryFive(Connection dbconn, Scanner inputReader)
        |
        |  Purpose: ask user for a specific airlines and print out count of 
        |           distinct passengers of each categories who flew in 2021 
        |
        |  Pre-condition:  tables inclduing data for the query already exist 
        |                   in the database. 
        |
        |  Post-condition: N/A
        |
        |  Parameters:
        |      dbconn -- a Connection object, use for Oracle database connection
        |      inputReader -- a Java Scanner object, use to read input from users
        |
        |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void queryFive(Connection dbconn, Scanner inputReader) {
        System.out.println("----------------------------QUERY 5----------------------------");
        String query_FrequentFlyer= " SELECT COUNT(DISTINCT cusID) as total FROM customer JOIN history USING (cusID) WHERE FrequentFlyer = 1";
        String query_Student= " SELECT COUNT(DISTINCT cusID) as total FROM customer JOIN history USING (cusID) WHERE student = 1";
        String query_handicap= " SELECT COUNT(DISTINCT cusID)as total FROM customer JOIN history USING (cusID) WHERE handicap = 1";
        
        Statement stmt = null;
        ResultSet result = null;
        String space = " ";
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query_FrequentFlyer);
            System.out.print("Number of distinct frequent flyer flying in 2021: ");
            if (result != null) {
                while (result.next()){
                    System.out.println(result.getInt("total"));
                }
            }
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query_Student);
            System.out.print("Number of distinct student flying in 2021: ");
            if (result != null) {
                while (result.next()){
                    System.out.println(result.getInt("total"));
                }
            }
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query_FrequentFlyer);
            System.out.print("Number of distinct handicap people flying in 2021: ");
            if (result != null) {
                while (result.next()){
                    System.out.println(result.getInt("total"));
                }
            }
            System.out.println("--------------------END OF RESULT FROM QUERY 5--------------------");
            stmt.close();  
        } catch (SQLException e) {
                System.err.println("*** SQLException:  "
                    + "Could not get unique ID.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
        }
        
    }
    /*---------------------------------------------------------------------
        |  Method queryFour(Connection dbconn, Scanner inputReader)
        |
        |  Purpose: For 3 categories of the passengers of United Airlines,
        |   a.   Traveled only once in the month of March
        |   b.   Traveled with exactly one checked in bag anytime in the months of June and July.
        |   c.   Ordered snacks/beverages on at least on one flight.
        |
        |  Pre-condition:  tables inclduing data for the query already exist 
        |                   in the database. 
        |
        |  Post-condition: N/A
        |
        |  Parameters:
        |      dbconn -- a Connection object, use for Oracle database connection
        |      inputReader -- a Java Scanner object, use to read input from users
        |
        |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void queryFour(Connection dbconn) {
        String fflier = "SELECT cusID from customer WHERE FrequentFlyer = 1";
        String student = "SELECT cusID from customer WHERE student = 1";
        String handicap = "SELECT cusID from customer WHERE handicap = 1";
    
        STRING queryA = " INTERSECT " + "SELECT DISTINCT custID FROM history JOIN flight USING (flightID) " +
                            "WHERE airlineID = 2 AND " + 
                            "departTime BETWEEN TO_DATE('2021/03/01', 'yyyy/mm/dd') AND TO_DATE('2021/03/31 23:59:59', 'yyyy/mm/dd HH24:MI:SS') "+ 
                            "GROUP BY cusID HAVING COUNT(*) = 1";

        // airlineID = 2;
        // query part a for student
        String student_A = student + queryA;
        //System.out.println(student_A);
        String fflier_A = fflier + queryA;
        String handicap_A = handicap + queryA;

        STRING queryB = " INTERSECT " + "SELECT DISTINCT custID FROM history JOIN flight USING (flightID) " +
                            "WHERE airlineID = 2 AND luggageCount = 1" + 
                            "departTime BETWEEN TO_DATE('2021/06/01', 'yyyy/mm/dd') AND TO_DATE('2021/07/31 23:59:59', 'yyyy/mm/dd HH24:MI:SS') ";
        
        String student_B = student + queryB;
        //System.out.println(student_A);
        String fflier_B = fflier + queryB;
        String handicap_B = handicap + queryB;

    }

    /*---------------------------------------------------------------------
        |  Method queryThree(Connection dbconn, Scanner inputReader)
        |
        |  Purpose: For input date in June 2021, display schedules of flight
        |           in ascending order of boarding time.
        |  Pre-condition:  tables inclduing data for the query already exist 
        |                   in the database. 
        |
        |  Post-condition: N/A
        |
        |  Parameters:
        |      dbconn -- a Connection object, use for Oracle database connection
        |      inputReader -- a Java Scanner object, use to read input from users
        |
        |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void queryThree(Connection dbconn, Scanner inputReader) {
        System.out.println("---------------QUERY 3---------------");
        // prompts user for a date in June 2021
        System.out.print("Please enter interested date in June 2021 (01-31): ");
        String input = inputReader.nextLine().trim();
        Integer interestedDate = null;
        // get the user option of which details about the tuple to change
        try {
            interestedDate = Integer.parseInt(input); 
            if (interestedDate < 0 || interestedDate > 31){
                System.err.println("Input invalid (1-31)");
                return;
            }
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // format the string representing the interested date
        String inputDate = "2021/06/" + (interestedDate < 10 ? "0" : "") + interestedDate.toString();
        String query = " SELECT FlightId, BoardGate, Name, BoardTime, DepartTime, Duration, ArrAirport, DepAirport " + 
                        "FROM airlines " + 
                        "JOIN (SELECT * FROM flight WHERE departTime BETWEEN TO_DATE('" + inputDate  +
                     "', 'yyyy/mm/dd') AND TO_DATE('"+ inputDate + " 23:59:59', 'yyyy/mm/dd HH24:MI:SS')) USING (airlineID) ORDER BY boardTime";
        Statement stmt = null;
        ResultSet answer = null;
        try {
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(query);
            System.out.println("\n---Start of the query result---\n");
            if (answer != null) {
                ResultSetMetaData answermetadata = answer.getMetaData();
                for (int i = 1; i <= answermetadata.getColumnCount(); i++) {
                    System.out.print(answermetadata.getColumnName(i) + "\t");
                }
                System.out.println();
                    // Use next() to advance cursor through the result
                    // tuples and print their attribute values
                while (answer.next()) {
                    System.out.println(answer.getInt("flightId") + "\t"
                        + answer.getInt("BoardGate") + "\t\t"
                        + answer.getString("Name") + "\t"
                        + answer.getString("Boardtime") + "\t"
                        + answer.getString("DepartTime") + "\t"
                        + answer.getString("Duration") + "\t"
                        + answer.getString("ArrAirport") + "\t"
                        + answer.getString("DepAirport") + "\t");
                }
            }
            System.out.println("---End of the query result---\n");
                // Shut down the connection to the DBMS.
            stmt.close();  
        } catch (SQLException e) {
                System.err.println("*** SQLException:  "
                    + "Could not fetch query results.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
        }
        System.out.println(query);
    }

    /*---------------------------------------------------------------------
        |  Method queryTwo(Connection dbconn, Scanner inputReader)
        |
        |  Purpose: For input date in June 2021, display schedules of flight
        |           in ascending order of boarding time.
        |  Pre-condition:  tables inclduing data for the query already exist 
        |                   in the database. 
        |
        |  Post-condition: N/A
        |
        |  Parameters:
        |      dbconn -- a Connection object, use for Oracle database connection
        |      inputReader -- a Java Scanner object, use to read input from users
        |
        |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void queryTwo(Connection dbconn, Scanner inputReader) {
        System.out.println("---------------QUERY 2---------------");
        // prompts user for a date in March 2021
        System.out.print("Please enter interested date in March 2021 (01-31): ");
        String input = inputReader.nextLine().trim();
        Integer interestedDate = null;
        // get the user option of which details about the tuple to change
        try {
            interestedDate = Integer.parseInt(input); 
            if (interestedDate < 0 || interestedDate > 31){
                System.err.println("Input invalid (1-31)");
                return;
            }
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        System.out.println("1. Delta ");
        System.out.println("2. SouthWest ");
        System.out.println("3. United ");
        System.out.println("4. Alaska ");
        System.out.print("Please enter interested airlineID (1-4): ");

        input = inputReader.nextLine().trim();
        Integer airlineID = null;
        String airline = null;
        // get the user option of which details about the tuple to change
        try {
            airlineID = Integer.parseInt(input); 
            if (airlineID < 0 || airlineID > 4){
                System.err.println("Input invalid (1-4)");
                return;
            }
            if (airlineID == 1) airline = "Delta";
            else if (airlineID == 2) airline = "SouthWest";
            else if (airlineID == 3) airline = "United";
            else airline = "Alaska";
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // format the string representing the interested date
        String inputDate = "2021/03/" + (interestedDate < 10 ? "0" : "") + interestedDate.toString();

        System.out.println("-----Passgener who flew " + airline + " on March " + interestedDate.toString() + " 2021-----");
        String query = " SELECT Name, luggageCount " + 
                        "FROM customer JOIN history USING (cusID) " + 
                        "JOIN (SELECT flightid FROM flight WHERE airlineID = " + airlineID + " AND departTime BETWEEN TO_DATE('" + inputDate  +
                     "', 'yyyy/mm/dd') AND TO_DATE('"+ inputDate + " 23:59:59', 'yyyy/mm/dd HH24:MI:SS') ) USING (flightID) ORDER BY luggageCount";
        //System.out.println(query);
        
        Statement stmt = null;
        ResultSet result = null;
        String space = " ";
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            ResultSetMetaData answermetadata = result.getMetaData();
            for (int i = 1; i <= answermetadata.getColumnCount(); i++) {
                System.out.print(answermetadata.getColumnName(i) + space.repeat((50 - answermetadata.getColumnName(i).length())));
            }
            System.out.println();
            if (result != null) {
                while (result.next()){
                    System.out.print(result.getString("name"));
                    System.out.print(space.repeat((55 - result.getString("name").length())));
                    System.out.print(result.getInt("luggageCount") + "\n");
                }
            }
            System.out.println("--------------------END OF RESULT FROM QUERY 2--------------------");
            stmt.close();  
        } catch (SQLException e) {
                System.err.println("*** SQLException:  "
                    + "Could not get unique ID.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
        }
    }

    /*---------------------------------------------------------------------
        |  Method queryOne(Connection dbconn)
        |
        |  Purpose: Display list of distinct passenger names who 
        |           flew all 4 airlines in 2021.
        |  Pre-condition:  tables inclduing data for the query already exist 
        |                   in the database. 
        |
        |  Post-condition: N/A
        |
        |  Parameters:
        |      dbconn -- a Connection object, use for Oracle database connection
        |      inputReader -- a Java Scanner object, use to read input from users
        |
        |  Returns:  None.
    *-------------------------------------------------------------------*/
    private static void queryOne(Connection dbconn) {
        System.out.println("-------------------------QUERY 1-------------------------");
        System.out.println("-----Distinct passgener name who flew all four airlines in 2021-----");
        String query = "SELECT DISTINCT CUSTOMER.NAME FROM (SELECT DISTINCT cusID FROM history JOIN flight USING (flightID) " + 
                        "GROUP BY cusID HAVING COUNT(DISTINCT airlineID) = 4) JOIN CUSTOMER USING (cusID)" ;
        Statement stmt = null;
        ResultSet result = null;
        try {
            stmt = dbconn.createStatement();
            result = stmt.executeQuery(query);
            if (result != null) {
                while (result.next()){
                    System.out.println(result.getString("name"));
                }
            }
            System.out.println("---------------END OF RESULT FROM QUERY 1---------------");
            stmt.close();  
        } catch (SQLException e) {
                System.err.println("*** SQLException:  "
                    + "Could not get unique ID.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
        }
    }

    public static void main(String[] args) {
        // Connecting to Oracle DB through JDBC.
        Connection dbconn = promptLogin(args);
        Scanner inputReader = new Scanner(System.in);
        String input = "";
        Integer userInput = 0;
        // Frontend interface/menu
        System.out.println("----------------CS460 Program 4----------------");
        while (userInput != -1) {
            System.out.println("-----------------------------------------------");
            System.out.println("Main menu:");
            System.out.println("    1. Insert");
            System.out.println("    2. Delete");
            System.out.println("    3. Update");
            System.out.println("    4. Perform Query");
            System.out.print("Which operation would you like to perform? (Enter -1 to exit): ");
            input = inputReader.nextLine().trim();
            try {
                userInput = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("ERR: please enter an integer");
                userInput = 0;
                continue;
            }
            if (userInput == -1) {
                break;
            }
            if (userInput == 1) {
                recordInsertion(dbconn, inputReader);
            } else if (userInput == 2) {
                recordDeletion(dbconn, inputReader);
            } else if (userInput == 3) {
                recordUpdate(dbconn, inputReader);
            } else {
                performQuery(dbconn, inputReader);
            }
        }
        endProgram(dbconn, inputReader);  
    }
}