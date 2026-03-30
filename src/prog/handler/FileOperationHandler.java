package prog.handler;

import javax.swing.*;
import java.io.File;

/** File selection and basic file-reference holder for the UI workflow. */
public class FileOperationHandler {

    private File selectedFile;
    private File outputFile;

    public File browseForFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a file to compress or decompress");
        return chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION
                ? chooser.getSelectedFile() : null;
    }

    public boolean isFileSelected() {
        return selectedFile != null && selectedFile.exists();
    }

    public void showNoFileSelectedWarning() {
        JOptionPane.showMessageDialog(null,
                "Please select a file first.",
                "No File Selected", JOptionPane.WARNING_MESSAGE);
    }

    public File getSelectedFile()          { return selectedFile; }
    public void setSelectedFile(File f)    { this.selectedFile = f; }
    public File getOutputFile()            { return outputFile; }
    public void setOutputFile(File f)      { this.outputFile = f; }
}
