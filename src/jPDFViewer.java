import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import javax.swing.*;
import java.awt.*;
import java.awt.desktop.AboutHandler;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

/**
 * This is a basic Java PDF Viewer (nothing to advanced); some feature will be added in the next
 * release.
 *
 * @author Abdon Morales (abdonm@cs.utexas.edu)
 * @version 1.0
 */
public class jPDFViewer extends JFrame {
    private PDDocument document;
    private PDFRenderer renderer;
    private int currentPage = 0;
    private double zoomFactor = 1.0;
    private JLabel label;

    public jPDFViewer() {
        initComponents();
        initMacOSAboutMenu();
    }

    private void initComponents() {
        setTitle("Java PDF Viewer 1.0");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setJMenuBar(createMenuBar());

        label = new JLabel();
        JScrollPane scrollPane = new JScrollPane(label);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(e -> openFile());
        fileMenu.add(openItem);

        JMenuItem printItem = new JMenuItem("Print");
        printItem.addActionListener(e -> printDocument());
        fileMenu.add(printItem);

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // View menu
        JMenu viewMenu = new JMenu("View");

        JMenuItem zoomInItem = new JMenuItem("Zoom in");
        zoomInItem.addActionListener(e -> adjustZoom(1.25));
        viewMenu.add(zoomInItem);

        JMenuItem zoomOutItem = new JMenuItem("Zoom out");
        zoomOutItem.addActionListener(e -> adjustZoom(0.8));
        viewMenu.add(zoomOutItem);

        JMenuItem setZoomItem = new JMenuItem("Set Zoom...");
        setZoomItem.addActionListener(e -> setZoom());
        viewMenu.add(setZoomItem);

        // Tools menu (implement this dead code in 1.1)
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem annotateItem = new JMenuItem("Annotate");

        JMenuItem highligthItem = new JMenuItem("Highlight");

        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        return menuBar;
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                if (document != null) {
                    document.close();
                }
                document = Loader.loadPDF(file);
                renderer = new PDFRenderer(document);
                displayPage(0);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void printDocument() {
        if (document != null) {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(new PDFPrintable(document, Scaling.ACTUAL_SIZE));
            boolean doPrint = job.printDialog();
            if (doPrint) {
                try {
                    job.print();
                } catch (PrinterException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void adjustZoom(double factor) {
        zoomFactor *= factor;
        displayPage(currentPage);
    }

    private void setZoom() {
        String result = JOptionPane.showInputDialog(this, "Enter zoom " +
                "percentage:", (int) (zoomFactor * 100));
        try {
            zoomFactor = Double.parseDouble(result) / 100.0;
            displayPage(currentPage);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format!");
        }
    }

    private void displayPage(int pageIndex) {
        try {
            BufferedImage image = renderer.renderImageWithDPI(pageIndex, (int) (150 * zoomFactor),
                    ImageType.RGB);
            label.setIcon(new ImageIcon(image));
            currentPage = pageIndex;
            revalidate();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initMacOSAboutMenu() {
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
                desktop.setAboutHandler(new AboutHandler() {
                    @Override
                    public void handleAbout(java.awt.desktop.AboutEvent e) {
                        JOptionPane.showMessageDialog(
                                null,
                                """
                                        Java PDF Viewer
                                        Version 1.0
                                        Released May 19, 2024
                                        Copyright (C) 2024 Morales Research \
                                        Technology Inc
                                        Copyright (C) 1992 - 2008 Sun Microsystems Inc.
                                        """,
                                "About Java PDF Viewer",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                });
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("apple.awt.application.name", "Java PDF Viewer");
        SwingUtilities.invokeLater(() -> {
            jPDFViewer viewer = new jPDFViewer();
            viewer.setVisible(true);
        });
    }
}