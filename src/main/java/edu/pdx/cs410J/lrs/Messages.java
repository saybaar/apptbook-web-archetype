package edu.pdx.cs410J.lrs;

/**
 * Class for formatting messages on the server side.  This is mainly to enable
 * test methods that validate that the server returned expected strings.
 */
public class Messages
{

    /**
     * Generates a message that a parameter is missing
     * @param parameterName The parameter in question
     * @return String message
     */
    public static String missingRequiredParameter( String parameterName )
    {
        return String.format("The required parameter \"%s\" is missing", parameterName);
    }

    /**
     * Message confirming that an appointment was created
     * @param owner Appointment owner
     * @param appt Appointment in question
     * @return String message
     */
    public static String createdAppointment(String owner, Appointment appt) {
        return String.format( "Created appointment for %s: %s", owner, appt);
    }

    /**
     * Message for bad date format in servlet
     * @return String message
     */
    public static String badDateFormat() { return "Bad date format; expected - mm/dd/yyyy hh:mm xm"; }

    /**
     * Message for no appointment book found for the given owner
     * @return String message
     */
    public static String noApptBookFound() { return "No appointment book with that owner. To create one, post an appointment."; }
}
