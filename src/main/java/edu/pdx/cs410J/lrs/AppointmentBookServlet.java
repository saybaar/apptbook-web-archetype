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
 * Servlet providing a REST API for adding to, printing, and searching AppointmentBooks.
 */
public class AppointmentBookServlet extends HttpServlet
{
    private final Map<String, AppointmentBook> apptBookCatalog = new HashMap<>();

    /**
     * Pulls parameters from the request and behaves according to which are present. If owner is not present, does
     * nothing; if only owner is present, pretty-prints the owner's appointment book; if owner, beginTime, and endTime
     * are present, displays search results for owner's appointments between those times.
     * @param request Request containing parameters
     * @param response Response to communicate result to
     * @throws ServletException if something goes wrong with the servlet
     * @throws IOException if something goes wrong with I/O
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        response.setContentType( "text/plain" );

        //If owner is undefined, do nothing
        String owner = getParameter( "owner", request );
        if (owner == null) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String beginTime = getParameter ( "beginTime", request );
        String endTime = getParameter ( "endTime", request );

        if((beginTime != null) && (endTime != null)) {
            //If beginTime and endTime are both defined, do a search
            try {
                AppointmentBook apptBook = searchForAppointments(owner, beginTime, endTime);
                if (apptBook == null) {
                    noApptBookForThatOwner(response);
                } else if (apptBook.getAppointments().isEmpty()) {
                    response.getWriter().println("No appointments found for " + owner + " between " +
                      beginTime + " and " + endTime);
                } else {
                    prettyPrint(apptBook, response.getWriter());
                }
            } catch (ParseException e) {
                badDateFormat(response);
                return;
            }
        } else if((beginTime == null) && (endTime == null)) {
            //If neither is defined, do a pretty-print of all appointments
            AppointmentBook book = getAppointmentBook(owner);
            if (book == null) {
                noApptBookForThatOwner(response);
            } else {
                prettyPrint(book, response.getWriter());
            }
        } else {
            missingRequiredParameter(response, "beginTime or endTime");
        }

        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Pulls parameters from the request and uses them to create a new Appointment in the owner's AppointmentBook,
     * creating a new AppointmentBook if the owner doesn't already have one.
     * @param request HttpServletRequest that must contain parameters owner, description, beginTime, and endTime.
     * @param response HttpServletResponse to communicate the result.
     * @throws ServletException if something goes wrong with the servlet
     * @throws IOException if something goes wrong with I/O
     */
    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        response.setContentType( "text/plain" );

        //For post, all parameters are required:
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

        //Validate date and time and convert back to Date objects:
        Date beginTime = null;
        Date endTime = null;
        try {
            beginTime = convertDate(beginTimeString);
            endTime = convertDate(endTimeString);
        } catch (ParseException e) {
            badDateFormat(response);
        }

        //Find owner's apptbook or create a new one:
        AppointmentBook apptBook;
        if(apptBookCatalog.containsKey(owner)) {
            apptBook = apptBookCatalog.get(owner);
        } else {
            apptBook = new AppointmentBook(owner);
            apptBookCatalog.put(owner, apptBook);
        }

        //Add the new appointment:
        Appointment appt = new Appointment(description, beginTime, endTime);
        apptBook.addAppointment(appt);

        //Print a success message:
        PrintWriter pw = response.getWriter();
        pw.println(Messages.createdAppointment(owner, appt));
        pw.flush();

        response.setStatus( HttpServletResponse.SC_OK);
    }

    /**
     * Handles a DELETE request by removing all the AppointmentBooks. Not used except for earlier testing.
     * @param request Servlet request received
     * @param response Response to communicate with
     * @throws ServletException if something goes wrong with the servlet
     * @throws IOException if something goes wrong with I/O
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");

        this.apptBookCatalog.clear();

        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Returns an AppointmentBook filled with appointments from owner's main appointment book that start at or after
     * begin time and end at or before end time. Returns null if the given owner has no main appointment book.
     *
     * @param owner Owner whose appointments to search
     * @param beginTimeString Begin time for search
     * @param endTimeString End time for search
     * @return null if owner has no appointment book to search; otherwise, AppointmentBook with result Appointments
     * @throws ParseException if date strings cannot be parsed
     */
    private AppointmentBook searchForAppointments(String owner, String beginTimeString, String endTimeString) throws ParseException {
        //Validate date and time and convert to Date objects:
        Date beginTime = ApptBookUtilities.parseDateTime(beginTimeString);
        Date endTime = ApptBookUtilities.parseDateTime(endTimeString);

        //Return null if owner has no appointment book to search:
        AppointmentBook apptBook = getAppointmentBook(owner);
        if(apptBook == null) {
            return null;
        }

        //Fill a new appointment book with the results:
        AppointmentBook results = new AppointmentBook(owner + " between " + beginTimeString + " and " + endTimeString);
        for(Appointment appt : getAppointmentBook(owner).getAppointments()) {
            if(appt.getBeginTime().compareTo(beginTime) >= 0
                && appt.getEndTime().compareTo(endTime) <= 0) {
                results.addAppointment(appt);
            }
        }
        return results;
    }

    /**
     * Gets the appointment book for the given owner
     * @param owner Owner name
     * @return Owner's appointment book
     */
    private AppointmentBook getAppointmentBook(String owner) {
        return apptBookCatalog.get(owner);
    }

    /**
     * Converts the given string to a Date
     * @param dateString String to convert
     * @return Converted Date object
     * @throws ParseException if date cannot be parsed
     */
    private Date convertDate(String dateString) throws ParseException {
        return ApptBookUtilities.parseDateTime(dateString);
    }

    /**
     * Attempts to pretty-print the given AppointmentBook. Returns false and does not print if the AppointmentBook
     * is null.
     * @param apptBook The AppointmentBook to print
     * @param pw The PrintWriter to print to
     * @throws IOException if something goes wrong while printing
     */
    private void prettyPrint(AppointmentBook apptBook, PrintWriter pw) throws IOException {
            PrettyPrinter pp = new PrettyPrinter(pw);
            pp.dump(apptBook);
    }

    /**
     * Writes an error message about a missing parameter to the HTTP response.
     * The text of the error message is created by {@link Messages#missingRequiredParameter(String)}
     * @param response Response to send the error to
     * @param parameterName Parameter which is missing
     * @throws IOException if something goes wrong with I/O
     */
    private void missingRequiredParameter( HttpServletResponse response, String parameterName )
        throws IOException
    {
        String message = Messages.missingRequiredParameter(parameterName);
        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, message);
    }

    /**
     * Writes an error message about a bad date format to the HTTP response.
     * The text of the error message is created by {@link Messages#badDateFormat()}
     * @param response Response to send the error to
     * @throws IOException if something goes wrong with I/O
     */
    private void badDateFormat( HttpServletResponse response )
            throws IOException
    {
        String message = Messages.badDateFormat();
        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, message);
    }

    /**
     * Writes an error message that no appointment book was found for an owner to the HTTP response.
     * The text of the error message is created by {@link Messages#noApptBookFound()}
     * @param response Response to send the error to
     * @throws IOException if something goes wrong with I/O
     */
    private void noApptBookForThatOwner( HttpServletResponse response )
            throws IOException
    {
        String message = Messages.noApptBookFound();
        response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED, message);
    }

    /**
     * Returns the value of the HTTP request parameter with the given name.
     * @param name Parameter name
     * @param request Request to pull from
     * @return <code>null</code> if the value of the parameter is
     *         <code>null</code> or is the empty string; String value otherwise
     */
    private String getParameter(String name, HttpServletRequest request) {
      String value = request.getParameter(name);
      if (value == null || "".equals(value)) {
        return null;

      } else {
        return value;
      }
    }

}
