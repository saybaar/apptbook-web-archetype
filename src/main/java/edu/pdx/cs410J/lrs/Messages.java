package edu.pdx.cs410J.lrs;

/**
 * Class for formatting messages on the server side.  This is mainly to enable
 * test methods that validate that the server returned expected strings.
 */
public class Messages
{

    public static String missingRequiredParameter( String parameterName )
    {
        return String.format("The required parameter \"%s\" is missing", parameterName);
    }

    public static String allMappingsDeleted() {
        return "All appointment books have been deleted";
    }

    public static String createdAppointment(String owner, Appointment appt) {
        return String.format( "Created appointment for %s: %s", owner, appt);
    }

    public static String badDateFormat() { return "Bad date format; expected - mm/dd/yyyy hh:mm xm"; }
}
