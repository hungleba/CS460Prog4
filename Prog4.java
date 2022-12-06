import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.time.Duration;
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
                + "Could not get unique ID for new customer.");
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
                + "Could not add new customer, please double check DOB.");
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
                + "Could not get unique ID for new flight.");
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
        formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime durationFormatted = LocalDateTime.parse(duration, formatter);
        if (departTimeFormatted.getHour() + durationFormatted.getHour() >= 24) {
            System.out.println("ERR: Departure time and Landing time must have the same date!");
            return;
        }
        // Get depAirport
        System.out.print("Departure Airport (3 characters): ");
        String depAirport = inputReader.nextLine().trim();
        if (depAirport.length() != 3) {
            System.out.println("ERR: Invalid airport abbreviation");
        }
        // Get arrAirport
        System.out.print("Arrival Airport (3 characters): ");
        String arrAirport = inputReader.nextLine().trim();
        if (arrAirport.length() != 3) {
            System.out.println("ERR: Invalid airport abbreviation");
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
                    + "Could not add new flight.");
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
        System.out.print("Please enter number of checked bag customer " + cusID 
                        + "checked in for flight " + flightID + ": ");
        input = inputReader.nextLine().trim();
        try {
            luggage = Integer.parseInt(input);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // Prompt for how many times the passenger ordered a beverage or snack 
        System.out.print("Please enter how many times the customer " + cusID 
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
                + "Could not insert a new flight history.");
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
        String query =  "SELECT flightID FROM history WHERE cusID = " +cusID + 
                        " AND "+ flightID + " IN (SELECT flightID FROM history WHERE cusID = " +cusID + ")" +
                        " AND flightID != " + flightID;
        Statement stmt = null;
        ResultSet result = null;
        ArrayList<Integer> cus_his = new ArrayList<>();
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
                    if (overlap.contains(result.getInt("flightID"))){
                        stmt.close();
                        return false;
                    }
                }
                // check if any of the flight in the history is the overlapping flight
                while(result.next()){
                    if (overlap.contains(result.getInt("flightID"))){
                        stmt.close();
                        return false;
                    }
                }
            }
            stmt.close();
            return true;  
            
        } catch (SQLException e) {
            System.err.println("*** SQLException:  "
                + "Could not get unique ID for new flight.");
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
        String query =  "SELECT flightID FROM flight WHERE  flightID NOT IN " +
                        "(SELECT flightID FROM   flight" +
                            " WHERE flight.DepartTime <= (SELECT DepartTime + Duration " +
                                                        "FROM   flight WHERE flightID = " + flightID + ") " +
                            "OR (flight.departTime + flight.duration) >= (SELECT  departTime " +
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
                + "Could not get unique ID for new flight.");
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
                        + "Could not delete customer flight history.");
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
                        + "Could not delete customer.");
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
                    + "Could not delete customer attribute.");
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
                + "Could not delete related flights from customer's flight history.");
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
                + "Could not delete provided flight, please double check flightID.");
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
                + "Could not delete from customer's flight history.");
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
                    + "Could not update attribute.");
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
                    + "Could not update attribute in customer.");
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
                + "Could not update flight history.");
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
            query = "UPDATE FLIGHT SET " + chosenOption + " = " +  update_val + "WHERE flightID = "+flightID;
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
            formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalDateTime durationFormatted = LocalDateTime.parse(duration, formatter);
            if (departTimeFormatted.getHour() + durationFormatted.getHour() >= 24) {
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
                + "Could not update flight.");
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
        String query = "SELECT DISTINCT cusID FROM history WHERE fligthID = " + flightID;
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
                + "Could not get unique ID for new flight.");
            System.err.println("\tMessage:   " + e.getMessage());
            System.err.println("\tSQLState:  " + e.getSQLState());
            System.err.println("\tErrorCode: " + e.getErrorCode());
            System.exit(-1);
        }
        return true;
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
        System.out.println("Please choose from one of the following queries to perform:");
        System.out.println("1. Display list of distinct passenger names who flew all 4 airlines in 2021.");
        System.out.println("2. For input airlines and a date in Mar 2021, display list of passenger and their checked bag count.");
        System.out.println("3. For input date in June 2021, display schedules of fligth in ascending order of boarding time.");
        System.out.println("4. For three categories (Student, Frequent Flyer and Handicap) of United Airlines, display passengers who:");
        System.out.println("\ta. Traveled only once in the month of March.");
        System.out.println("\tb. Traveled with exactly one checked in bag anytime in the months of June and July.");
        System.out.println("\tc. Ordered snacks/beverages on at least on one flight.");
        System.out.println("5. For an input airline, display all the total number of passengers in each category that flew in 2021.");
        Integer userInput = 0;
        while (userInput != -1){
            String input = inputReader.nextLine().trim();
            try {
                userInput = Integer.parseInt(input);
            } catch (NumberFormatException nfe) {
                System.out.println("ERR: please enter an integer");
                userInput = 0;
                continue;
            }
            if (userInput == 1) queryOne(dbconn);
            if (userInput == 1) queryTwo(dbconn, inputReader);
            if (userInput == 1) queryThree(dbconn, inputReader);
            if (userInput == 1) queryFour(dbconn);
            if (userInput == 1) queryFive(dbconn, inputReader);
        }
    }

    /*---------------------------------------------------------------------
        |  Method queryFive(Connection dbconn, Scanner inputReader)
        |
        |  Purpose: ask user for a specific airlines and print out count of 
        |           distinct passengers of each categories who flew that airlines in 2021 
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
        // String cusID_query = "SELECT DISTINCT CusID FROM CUSTOMER";
        // Statement cusID_stmtt = null;
        // ResultSet cusID_result = null;
        // String current_cusID = null;
        // String flight_count_query = "SELECT COUNT(DISTINCT AirlineID) " +
        //                             "FROM HISTORY" + current_cusID+ "FROM CUSTOMER";
        // Statement cusID_stmt = null;
        // ResultSet cusID_result = null;
        // try {
        //     stmt = dbconn.createStatement();
        //     answer = stmt.executeQuery(query);
        //     if (answer != null) {
        //         answer.next();
        //         employeeId = answer.getInt("NEXTVAL");
        //     }
        //     stmt.close();  
        // } catch (SQLException e) {
        //         System.err.println("*** SQLException:  "
        //             + "Could not get unique ID.");
        //         System.err.println("\tMessage:   " + e.getMessage());
        //         System.err.println("\tSQLState:  " + e.getSQLState());
        //         System.err.println("\tErrorCode: " + e.getErrorCode());
        //         System.exit(-1);
        // }
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