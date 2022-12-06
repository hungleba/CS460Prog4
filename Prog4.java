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
            + " VALUES (%s, %s, %s, %s, %s, %s, TO_DATE('%s', 'yyyy/mm/dd hh24:mi'), TO_DATE('%s', 'yyyy/mm/dd hh24:mi'), TO_DSINTERVAL('%s'), '%s', '%s')";
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
        // TO-DO
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
            // Perform deletion
            Statement stmt = null;
            ResultSet answer = null;
            String query = "DELETE FROM CUSTOMER WHERE CusId = %s";
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
            // Delete related flight history table
            query = "DELETE FROM HISTORY WHERE CusId = %s";
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
        // Perform deletion
        Statement stmt = null;
        ResultSet answer = null;
        String query = "DELETE FROM FLIGHT WHERE FlightId = %s";
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
        // Delete related flight from customer history table
        query = "DELETE FROM HISTORY WHERE FlightId = %s";
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

    private static void recordUpdate(Connection dbconn, Scanner inputReader) {
        // TO-DO
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
        String cusID_query = "SELECT DISTINCT CusID FROM CUSTOMER";
        Statement cusID_stmt = null;
        ResultSet cusID_result = null;
        String current_cusID = null;
        String flight_count_query = "SELECT COUNT(DISTINCT AirlineID) " +
                                    "FROM HISTORY" + current_cusID+ "FROM CUSTOMER";
        Statement cusID_stmt = null;
        ResultSet cusID_result = null;
        try {
            stmt = dbconn.createStatement();
            answer = stmt.executeQuery(query);
            if (answer != null) {
                answer.next();
                employeeId = answer.getInt("NEXTVAL");
            }
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