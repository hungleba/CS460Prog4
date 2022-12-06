-- Create CUSTOMER table & sequence
CREATE TABLE CUSTOMER (
    CusId INT NOT NULL,
    Name VARCHAR2(50) NOT NULL,
    DOB DATE,
    Address VARCHAR2(100),
    FrequentFlyer INT NOT NULL,
    Student INT NOT NULL,
    Handicap INT NOT NULL,
    PRIMARY KEY (CusId)
);
CREATE SEQUENCE CUSTOMER_SEQ START WITH 1 INCREMENT BY 1;

-- Create AIRLINES table & sequence
CREATE TABLE AIRLINES (
    AirlineId INT NOT NULL,
    Name VARCHAR2(10),
    PRIMARY KEY (AirlineId)
);
INSERT INTO AIRLINES (AirlineId, Name) VALUES (1, 'Delta');
INSERT INTO AIRLINES (AirlineId, Name) VALUES (2, 'SouthWest');
INSERT INTO AIRLINES (AirlineId, Name) VALUES (3, 'United');
INSERT INTO AIRLINES (AirlineId, Name) VALUES (4, 'Alaska');

-- Create EMPLOYEE table & sequence
CREATE TABLE EMPLOYEE (
    EmployeeId INT NOT NULL,
    AirlineId INT NOT NULL,
    Name VARCHAR2(50),
    DOB DATE,
    Role VARCHAR2(12),
    PRIMARY KEY (EmployeeId),
    FOREIGN KEY (AirlineId) REFERENCES AIRLINES (AirlineId)
);
CREATE SEQUENCE EMPLOYEE_SEQ START WITH 1 INCREMENT BY 1;

-- Create FLIGHT table & sequence
CREATE TABLE FLIGHT (
    FlightId INT NOT NULL,
    AirlineId INT NOT NULL,
    PilotId INT NOT NULL,
    CrewId INT NOT NULL,
    GroundStaffId INT NOT NULL,
    BoardGate INT NOT NULL,
    BoardTime DATE NOT NULL,
    DepartTime DATE NOT NULL,
    Duration INTERVAL DAY TO SECOND NOT NULL,
    DepAirport VARCHAR2(3) NOT NULL,
    ArrAirport VARCHAR2(3) NOT NULL,
    PRIMARY KEY (FlightId),
    FOREIGN KEY (AirlineId) REFERENCES AIRLINES (AirlineId),
    FOREIGN KEY (PilotId) REFERENCES EMPLOYEE (EmployeeId),
    FOREIGN KEY (CrewId) REFERENCES EMPLOYEE (EmployeeId),
    FOREIGN KEY (GroundStaffId) REFERENCES EMPLOYEE (EmployeeId),
    CONSTRAINT check_TUS_airport CHECK (DepAirport = 'TUS' OR ArrAirport = 'TUS')
);
CREATE SEQUENCE FLIGHT_SEQ START WITH 1 INCREMENT BY 1;


-- Create HISTORY table
CREATE TABLE HISTORY (
    CusId INT NOT NULL,
    FlightId INT NOT NULL,
    LuggageCount INT NOT NULL,
    FoodOrderCount INT NOT NULL,
    PRIMARY KEY (CusId, FlightId),
    FOREIGN KEY (CusId) REFERENCES CUSTOMER (CusId),
    FOREIGN KEY (FlightId) REFERENCES FLIGHT (FlightId)
);