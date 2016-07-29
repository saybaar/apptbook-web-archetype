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
 * //TODO Error to not specify host and port? No - just revert to project 1 behavior
 */
public class Project4 {

    public static final String MISSING_ARGS = "Missing command line arguments";
    enum mode {ADD, SEARCH};

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
                    error("-host flag requires a value");
                }
            } else if (args[i].equals("-port")) {
                try {
                    portString = args[++i];
                } catch (ArrayIndexOutOfBoundsException e) {
                    error("-port flag requires a value");
                }
            } else if (args[i].equals("-search")) {
                activeMode = mode.SEARCH;
            } else if (args[i].equals("-print")) {
                shouldPrint = true;
            } else {
                error("Unrecognized flag: " + args[i]);
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
                System.err.println("Too many options; expected: owner [description] beginTime endTime");
                System.exit(1);
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
                error("Too few options; expected: owner [description] beginTime endTime");
            }
        }

        //Check for empty description:
        if(description.isEmpty()) {
            error("Description may not be empty");
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
            error("Invalid date/time format; expected: mm/dd/yyyy hh:mm xm");
        }

        if (hostName == null) {
            usage( MISSING_ARGS );

        } else if ( portString == null) {
            usage( "Missing port" );
        }

        int port;
        try {
            port = Integer.parseInt( portString );
            
        } catch (NumberFormatException ex) {
            usage("Port \"" + portString + "\" must be an integer");
            return;
        }

        AppointmentBookRestClient client = new AppointmentBookRestClient(hostName, port);

        HttpRequestHelper.Response response;
        try {
            if (activeMode == mode.ADD) {
                response = client.addAppointment(owner, description, beginDateTime, endDateTime);

            } else if (activeMode == mode.SEARCH) {
                response = client.getApptsForOwnerAndSearch(owner, beginDateTime, endDateTime);

            } else {
                // Post the owner/description pair
                response = client.getApptBookForOwner(owner);
            }

            checkResponseCode( HttpURLConnection.HTTP_OK, response);

        } catch ( IOException ex ) {
            error("While contacting server: " + ex);
            return;
        }

        System.out.println(response.getContent());

        System.exit(0);
    }


    private static void printReadMe() {
        //TODO!
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
        err.println("usage: java Project4 host port [key] [value]");
        err.println("  host    Host of web server");
        err.println("  port    Port of web server");
        err.println("  key     Key to query");
        err.println("  value   Value to add to server");
        err.println();
        err.println("This simple program posts key/value pairs to the server");
        err.println("If no value is specified, then all values are printed");
        err.println("If no key is specified, all key/value pairs are printed");
        err.println();

        System.exit(1);
    }
}