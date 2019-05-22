package sample;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import java.util.*;
import java.awt.Desktop;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.multipdf.LayerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
/**
 *
 * @author Dante
 */
public class pdf2pocketmod extends Application {

    public static ArrayList<BufferedImage> shrinkPages(float scale, PDDocument _doc){

        ArrayList<BufferedImage> pages = new ArrayList<>();

        try {
            for (int i=0;i<_doc.getNumberOfPages();i++){
                pages.add(shrinkPage(scale,_doc,i));
            }
        } catch(Exception e) { e.printStackTrace(); }

        return pages;

    }

    public static BufferedImage shrinkPage(float scale, PDDocument _doc, int i){
        BufferedImage bim = new BufferedImage(1,1,1);
        PDPage curPage = _doc.getPage(i);

        try {
            //final PDRectangle mediaBox = curPage.getMediaBox();
            //mediaBox.setUpperRightX(mediaBox.getUpperRightX() * scale);
            //mediaBox.setUpperRightY(mediaBox.getUpperRightY() * scale);
            //curPage.setMediaBox(mediaBox);
            PDFRenderer rend = new PDFRenderer(_doc);
            bim = rend.renderImage(i,scale);
        } catch(Exception e) { e.printStackTrace(); }
        return bim;
    }


    static double VERSION = 0.1;
    private Desktop desktop = Desktop.getDesktop();
    static boolean hasFile = false;
    static File importedFile;
    public static PDDocument doc;
    // static PDDocument doc = new PDDocument();

    Map<String, Double> widthMap = new HashMap<>();
    Map<String, Double> heightMap = new HashMap<>();
    //widthMap.add("A1",
    //LinkedHashSet<String> typesOfPaper = new LinkedHashSet(newLinkedHashSet("A1","A2","A3","A4","A5","B4","B5","Letter","Legal","Tabloid","Ledger","Statement","Executive","Folio","Quarto","Size10x14","Custom"));
    LinkedHashSet<String> typesOfPaper = new LinkedHashSet<>();

    public void addNewPaperType(String _name, double _width, double _height){
        widthMap.put(_name,_width);
        heightMap.put(_name, _height);
        typesOfPaper.add(_name);
    }



    static TextField tfWidth = new TextField("");
    static TextField tfHeight = new TextField("");


    public File getImportedFile(){
        return importedFile;
    }

    @Override
    public void start(Stage primaryStage) {


        addNewPaperType("Letter",8.5,11);

        //"A1","A2","A3","A4","A5","B4","B5","Letter","Legal"
        Button btnImport = new Button("Import PDF");
        //btnImport.setCursor(Cursor.HAND);
        final FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setSelectedExtensionFilter(extFilter);

        fileChooser.setTitle("Select PDF to Import");

        Label lblPath = new Label("Path:");
        Label lblReady = new Label("Ready:");
        Label lblReady2 = new Label("No");
        Label lblPathFull = new Label("...enter path or use Import File.");
        lblPathFull.setMinWidth(400);
        tfWidth.maxWidth(10);
        tfHeight.maxWidth(10);
        //
        Label lblPaper = new Label("Paper:");
        Label lblWidth = new Label("W:");
        enableCustomDimensions(false);
        Label lblHeight = new Label("H:");

        // Create a combo box for paper types
        ComboBox<String> cboPaper = new ComboBox<>(); // flagTitles
        cboPaper.getItems().addAll(typesOfPaper);
        cboPaper.setValue("Letter");
        //EventHandlers
        btnImport.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Looking for file to import...");
                importedFile = fileChooser.showOpenDialog(primaryStage);
                if (importedFile != null) {
                    lblPathFull.setText(importedFile.getAbsolutePath());
                    validatePDF(importedFile, doc);
                }
            }
        });
        //
        //HBox for paper
        //HBox paneForPaper = new HBox(5);
        GridPane paneForPaper = new GridPane();
        paneForPaper.setAlignment(Pos.CENTER);
        paneForPaper.setHgap(5.5);
        paneForPaper.setVgap(5.5);
        //Child nodes
        paneForPaper.add(lblPaper,0,0);
        paneForPaper.add(cboPaper,1,0);
        CheckBox cbOpen = new CheckBox("Open PDF on Export");
        CheckBox cbPageLines = new CheckBox("Draw Page Borders");
        CheckBox cbCutLines = new CheckBox("Draw Cut Lines");
        cbOpen.selectedProperty().setValue(true);
        cbOpen.selectedProperty().setValue(true);
        cbOpen.selectedProperty().setValue(true);

        paneForPaper.add(cbOpen,0,1);
        paneForPaper.add(cbPageLines,0,2);
        paneForPaper.add(cbCutLines,0,3);
        //paneForPaper.add(lblWidth,2,0);
        //paneForPaper.add(tfWidth,3,0);
        //paneForPaper.add(lblHeight,2,1);
        //paneForPaper.add(tfHeight,3,1);

        Button btnExport = new Button();
        btnExport.setText("Export PocketMod");
        //EventHandlers
        btnExport.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (hasFile()) {
                    logMessage("Attempting export...");
                    fileChooser.setTitle("Select output PDF file.");
                    String importedFileName = importedFile.getName();
                    //importedFileName.substring(0,importedFileName.lastIndexOf(".pdf"));
                    //String fileName = importedFileName.substring(0,importedFileName.length()-4)+"-PocketMod.pdf";
                    String fileName = importedFileName + "_pmod";
                    fileChooser.setInitialFileName(fileName);
                    File outputFile = fileChooser.showSaveDialog(primaryStage);
                    exportPDF(getImportedFile(), outputFile);}
                else {System.out.println("No file imported.  Please import a PDF.");}
            }
        });


        //

        //HBox for buttons
        HBox bottomHbox = new HBox(5);
        bottomHbox.setAlignment(Pos.CENTER);
        //Add buttons to HBOX
        bottomHbox.getChildren().add(btnImport);
        bottomHbox.getChildren().add(btnExport);
        //HBox bottomHbox = new HBox(2);
        HBox paneForPath = new HBox(5);
        paneForPath.getChildren().add(lblPath);
        paneForPath.getChildren().add(lblPathFull);
        //
        BorderPane pane = new BorderPane();
        pane.setBottom(bottomHbox);
        pane.setCenter(paneForPaper);
        pane.setTop(paneForPath);
        //StackPane root = new StackPane();
        //root.getChildren().add(bottomHbox);

        Scene scene = new Scene(pane, 450, 300);
        //
        primaryStage.setTitle("PDF to PocketMod");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException{
        launch(args);
    }

    private void openFile(File file) {
        try {
            desktop.open(file);
        } catch (IOException ex) {
            Logger.getLogger(
                    pdf2pocketmod.class.getName()).log(
                    Level.SEVERE, null, ex
            );
        }
    }
    //Utility method.
    //Because Java 8 can't do this for some reason??? Supposedly coming in the future
    public static Set<String> newLinkedHashSet(String... strings) {
        LinkedHashSet<String> set = new LinkedHashSet<>();

        for (String s : strings) {
            set.add(s);
        }
        return set;
    }

    public static void enableCustomDimensions(boolean state){

        tfWidth.setEditable(state);
        tfWidth.setDisable(!state);
        tfHeight.setEditable(state);
        tfHeight.setDisable(!state);

    }

    public static boolean validatePDF(File _file, PDDocument _doc){

        try
        {
            _doc = PDDocument.load(_file);
            int pages = _doc.getNumberOfPages();
            logMessage("There are "+pages+" pages.");
            if (pages>8) logMessage("Too many pages, can only have up to 8.");//MUst be 8 pages to work
                //PDFRenderer renderer = new PDFRenderer(doc);
            else {
                hasFile=true;
                logMessage("Imported PDF successfuly.");
            }
            _doc.close();
        }

        catch(Exception e){
            //Catch
            logMessage("There was an error importing the PDF.");
        }
        return false;
    }

    public static boolean exportPDF(File _file, File _outputFile){

        try
        {
            doc = PDDocument.load(_file);
            int pages = doc.getNumberOfPages();
            //MAKE NEW PAGE
            logMessage("There are "+pages+" pages.");
            //Shear off any remaining pages
            if (pages>8) {logMessage("Too many pages, truncating to 8.");}//MUst be 8 pages to work
            while (pages>8){
                doc.removePage(pages-1);
                pages--;
            }
            if (pages<8){
                logMessage("Padding out to 8 pages.");
                while (pages<8){
                    doc.addPage(new PDPage());
                    pages++;
                }
            }
            //Rotate pages
            PDDocument roDoc = reorderPages(doc);
            rotatePages(roDoc);
            ArrayList<BufferedImage> imgPages = shrinkPages(0.25f, doc);
            //
            int width = imgPages.get(0).getWidth()*2;
            int height = imgPages.get(0).getHeight()*4;
            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);


            //for (int i=0;i<doc.getNumberOfPages();i++){
            //    PDRectangle oframe = doc.getPage(i).getCropBox();
            //    doc.getPage(i).setCropBox(new PDRectangle(0,0,oframe.getWidth()/2,oframe.getHeight()/2));
            //}
            //Create the final document.
            PDDocument exportDoc = new PDDocument();
            //Take page one and put it at the end
            Vector allPages = new Vector();
            for (int i=0;i<doc.getNumberOfPages();i++){
                allPages.add(i,doc.getPage(i));
            }
            PDDocument workingDoc = new PDDocument();
            for (int i=0;i<allPages.size();i++){
                workingDoc.addPage((PDPage)allPages.remove(0));
            }


            PDDocument stitchDoc = new PDDocument();
            stitchDoc.addPage(combinePagesHorizontal(roDoc,0,1,true));
            stitchDoc.addPage(combinePagesHorizontal(roDoc,2,3,true));
            stitchDoc.addPage(combinePagesHorizontal(roDoc,4,5,true));
            stitchDoc.addPage(combinePagesHorizontal(roDoc,6,7,true));

            stitchDoc.addPage(combinePagesHorizontal(stitchDoc,0,1,true));
            stitchDoc.addPage(combinePagesHorizontal(stitchDoc,2,3,true));

            stitchDoc.addPage(combinePagesHorizontal(stitchDoc,5,4,false));

            //for (int i=0;i<stitchDoc.getNumberOfPages();i++){
            //    exportDoc.importPage(stitchDoc.getPage(i));
            //}

            exportDoc.importPage(stitchDoc.getPage(6));
            //Save to external file.
            stitchDoc.save(_outputFile);
            //exportDoc.save(_outputFile);
            doc.close();
            exportDoc.close();
            if (getOpenInSystemViewer()){
                Desktop.getDesktop().open(_outputFile);
            }
            logMessage("Saved output PDF to :");
        }

        catch(Exception e){
            //Catch
            logMessage("There was an error exporting the PDF.");
        }
        return false;
    }

    public static void logMessage(String string){
        System.out.println(string);
    }

    public static boolean getOpenInSystemViewer(){
        return true;
    }

    public static boolean hasFile(){
        if (hasFile) return true;
        else return false;
    }

    public static PDPage combinePagesHorizontal(PDDocument sourceDoc, int page1, int page2, boolean horizontal){

        PDDocument tempDoc = new PDDocument();
        PDRectangle outPdfFrame;

        PDRectangle pdf1Frame = sourceDoc.getPage(page1).getCropBox();
        PDRectangle pdf2Frame = sourceDoc.getPage(page2).getCropBox();
        if (horizontal){
            outPdfFrame = new PDRectangle(pdf1Frame.getWidth()+pdf2Frame.getWidth(), Math.max(pdf1Frame.getHeight(), pdf2Frame.getHeight()));
        }
        else {
            outPdfFrame = new PDRectangle(pdf1Frame.getWidth(), pdf2Frame.getHeight()+pdf1Frame.getHeight());
        }
        // Create output page with calculated frame and add it to the document
        COSDictionary dict = new COSDictionary();
        dict.setItem(COSName.TYPE, COSName.PAGE);
        dict.setItem(COSName.MEDIA_BOX, outPdfFrame);
        dict.setItem(COSName.CROP_BOX, outPdfFrame);
        dict.setItem(COSName.ART_BOX, outPdfFrame);
        PDPage outPdfPage = new PDPage(dict);
        try{
            // Source PDF pages has to be imported as form XObjects to be able to insert them at a specific point in the output page
            LayerUtility layerUtility = new LayerUtility(tempDoc);
            PDFormXObject formPdf1 = layerUtility.importPageAsForm(sourceDoc, page1);
            PDFormXObject formPdf2 = layerUtility.importPageAsForm(sourceDoc, page2);

            // Add form objects to output page
            AffineTransform afLeft = new AffineTransform();
            layerUtility.appendFormAsLayer(outPdfPage, formPdf1, afLeft, "left");

            if (horizontal){
                AffineTransform afRight = AffineTransform.getTranslateInstance(pdf1Frame.getWidth(), 0.0);
                layerUtility.appendFormAsLayer(outPdfPage, formPdf2, afRight, "right");
            }
            else {
                AffineTransform afRight = AffineTransform.getTranslateInstance(0.0, pdf1Frame.getHeight());
                layerUtility.appendFormAsLayer(outPdfPage, formPdf2, afRight, "right");
            }
        }
        catch (Exception e){
            logMessage("Something went wrong stitching pages.");
        }
        return outPdfPage;
    }

    public static PDDocument reorderPages(PDDocument _doc){
        PDDocument newDoc = new PDDocument();
        PDDocument oldDoc = _doc;
        PDPageTree allPages = oldDoc.getDocumentCatalog().getPages();

        //First four is pretty straightforward...
        newDoc.addPage(oldDoc.getPage(1));
        newDoc.addPage(oldDoc.getPage(2));
        newDoc.addPage(oldDoc.getPage(3));
        newDoc.addPage(oldDoc.getPage(4));
        //NOT SURE BRAIN HURTS
        newDoc.addPage(oldDoc.getPage(0));
        newDoc.addPage(oldDoc.getPage(7));
        newDoc.addPage(oldDoc.getPage(6));
        newDoc.addPage(oldDoc.getPage(5));

        return newDoc;
    }

    public static void rotatePages(PDDocument _doc){
        //Rotate pages
        for (int i=0;i<_doc.getNumberOfPages();i++){
            //if (i<4) {_doc.getPage(i).setRotation(90);}
            if (i>=4) {_doc.getPage(i).setRotation(180);}
            //PDRectangle oframe = _doc.getPage(i).getCropBox();
            //_doc.getPage(i).setCropBox(new PDRectangle(0,0,oframe.getWidth()/3,oframe.getHeight()/2));
        }
    }



    public static void getImportPDF(File file){

    }
}

