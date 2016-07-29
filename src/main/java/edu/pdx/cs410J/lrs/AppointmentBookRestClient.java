package edu.pdx.cs410J.lrs;

import com.google.common.annotations.VisibleForTesting;
import edu.pdx.cs410J.web.HttpRequestHelper;

import java.io.IOException;
import java.util.Date;
import static edu.pdx.cs410J.lrs.ApptBookUtilities.dumpDateTime;

/**
 * A helper class for accessing the rest client
 */
public class AppointmentBookRestClient extends HttpRequestHelper
{
    private static final String WEB_APP = "apptbook";
    private static final String SERVLET = "appointments";

    private final String url;

    /**
     * Creates a client to the appointment book REST service running on the given host and port
     * @param hostName The name of the host
     * @param port The port
     */
    public AppointmentBookRestClient( String hostName, int port )
    {
        this.url = String.format( "http://%s:%d/%s/%s", hostName, port, WEB_APP, SERVLET );
    }

    /**
     * Sends a GET to the url, specifying owner, beginTime, and endTime; servlet will interpret this as a search
     * @param ownerName Owner whose appointments to search
     * @param startTime Start date for search
     * @param endTime End date for search
     * @return Servlet's response
     * @throws IOException if server throws one
     */
    public Response searchForAppointments(String ownerName, Date startTime, Date endTime ) throws IOException
    {
        return get(this.url, "owner", ownerName, "beginTime", dumpDateTime(startTime), "endTime", dumpDateTime(endTime));
    }

    /**
     * Sends a POST to the url, specifying owner, description, beginTime, and endTime; servlet will interpret this as
     * a new appointment to be added
     * @param ownerName Appointment's owner
     * @param description Appointment's description
     * @param startTime Appointment's start date
     * @param endTime Appointment's end date
     * @return Servlet's response
     * @throws IOException if server throws one
     */
    public Response addAppointment(String ownerName, String description, Date startTime, Date endTime ) throws IOException
    {
        return postToMyURL("owner", ownerName, "description", description, "beginTime", dumpDateTime(startTime),
                "endTime", dumpDateTime(endTime));
    }

    /**
     * Utility method that posts the given parameters to the url
     * @param parameters String parameters to post
     * @return Servlet's response
     * @throws IOException if server throws one
     */
    @VisibleForTesting
    Response postToMyURL(String... parameters) throws IOException {
        return post(this.url, parameters);
    }

}
