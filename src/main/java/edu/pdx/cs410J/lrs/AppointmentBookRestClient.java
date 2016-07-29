package edu.pdx.cs410J.lrs;

import com.google.common.annotations.VisibleForTesting;
import edu.pdx.cs410J.web.HttpRequestHelper;

import java.io.IOException;
import java.util.Date;
import static edu.pdx.cs410J.lrs.ApptBookUtilities.dumpDateTime;

/**
 * A helper class for accessing the rest client
 * TODO: The client should definitely capture and handle errors from the servlet.
 * TODO: Whether and how it passes those on to the main method is a design concern.
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
     * TODO: Do we need this at all if we can't pretty-print from the command line?
     */
    public Response getApptBookForOwner( String ownerName ) throws IOException
    {
        return get(this.url, "owner", ownerName);
    }

    public Response getApptsForOwnerAndSearch(String ownerName, Date startTime, Date endTime ) throws IOException
    {
        return get(this.url, "owner", ownerName, "beginTime", dumpDateTime(startTime), "endTime", dumpDateTime(endTime));
    }

    public Response addAppointment(String ownerName, String description, Date startTime, Date endTime ) throws IOException
    {
        return postToMyURL("owner", ownerName, "description", description, "beginTime", dumpDateTime(startTime),
                "endTime", dumpDateTime(endTime));
    }

    @VisibleForTesting
    Response postToMyURL(String... keysAndValues) throws IOException {
        return post(this.url, keysAndValues);
    }

    public Response removeAllMappings() throws IOException {
        return delete(this.url);
    }
}
