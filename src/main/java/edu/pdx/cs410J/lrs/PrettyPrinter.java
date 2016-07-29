package edu.pdx.cs410J.lrs;

import edu.pdx.cs410J.AbstractAppointmentBook;
import edu.pdx.cs410J.AppointmentBookDumper;

import java.io.*;

/**
 * Class for dumping appointment book files
 */
public class PrettyPrinter implements AppointmentBookDumper {

    private PrintWriter pw;

    /**
     * Creates a new TextDumper that will write to the given file name/path.
     * @param pw The (relative) filepath to write to
     */
    public PrettyPrinter(PrintWriter pw) {
        this.pw = pw;
    }

    /**
     * Pretty-prints the given AppointmentBook to file at the location given by the PrettyPrinter's filePath string.
     * If that path is "-", prints to stdout instead of a file.
     * @param apptBook The appointment book to dump
     * @throws IOException if something goes wrong while writing
     */
    @Override
    public void dump(AbstractAppointmentBook apptBook) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(pw)) {
            bw.write(dumpString(apptBook));
            bw.flush();
        } catch (IOException e) {
            throw new IOException("IOException while pretty-printing: " + e.getMessage());
        }
    }

    /**
     * Dumps an AppointmentBook to a pretty-printed String, ready to be output to a file or stdout.
     * @param apptBook The appointment book to pretty-print.
     * @return The pretty-printed output as a String.
     */
    private String dumpString(AbstractAppointmentBook apptBook) {
        StringBuilder sb = new StringBuilder();
        sb.append("Appointment book for " + apptBook.getOwnerName() + ":");
        for(Appointment appt : ((AppointmentBook) apptBook).getAppointments()) {
            sb.append("\n\t - ");
            sb.append(appt.getDescription() + "\n\t\t");
            sb.append(ApptBookUtilities.prettyDateTime(appt.getBeginTime()) + " to ");
            sb.append(ApptBookUtilities.prettyDateTime(appt.getEndTime()) + "\n");
            sb.append("\t\t(" + appt.getDurationInMinutes() + " minutes)");
        }
        return sb.toString();
    }

}
