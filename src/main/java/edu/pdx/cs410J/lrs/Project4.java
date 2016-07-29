package edu.pdx.cs410J.lrs;

import edu.pdx.cs410J.web.HttpRequestHelper;

import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * The main class that parses the command line and communicates with the
 * Appointment Book server using REST.
 */
public class Project4 {

    public static final String MISSING_ARGS = "Missing command line arguments";
    enum mode {ADD, SEARCH}

    /**
     * Main method for project 4; does command-line parsing and date validation, makes a request to the server
     * if appropriate, and prints output if appropriate
     * @param args command-line arguments to parse
     */
    public static void main(String... args) {
        String hostName = null;
        String portString = null;
        String owner = null;
        String description = null;
        String beginTimeDate = null;
        String beginTimeTime = null;
        String beginTimeAMPM = null;
        String endTimeDate = null;
        String endTimeTime = null;
        String endTimeAMPM = null;

        mode activeMode = mode.ADD;
        boolean shouldPrint = false;

        int i = 0;
        for(; i < args.length && args[i].startsWith("-"); i++) {
            if (args[i].equals("-README")) {
                printReadMe();
                System.exit(0);
            } else if (args[i].equals("-host")) {
                try {
                    hostName = args[++i];
                } catch (ArrayIndexOutOfBoundsException e) {
                    usage(MISSING_ARGS);
                }
            } else if (args[i].equals("-port")) {
                try {
                    portString = args[++i];
                } catch (ArrayIndexOutOfBoundsException e) {
                    usage(MISSING_ARGS);
                }
            } else if (args[i].equals("-search")) {
                activeMode = mode.SEARCH;
            } else if (args[i].equals("-print")) {
                shouldPrint = true;
            } else {
                usage("Unrecognized flag: " + args[i]);
            }
        }
        for(; i < args.length; i++) {
            if(owner == null) {
                owner = args[i];
            } else if(description == null && activeMode == mode.ADD) {
                description = args[i];
            } else if(beginTimeDate == null) {
                beginTimeDate = args[i];
            } else if(beginTimeTime == null) {
                beginTimeTime = args[i];
            } else if(beginTimeAMPM == null) {
                beginTimeAMPM = args[i];
            } else if(endTimeDate == null) {
                endTimeDate = args[i];
            } else if(endTimeTime == null) {
                endTimeTime = args[i];
            } else if(endTimeAMPM == null) {
                endTimeAMPM = args[i];
            } else {
                usage("Too many options");
            }
        }

        //Check for wrong number of options:
        List<String> necessaryOptionsList = null;
        if(activeMode == mode.ADD) {
            necessaryOptionsList = Arrays.asList(owner, description, beginTimeDate, beginTimeTime,
                    beginTimeAMPM, endTimeDate, endTimeTime, endTimeAMPM);
        } else if(activeMode == mode.SEARCH) {
            necessaryOptionsList = Arrays.asList(owner, beginTimeDate, beginTimeTime,
                    beginTimeAMPM, endTimeDate, endTimeTime, endTimeAMPM);
        }
        for(String option : necessaryOptionsList) {
            if (option == null) {
                usage(MISSING_ARGS);
            }
        }

        //Check validity of date and time and convert to Date objects:
        Date beginDateTime = null;
        Date endDateTime = null;
        try {
            beginDateTime = ApptBookUtilities.parseDateTime(beginTimeDate + " " + beginTimeTime +
                    " " + beginTimeAMPM);
            endDateTime = ApptBookUtilities.parseDateTime(endTimeDate + " " + endTimeTime +
                    " " + endTimeAMPM);
        } catch (ParseException e) {
            usage("Invalid date/time format");
        }

        if ((hostName == null) != (portString == null)) {
            usage("Must specify both host and port, or neither");

        } else if ( hostName == null && portString == null) {
            //Project 1 behavior
            if(activeMode == mode.SEARCH) {
                usage("Must specify a host and port for search mode");
            } else {
                AppointmentBook apptBook = new AppointmentBook(owner);
                Appointment appt = new Appointment(description, beginDateTime, endDateTime);
                apptBook.addAppointment(appt);
                if(shouldPrint) {
                    System.out.println(appt.toString());
                }
                System.exit(0);
            }
        }

        int port;
        try {
            port = Integer.parseInt( portString );
            
        } catch (NumberFormatException ex) {
            usage("Port \"" + portString + "\" must be an integer");
            return;
        }

        AppointmentBookRestClient client = new AppointmentBookRestClient(hostName, port);

        HttpRequestHelper.Response response = null;
        try {
            if (activeMode == mode.ADD) {
                //Check for empty description:
                if(description.isEmpty()) {
                    usage("Description may not be empty when adding a new appointment");
                }
                response = client.addAppointment(owner, description, beginDateTime, endDateTime);
                checkResponseCode( HttpURLConnection.HTTP_OK, response);
                if(shouldPrint) {
                    System.out.println(response.getContent());
                }
            } else if (activeMode == mode.SEARCH) {
                response = client.searchForAppointments(owner, beginDateTime, endDateTime);
                checkResponseCode( HttpURLConnection.HTTP_OK, response);
                System.out.println(response.getContent());
            } else {
                error("Unrecognized mode - we should never be here - bad news bears!!");
            }


        } catch ( IOException ex ) {
            error("While contacting server: " + ex);
            return;
        }

        System.exit(0);
    }

    /**
     * Prints README for project 4
     */
    private static void printReadMe() {
        System.out.print("\n\nLydia Simmons - Advanced Java Project 4\n\n" +
                "This program connects to a servlet and either adds an appointment\n" +
                "or searches for existing appointments between two times. \n\n" +
                getUsageString() + "\n\n");
    }

    /**
     * Makes sure that the give response has the expected HTTP status code
     * @param code The expected status code
     * @param response The response from the server
     */
    private static void checkResponseCode( int code, HttpRequestHelper.Response response )
    {
        if (response.getCode() != code) {
            error(String.format("Expected HTTP code %d, got code %d.\n\n%s", code,
                                response.getCode(), response.getContent()));
        }
    }

    /**
     * Prints an error and exits
     * @param message Error message to print
     */
    private static void error( String message )
    {
        PrintStream err = System.err;
        err.println("** " + message);

        System.exit(1);
    }

    /**
     * Prints usage information for this program and exits
     * @param message An error message to print
     */
    private static void usage( String message )
    {
        PrintStream err = System.err;
        err.println("** " + message);
        err.println();
        err.println(getUsageString());
        err.println();

        System.exit(1);
    }

    /**
     * Returns a string that specifies parameters and usage. Used for error feedback and README.
     * @return Usage string
     */
    private static String getUsageString() {
        StringBuilder sb = new StringBuilder();

        sb.append("usage: [-host hostname] [-port portname] [-README] [-print] [-search] owner description beginTime endTime\n");
        sb.append("  hostname    Host of web server\n");
        sb.append("  portname    Port of web server\n");
        sb.append("  owner       Appointment book owner\n");
        sb.append("  description Description (for new appointment only)\n");
        sb.append("  beginTime   Start time (mm/dd/yyyy hh:mm xm)\n");
        sb.append("  endTime     End time (mm/dd/yyyy hh:mm xm)\n");
        sb.append("\n");
        sb.append("If -search is enabled, only owner, beginTime, and endTime are expected.\n");
        sb.append("If -host and -port are omitted, the program will not search but will\n");
        sb.append("create an appointment without saving it anywhere. \n");
        sb.append("If -print is enabled, the newly created appointment will be printed out.\n");

        return sb.toString();
    }
}