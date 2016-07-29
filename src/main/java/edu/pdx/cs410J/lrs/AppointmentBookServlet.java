package edu.pdx.cs410J.lrs;

import com.google.common.annotations.VisibleForTesting;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.*;

/**
 * This servlet ultimately provides a REST API for working with an
 * <code>AppointmentBook</code>.  However, in its current state, it is an example
 * of how to use HTTP and Java servlets to store simple key/value pairs.
 * //TODO: Validate all the request parameters!!
 */
public class AppointmentBookServlet extends HttpServlet
{
    private final Map<String, AppointmentBook> apptBookCatalog = new HashMap<>();

    /**
     * Handles an HTTP GET request from a client by writing the value of the key
     * specified in the "key" HTTP parameter to the HTTP response.  If the "key"
     * parameter is not specified, all of the key/value pairs are written to the
     * HTTP response.
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        response.setContentType( "text/plain" );

        String owner = getParameter( "owner", request );
        if (owner == null) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String beginTime = getParameter ( "beginTime", request );
        String endTime = getParameter ( "endTime", request );

        if((beginTime != null) && (endTime != null)) {
            try {
                prettyPrint(searchForAppointments(owner, beginTime, endTime), response.getWriter());
            } catch (ParseException e) {
                badDateFormat(response);
                return;
            }
        } else if((beginTime == null) && (endTime == null)) {
            AppointmentBook book = getAppointmentBook(owner);
            prettyPrint(book, response.getWriter());
        } else {
            missingRequiredParameter(response, "beginTime and endTime must both be defined");
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    private AppointmentBook searchForAppointments(String owner, String beginTimeString, String endTimeString) throws ParseException {
        Date beginTime = null;
        Date endTime = null;
        beginTime = ApptBookUtilities.parseDateTime(beginTimeString);
        endTime = ApptBookUtilities.parseDateTime(endTimeString);
        AppointmentBook results = new AppointmentBook(owner + " between " + beginTimeString + " and " + endTimeString);
        for(Appointment appt : getAppointmentBook(owner).getAppointments()) {
            if(appt.getBeginTime().compareTo(beginTime) >= 0
                && appt.getEndTime().compareTo(endTime) <= 0) {
                results.addAppointment(appt);
            }
        }
        return results;
    }

    private AppointmentBook getAppointmentBook(String owner) {
        return apptBookCatalog.get(owner);
    }

    private void prettyPrint(AppointmentBook apptBook, PrintWriter pw) throws IOException {
        if(apptBook == null) {
            pw.write("No appointment book with that owner. To create one, post an appointment.");
        } else {
            PrettyPrinter pp = new PrettyPrinter(pw);
            pp.dump(apptBook);
        }
    }

    /**
     * Handles an HTTP POST request by storing the key/value pair specified by the
     * "key" and "value" request parameters.  It writes the key/value pair to the
     * HTTP response.
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        response.setContentType( "text/plain" );

        String owner = getParameter( "owner", request );
        if (owner == null) {
            missingRequiredParameter(response, "owner");
            return;
        }
        String description = getParameter( "description", request );
        if ( description == null) {
            missingRequiredParameter( response, "description" );
            return;
        }
        String beginTimeString = getParameter( "beginTime", request );
        if ( beginTimeString == null) {
            missingRequiredParameter( response, "beginTime" );
            return;
        }
        String endTimeString = getParameter( "endTime", request );
        if ( endTimeString == null) {
            missingRequiredParameter( response, "endTime" );
            return;
        }

        AppointmentBook apptBook;
        if(apptBookCatalog.containsKey(owner)) {
            apptBook = apptBookCatalog.get(owner);
        } else {
            apptBook = new AppointmentBook(owner);
            apptBookCatalog.put(owner, apptBook);
        }

        Date beginTime = null;
        Date endTime = null;
        try {
            beginTime = ApptBookUtilities.parseDateTime(beginTimeString);
            endTime = ApptBookUtilities.parseDateTime(endTimeString);
        } catch (ParseException e) {
            badDateFormat(response);
            return;
        }

        Appointment appt = new Appointment(description, beginTime, endTime);
        apptBook.addAppointment(appt);

        PrintWriter pw = response.getWriter();
        pw.println(Messages.createdAppointment(owner, appt));
        pw.flush();

        response.setStatus( HttpServletResponse.SC_OK);
    }

    /**
     * Handles an HTTP DELETE request by removing all key/value pairs.  This
     * behavior is exposed for testing purposes only.  It's probably not
     * something that you'd want a real application to expose.
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");

        this.apptBookCatalog.clear();

        PrintWriter pw = response.getWriter();
        pw.println(Messages.allMappingsDeleted());
        pw.flush();

        response.setStatus(HttpServletResponse.SC_OK);

    }

    /**
     * Writes an error message about a missing parameter to the HTTP response.
     *
     * The text of the error message is created by {@link Messages#missingRequiredParameter(String)}
     */
    private void missingRequiredParameter( HttpServletResponse response, String parameterName )
        throws IOException
    {
        String message = Messages.missingRequiredParameter(parameterName);
        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, message);
    }

    /**
     * Writes an error message about a bad date format to the HTTP response.
     *
     * The text of the error message is created by {@link Messages#badDateFormat()}
     */
    private void badDateFormat( HttpServletResponse response )
            throws IOException
    {
        String message = Messages.badDateFormat();
        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, message);
    }
    /**
     * Writes the value of the given key to the HTTP response.
     *
     * The text of the message is formatted with {@link Messages#getMappingCount(int)}
     * and {@link Messages#formatKeyValuePair(String, String)}
     */
    /*
    private void writeValue( String key, HttpServletResponse response ) throws IOException
    {
        AppointmentBook value = this.apptBookCatalog.get(key);

        PrintWriter pw = response.getWriter();
        pw.println(Messages.getMappingCount( value != null ? 1 : 0 ));
        pw.println(Messages.formatKeyValuePair(key, "test"));

        pw.flush();

        response.setStatus( HttpServletResponse.SC_OK );
    }
    */
    /**
     * Writes all of the key/value pairs to the HTTP response.
     *
     * The text of the message is formatted with
     * {@link Messages#formatKeyValuePair(String, String)}
     */
    /*
    private void writeAllMappings( HttpServletResponse response ) throws IOException
    {
        PrintWriter pw = response.getWriter();
        pw.println(Messages.getMappingCount(apptBookCatalog.size()));

        for (Map.Entry<String, AppointmentBook> entry : this.apptBookCatalog.entrySet()) {
            pw.println(Messages.formatKeyValuePair(entry.getKey(), entry.getValue().getOwnerName()));
        }

        pw.flush();

        response.setStatus( HttpServletResponse.SC_OK );
    }
*/
    /**
     * Returns the value of the HTTP request parameter with the given name.
     *
     * @return <code>null</code> if the value of the parameter is
     *         <code>null</code> or is the empty string
     */
    private String getParameter(String name, HttpServletRequest request) {
      String value = request.getParameter(name);
      if (value == null || "".equals(value)) {
        return null;

      } else {
        return value;
      }
    }

    @VisibleForTesting
    void setValueForKey(String key, String value) {
        this.apptBookCatalog.put(key, new AppointmentBook("test"));
    }

    @VisibleForTesting
    String getValueForKey(String key) {
        return this.apptBookCatalog.get(key).getOwnerName();
    }
}
