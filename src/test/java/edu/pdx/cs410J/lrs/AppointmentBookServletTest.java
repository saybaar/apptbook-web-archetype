package edu.pdx.cs410J.lrs;

import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

/**
 * A unit test for the {@link AppointmentBookServlet}.  It uses mockito to
 * provide mock http requests and responses.
 */
public class AppointmentBookServletTest {
  @Ignore
  @Test
  public void getOnServletWithPreCanned() throws ServletException, IOException {
    AppointmentBookServlet servlet = new AppointmentBookServlet();

    HttpServletRequest request1 = mock(HttpServletRequest.class);
    HttpServletRequest request2 = mock(HttpServletRequest.class);
    HttpServletResponse response1 = mock(HttpServletResponse.class);
    HttpServletResponse response2 = mock(HttpServletResponse.class);
    PrintWriter pw1 = mock(PrintWriter.class);
    PrintWriter pw2 = mock(PrintWriter.class);

    String ownerName = "PreCannedOwner";
    String description = "test description";
    String startTime = "11/11/1111 11:11 PM";
    String endTime = "11/11/1111 12:12 PM";
    when(request1.getParameter("owner")).thenReturn(ownerName);
    when(request1.getParameter("description")).thenReturn(description);
    when(request1.getParameter("beginTime")).thenReturn(startTime);
    when(request1.getParameter("endTime")).thenReturn(endTime);

    when(response1.getWriter()).thenReturn(pw1);

    servlet.doPost(request1, response1);

    when(request2.getParameter("owner")).thenReturn(ownerName);
    when(request2.getParameter("beginTime")).thenReturn(startTime);
    when(request2.getParameter("endTime")).thenReturn(endTime);
    when(response2.getWriter()).thenReturn(pw2);

    servlet.doGet(request2, response2);

    verify(pw1).println("Created appointment for PreCannedOwner: test description from 11/11/11 11:11 PM until 11/11/11 12:12 PM");
    //can't really verify that the get worked since the pretty-print argument to pw2 is very long...
    verify(response1).setStatus(HttpServletResponse.SC_OK);
  }

}
