import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalDateTime;  
import java.time.format.DateTimeFormatter;  
import java.util.Scanner;
public class Program4 {
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

    private static void recordInsertion(Connection dbconn, Scanner inputReader) {
        String input = "";
        Integer userInput = 0;
        while (userInput != -1) {
            System.out.println("Which data would you like to add? (Enter -1 to exit):");
            System.out.println("    1. Customer");
            System.out.println("    2. Flight");
            System.out.println("    3. Employee");
            System.out.println("    4. Customer's Flight History");
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
                recordInsertionEmployee(dbconn, inputReader);
            } else if (userInput == 4) {
                recordInsertionCustomerFlightHistory(dbconn, inputReader);
            }
        }
    }

    /*---------------------------------------------------------------------
        |  Method recordInsertionFlight(Connection dbconn, Scanner inputReader)
        |
        |  Purpose: prompt user for information of a new flight and 
        |           insert its record accordingly into the FLIGHT table
        |
        |  Pre-condition: FLIGHT table already exists and can be inserted values into
        |
        |  Post-condition: None
        |
        |  Parameters:
        |      dbconn -- a Connection object, use for Oracle database connection
        |      inputReader -- a Java Scanner object, use to read input from users
        |
        |  Returns: a Connection object used to connect to Oracle database
    *-------------------------------------------------------------------*/
    private static void recordInsertionFlight(Connection dbconn, Scanner inputReader){
        // Necessary fields to perform insertion
        Integer flightId = null, airlineId = null, pilotId = null, crewId = null, groundStaffId = null;
        String  flight_date = null;     // prompts user for format of yyyy/mm/dd
        String  duration = null;
        String  boardTime = null, departTime = null;        // prompt user for format hh24:mi
        Integer boardGate = null;
        String  depAirport = null, arrAirport = null;       // prompt user for 3-letter abbreviation

        // Get unique ID for new flight from sequence
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
                    + "Could not get unique ID.");
                System.err.println("\tMessage:   " + e.getMessage());
                System.err.println("\tSQLState:  " + e.getSQLState());
                System.err.println("\tErrorCode: " + e.getErrorCode());
                System.exit(-1);
        }

        System.out.println("Please enter details for new flight ID " + flightId);
        // Prompt for airlineID
        System.out.print("Please enter airlineID (1-4 for Delta : SouthWeset: United: Alaska): ");
        String input = inputReader.nextLine().trim();
        try {
            airlineId = Integer.parseInt(input);
            System.out.print("DEBUG: airlineID " + airlineID);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        }
        // Prompt for pilotID, crew member ID and groundstaffID
        System.out.print("Please enter pilotID, crew member ID and ground staff ID, separting by space: ");
        String[] input = inputReader.nextLine().trim().split();
        try {
            pilotID = Integer.parseInt(input[0]);
            crewID = Integer.parseInt(input[1]);
            groundStaffId = Integer.parseInt(input[2]);
            System.out.print("DEBUG: pilotID crewID and GSID: " + pilotID + " " + crewID + " " + groundStaffId);
        } catch (NumberFormatException nfe) {
            System.out.println("ERR: please enter an integer");
            return;
        } catch (IndexOutOfBoundException e){
            System.out.println("ERR: please enter 3 integers separated by space");
            return;
        }
        // Prompt for flight date 
        System.out.print("Please enter flight date in the format of yyyy/mm/dd: ");
        String input = inputReader.nextLine().trim();
        System.out.print("DEBUG: date " + input;
        if (input.length() != 10 || input[4] != '/' || input[7] != '/'){
            System.out.println("ERR: please enter the date in format yyyy/mm/dd");
            return;
        }
        // Prompt for flight duration 
        System.out.print("Please enter flight date in the format of yyyy/mm/dd: ");
        String input = inputReader.nextLine().trim();
        System.out.print("DEBUG: date " + input);
        if (input.length() != 10 || input[4] != '/' || input[7] != '/'){
            System.out.println("ERR: please enter the date in format yyyy/mm/dd");
            return;
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
                recordDeletionFlight(dbconn, inputReader, null);
            } else if (userInput == 3) {
                recordDeletionStaff(dbconn, inputReader);
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