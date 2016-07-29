package edu.pdx.cs410J.lrs;

import edu.pdx.cs410J.AbstractAppointmentBook;
import edu.pdx.cs410J.AppointmentBookDumper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class for dumping appointment book files
 */
public class TextDumper implements AppointmentBookDumper {

    private String filePath;

    /**
     * Creates a new TextDumper that will write to the given file name/path.
     * @param filePath The (relative) filepath to write to
     */
    public TextDumper(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Writes the given AppointmentBook to file at the location given by the TextDumper's filePath string.
     * Will generate a system error if filePath contains a directory that does not exist.
     * @param apptBook The appointment book to dump
     * @throws IOException
     */
    @Override
    public void dump(AbstractAppointmentBook apptBook) throws IOException {
        File file = new File(filePath);
        if(!file.isFile()) {
            file.createNewFile();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(apptBook.getOwnerName(), 0, apptBook.getOwnerName().length());
            for(Appointment appt : ((AppointmentBook) apptBook).getAppointments()) { //TODO: again, is this okay?
                StringBuilder sb = new StringBuilder();
                sb.append("\n\n");
                sb.append(appt.getDescription() + "\n");
                sb.append(ApptBookUtilities.dumpDateTime(appt.getBeginTime()) + "\n");
                sb.append(ApptBookUtilities.dumpDateTime(appt.getEndTime()));
                bw.write(sb.toString(), 0, sb.toString().length());
            }
        } catch (IOException e) {
            throw new IOException("IOException while dumping apptBook");
        }
    }
}
