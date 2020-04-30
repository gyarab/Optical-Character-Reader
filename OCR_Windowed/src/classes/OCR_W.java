/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;
import javax.swing.border.LineBorder;

/**
 *
 * @author Ivo Hanuš
 */
public class OCR_W extends javax.swing.JFrame {

    BufferedImage image;
    ArrayList<String> specialNames;

    /**
     * Metoda připravující vlastnosti komponent v okně
     */
    public OCR_W() {
        super("Optical Character Reader");      //Nastaví titulek okna
        initComponents();       //inicializuje části okna a jejich vlastnosti

        //Na začátku je vidět jen okno na přetažení obrázku
        instructionLabel.setVisible(false);
        colonLabel.setVisible(false);
        recognizedChar.setVisible(false);
        jRadioButton1.setVisible(false);
        jRadioButton2.setVisible(false);
        correctingTextField.setVisible(false);
        saveButton.setVisible(false);
        foldersButton.setVisible(false);

        handleComponents();
    }

    /**
     * Každé komponentě definuje reakční metody
     */
    private void handleComponents() {
        specialNames = new ArrayList();
        specialNames.add("dot");
        specialNames.add("comma");
        specialNames.add("semicolon");
        specialNames.add("colon");
        specialNames.add("understrike");
        specialNames.add("plus");
        specialNames.add("minus");
        specialNames.add("asterisk");
        specialNames.add("slash");
        specialNames.add("openBracket");
        specialNames.add("closedBracket");
        specialNames.add("exclamationMark");
        specialNames.add("questionMark");
        //Reakce na přetažení obrázku
        pictureLabel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {       //měl by vrátit true
                return true;        //povoluje import
            }

            /**
             * @param comp pole na přetažení obrázku
             * @param t přetažený obrázek
             * @return
             */
            @Override
            public boolean importData(JComponent comp, Transferable t) {        //do labelu přetažen soubor
                List<File> files = null;
                try {
                    files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                } catch (UnsupportedFlavorException | IOException ex) {
                    Logger.getLogger(OCR_W.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (files != null) {        //Zatím bez exceptiony
                    if (files.size() != 1) {
                        instructionLabel.setText("Upload one picture only!");

                    } else {
                        File imageFile = files.get(0);

                        //Zmenší pictureLabel, aby se do okna později vešly fileIndex další komponenty
                        if (!saveButton.isVisible()) {
                            pictureLabel.setSize(pictureLabel.getWidth(), pictureLabel.getHeight() - 30 - 11 - 47 - 19 + 2);
                            //30 je výška instructionLabelu, 11 výška layout gapu pod ním, 47 výška oněch dalších komponentů (nelze přes Button.getHeight() apod.), 19 je výška spodního layout gapu, 2 pixely se ztrácí při zobrazení oněch dalších komponentů
                        }

                        //Uložení obrázku do proměnné image
                        try {
                            image = ImageIO.read(imageFile);
                        } catch (IOException e) {
                            instructionLabel.setVisible(true);
                            instructionLabel.setText("Can't read that image!: " + e.toString());
                        }
                        if (image == null) {        //nastává při špatném (neobrázkovém) formátu
                            instructionLabel.setVisible(true);
                            instructionLabel.setText("Can't read that image: unknown format.");
                        } else {

                            //Pole se vzory
                            File[] UpperCaseExamples = new File(new File("").getAbsolutePath()
                                    + "\\src\\upperCase").listFiles();       //pole souborů ve složce upperCase
                            File[] LowerCaseExamples = new File(new File("").getAbsolutePath()
                                    + "\\src\\lowerCase").listFiles();       //pole souborů ve složce lowerCase
                            File[] SpecialCharsExamples = new File(new File("").getAbsolutePath()
                                    + "\\src\\specialChars").listFiles();       //pole souborů ve složce specialChars
                            //Sjednocení polí
                            File[] examples
                                    = new File[UpperCaseExamples.length
                                    + LowerCaseExamples.length
                                    + SpecialCharsExamples.length];
                            int fileIndex = 0;
                            for (File[] dir : new File[][]{UpperCaseExamples, LowerCaseExamples, SpecialCharsExamples}) {
                                for (File f : dir) {
                                    examples[fileIndex] = f;
                                    fileIndex++;
                                }
                            }

                            int imageWidth = image.getWidth();
                            int imageHeight = image.getHeight();

                            //Zvýšení kontrastu
                            int colorMin = 255;
                            int colorMax = 0;
                            int red, green, blue;
                            //zjištění nejmenších a nejvyšších hodnot jednotlivých složek
                            for (int x = 0; x < imageWidth; x++) {
                                for (int y = 0; y < imageHeight; y++) {
                                    red = new Color(image.getRGB(x, y)).getRed();
                                    if (colorMin > red) {
                                        colorMin = red;
                                    }
                                    if (colorMax < red) {
                                        colorMax = red;
                                    }
                                    green = new Color(image.getRGB(x, y)).getGreen();
                                    if (colorMin > green) {
                                        colorMin = green;
                                    }
                                    if (colorMax < green) {
                                        colorMax = green;
                                    }
                                    blue = new Color(image.getRGB(x, y)).getBlue();
                                    if (colorMin > blue) {
                                        colorMin = blue;
                                    }
                                    if (colorMax < blue) {
                                        colorMax = blue;
                                    }
                                }
                            }
                            //přebarvení - na světlejší/tmavší podle toho, kde v rozsahu hodnot složky RGB se na minulém obrázku nacházela (rozsah se rozšíří na 0-255)
                            for (int x = 0; x < imageWidth; x++) {
                                for (int y = 0; y < imageHeight; y++) {
                                    red = (int) ((float) (255 * (new Color(image.getRGB(x, y)).getRed() - colorMin))
                                            / (float) (colorMax - colorMin));
                                    green = (int) ((float) (255 * (new Color(image.getRGB(x, y)).getGreen() - colorMin))
                                            / (float) (colorMax - colorMin));
                                    blue = (int) ((float) (255 * (new Color(image.getRGB(x, y)).getBlue() - colorMin))
                                            / (float) (colorMax - colorMin));
                                    //jakou pozici zaujímala hodnota v rozmezí minBlue-maxBlue, takovou teď má zaujímat v rozmezí 0-256
                                    image.setRGB(x, y, -1 - 256 * 256 * (255 - red) - 256 * (255 - green) - (255 - blue));
                                }
                            }

                            colorMin = 255;
                            colorMax = 0;
                            int minX = 0;
                            int minY = 0;
                            int maxX = 0;
                            int maxY = 0;
                            for (int x = 0; x < imageWidth; x++) {
                                for (int y = 0; y < imageHeight; y++) {
                                    red = new Color(image.getRGB(x, y)).getRed();
                                    if (colorMin > red) {
                                        colorMin = red;
                                        minX = x;
                                        minY = y;
                                    }
                                    if (colorMax < red) {
                                        colorMax = red;
                                        maxX = x;
                                        maxY = y;
                                    }
                                    green = new Color(image.getRGB(x, y)).getGreen();
                                    if (colorMin > green) {
                                        colorMin = green;
                                        minX = x;
                                        minY = y;
                                    }
                                    if (colorMax < green) {
                                        colorMax = green;
                                        maxX = x;
                                        maxY = y;
                                    }
                                    blue = new Color(image.getRGB(x, y)).getBlue();
                                    if (colorMin > blue) {
                                        colorMin = blue;
                                        minX = x;
                                        minY = y;
                                    }
                                    if (colorMax < blue) {
                                        colorMax = blue;
                                        maxX = x;
                                        maxY = y;
                                    }
                                }
                            }

                            //Zarovnání na střed podle "těžiště" barev (vážený průměr souřadnic x a y, kde váhu představuje "tmavost" barvy)
                            //zjištění těžiště
                            long xSum = 0;
                            long ySum = 0;
                            long weight;
                            long weightSum = 0;
                            for (int x = 0; x < imageWidth; x++) {
                                for (int y = 0; y < imageHeight; y++) {
                                    weight = 3 * 255
                                            - new Color(image.getRGB(x, y)).getRed()
                                            - new Color(image.getRGB(x, y)).getGreen()
                                            - new Color(image.getRGB(x, y)).getBlue();      //černá - #000000 -> nejvyšší váha
                                    xSum += x * weight;
                                    ySum += y * weight;
                                    weightSum += weight;
                                }
                            }
                            int xCentre = (int) (xSum / weightSum);
                            int yCentre = (int) (ySum / weightSum);

                            //samotný posun - 4 dvojice for-cyklů s rozdílem pouze mezi sestupnými a vzestupnými indexy (aby se z obrázku nevytvořila "harmonika"
                            int xShiftedFrom;
                            int yShiftedFrom;

                            if (xCentre - imageWidth / 2 <= 0 && yCentre - imageHeight / 2 <= 0) {
                                for (int x = imageWidth - 1; x >= 0; x--) {
                                    for (int y = imageHeight - 1; y >= 0; y--) {
                                        xShiftedFrom = x + xCentre - imageWidth / 2;
                                        yShiftedFrom = y + yCentre - imageHeight / 2;
                                        if (xShiftedFrom >= 0 && xShiftedFrom < imageWidth && yShiftedFrom >= 0 && yShiftedFrom < imageHeight) {
                                            image.setRGB(x, y, image.getRGB(xShiftedFrom, yShiftedFrom));
                                        } else {
                                            image.setRGB(x, y, -1);
                                        }
                                    }
                                }
                            } else if (xCentre - imageWidth / 2 <= 0 && yCentre - imageHeight / 2 > 0) {
                                for (int x = imageWidth - 1; x >= 0; x--) {
                                    for (int y = 0; y < imageHeight; y++) {
                                        xShiftedFrom = x + xCentre - imageWidth / 2;
                                        yShiftedFrom = y + yCentre - imageHeight / 2;
                                        if (xShiftedFrom >= 0 && xShiftedFrom < imageWidth && yShiftedFrom >= 0 && yShiftedFrom < imageHeight) {
                                            image.setRGB(x, y, image.getRGB(xShiftedFrom, yShiftedFrom));
                                        } else {
                                            image.setRGB(x, y, -1);
                                        }
                                    }
                                }
                            } else if (xCentre - imageWidth / 2 > 0 && yCentre - imageHeight / 2 <= 0) {
                                for (int x = 0; x < imageWidth; x++) {
                                    for (int y = imageHeight - 1; y >= 0; y--) {
                                        xShiftedFrom = x + xCentre - imageWidth / 2;
                                        yShiftedFrom = y + yCentre - imageHeight / 2;
                                        if (xShiftedFrom >= 0 && xShiftedFrom < imageWidth && yShiftedFrom >= 0 && yShiftedFrom < imageHeight) {
                                            image.setRGB(x, y, image.getRGB(xShiftedFrom, yShiftedFrom));
                                        } else {
                                            image.setRGB(x, y, -1);
                                        }
                                    }
                                }
                            } else {
                                for (int x = 0; x < imageWidth; x++) {
                                    for (int y = 0; y < imageHeight; y++) {
                                        xShiftedFrom = x + xCentre - imageWidth / 2;
                                        yShiftedFrom = y + yCentre - imageHeight / 2;
                                        if (xShiftedFrom >= 0 && xShiftedFrom < imageWidth && yShiftedFrom >= 0 && yShiftedFrom < imageHeight) {
                                            image.setRGB(x, y, image.getRGB(xShiftedFrom, yShiftedFrom));
                                        } else {
                                            image.setRGB(x, y, -1);
                                        }
                                    }
                                }
                            }

                            //V poli se zobrazí importovaný posunutý obrázek
                            //Škálování - obrázek v okně vyžaduje jiné rozměry než má ten surový importovaný
                            Image imageOfIcon = image;
                            imageOfIcon = imageOfIcon.getScaledInstance(
                                    pictureLabel.getWidth() - 2 * ((LineBorder) pictureLabel.getBorder()).getThickness(),
                                    pictureLabel.getHeight() - 2 * ((LineBorder) pictureLabel.getBorder()).getThickness(),
                                    Image.SCALE_FAST);      // - 2 × šířka borders, aby se to mezi borders vešlo (při znovunahrávání obrázku)
                            ImageIcon icon = new ImageIcon(imageOfIcon);
                            pictureLabel.setIcon(icon);
                            pictureLabel.setText("");
                            //pictureLabel.setIcon(new ImageIcon(image));     //zobrazí posunutý obrázek

                            //Vyhodnocení podobnosti každého ze vzorů
                            int minDifference = imageWidth * imageHeight * 255;      //nejnižší spočtená hodnota rozdílnosti nějakého vzoru
                            String theMostSimilarChar = "#";       //nejpodobnější písmeno

                            for (File exampleF : examples) {
                                BufferedImage example = null;
                                try {
                                    example = ImageIO.read(exampleF);
                                } catch (IOException e) {
                                    System.out.println(e.toString());
                                }
                                if (example != null) {
                                    //každá ze složek RGB se vyhodnocuje zvlášť --> citlivost na barvy
                                    int exampleWidth = example.getWidth();
                                    int exampleHeight = example.getHeight();

                                    //výpočty pro 3 hodnoty rozdílnosti - pro každou složku RGB
                                    int[] differences = {0, 0, 0};
                                    for (int x = 0; x < imageWidth; x++) {
                                        for (int y = 0; y < imageHeight; y++) {

                                            //v případě rozdílných rozměrů vzoru a zkoumaného obrázku se některé pixely vynechávají (považováno za zanedbatelnou ztrátu přesnosti)
                                            int exampleXCoordinate = (int) (x * (float) exampleWidth / imageWidth);
                                            int exampleYCoordinate = (int) (y * (float) exampleHeight / imageHeight);
                                            differences[0] += Math.abs(new Color(image.getRGB(x, y)).getRed() - new Color(example.getRGB(exampleXCoordinate, exampleYCoordinate)).getRed());
                                            differences[1] += Math.abs(new Color(image.getRGB(x, y)).getGreen() - new Color(example.getRGB(exampleXCoordinate, exampleYCoordinate)).getGreen());
                                            differences[2] += Math.abs(new Color(image.getRGB(x, y)).getBlue() - new Color(example.getRGB(exampleXCoordinate, exampleYCoordinate)).getBlue());
                                        }
                                    }
                                    //zapamatování písmene, pokud je vzor nejpodobnější
                                    for (int difference : differences) {
                                        if (difference < minDifference) {
                                            minDifference = difference;
                                            if (exampleF.getName().charAt(0) == '_') {
                                                String specialName = exampleF.getName().split("-")[0].substring(1); //jméno souboru rozdělí na 2 části tam, kde je pomlčka, a vezme tu nultou, ze které vynechá první znak ("_")
                                                String[] specialChars = {".", ",", ";", ":", "_", "+", "-", "*", "/", "(", ")", "!", "?"};
                                                theMostSimilarChar = specialChars[specialNames.indexOf(specialName)];
                                            } else {
                                                theMostSimilarChar = Character.toString(exampleF.getName().charAt(0));        //první písmeno názvu vzoru musí být skutečným písmenem na obrázku vzoru
                                            }
                                        }
                                    }
                                }
                            }
                            //Vypíše výsledek
                            recognizedChar.setText(theMostSimilarChar);
                            instructionLabel.setText("Reading finished.");

                            //Odemnkne možnosti uložení
                            instructionLabel.setVisible(true);
                            colonLabel.setVisible(true);
                            recognizedChar.setVisible(true);
                            foldersButton.setVisible(true);
                            jRadioButton1.setVisible(true);
                            jRadioButton2.setVisible(true);
                            correctingTextField.setVisible(true);
                            saveButton.setVisible(true);
                            jRadioButton1.setEnabled(true);
                            jRadioButton2.setEnabled(true);

                            //Při přidávání více obrázků do databáze za jednoho zapnutí programu se někdy hodí nechat tlačítko "Save" dostupné
                            if (jRadioButton1.isSelected()) {
                                saveButton.setEnabled(true);
                            } else if (jRadioButton2.isSelected()) {
                                if (correctingTextField.getText().length() == 1
                                        && "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,;:_+-*/()!?".indexOf(correctingTextField.getText().charAt(0)) > -1) {      // vrácená -1 by indikovala nepřítomnost znaku ve vlákně
                                    saveButton.setEnabled(true);
                                }
                            }
                        }
                    }
                }
                return true; //super.importData(comp, t) generovali NB
            }
        }
        );     //Konec reakce na přetažení obrázku

        //Reakce na volbu 1 - správné rozpoznání
        jRadioButton1.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e
            ) {
                correctingTextField.setEnabled(false);
                saveButton.setEnabled(true);
            }
        }
        );

        //Reakce na volbu 2 - chybné rozpoznání
        jRadioButton2.addActionListener(
                (ActionEvent e) -> {
                    correctingTextField.setEnabled(true);
                    correctingTextField.setEditable(true);
                    if (correctingTextField.getText().length() == 1
                    && "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,;:_+-*/()!?".indexOf(correctingTextField.getText().charAt(0)) > -1) {      // vrácená -1 by indikovala nepřítomnost znaku ve vlákně
                        saveButton.setEnabled(true);
                    } else {
                        saveButton.setEnabled(false);
                    }
                }
        );

        //Reakce na text field v případě volby chybného rozpoznání
        correctingTextField.addActionListener(
                (ActionEvent e) -> {
                    if (correctingTextField.getText().length() == 1
                    && "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,;:_+-*/()!?".indexOf(correctingTextField.getText().charAt(0)) > -1) {      // vrácená -1 by indikovala nepřítomnost znaku ve vlákně
                        saveButton.setEnabled(true);
                    } else {
                        saveButton.setEnabled(false);
                    }
                }
        );

        //Reakce na tlačítko "Save"
        saveButton.addActionListener(
                (ActionEvent e) -> {
                    if (jRadioButton1.isSelected() || (correctingTextField.getText().length() == 1
                    && "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.,;:_+-*/()!?".indexOf(correctingTextField.getText().charAt(0)) > -1)) {      // vrácená -1 by indikovala nepřítomnost znaku ve vlákně

                        //Uložení obrázku do složky examples pod názvem ve formátu: písmeno/číslo, (nebo, v případě speciálních znaků, podtržítko, název znaku,) pomlčka a volné pořadové číslo (např. "a-12" nebo "_understrike-12"),
                        String exampleName = "";     //název souboru
                        //Klíčové písmeno názvu soboru
                        if (jRadioButton1.isSelected()) {
                            exampleName = recognizedChar.getText();
                        } else if (jRadioButton2.isSelected()) {
                            exampleName = String.valueOf(correctingTextField.getText().charAt(0));
                        }

                        //Načte soubory se vzory
                        String projectPath = new File("").getAbsolutePath();        //načítá cestu složky projektu
                        String examplesPath;
                        if ("0123456789.,;:_+-*/()!?".contains(exampleName)) {
                            examplesPath = "\\src\\specialChars";
                        } else if (exampleName.toLowerCase().equals(exampleName)) {
                            examplesPath = "\\src\\lowerCase";
                        } else {
                            examplesPath = "\\src\\upperCase";
                        }
                        File[] examples = new File(projectPath + examplesPath).listFiles();        //sjednocuje cestu v úplnou a vybere vzory v příslušné složce

                        //ověření, zda se nejedná o speciální znak a jeho případné pojmenování
                        int specialIndex = ".,;:_+-*/()!?".indexOf(exampleName);        //pokud se ve vlákně znak nenachází, vrátí -1
                        if (specialIndex > -1) {
                            exampleName = "_".concat(specialNames.get(specialIndex));
                        }

                        //Hledání volného pořadového čísla
                        boolean isTaken = true;
                        for (int i = 0; isTaken; i++) {
                            isTaken = false;

                            for (File f : examples) {
                                //kontrola, zda se tak už nějaký soubor nejmenuje
                                if (f.getName().substring(0, f.getName().length() - 4).equals(exampleName + "-" + String.valueOf(i))) {        //jména příkladů jsou ve tvaru "a-123.png"
                                    isTaken = true;
                                }
                            }

                            if (!isTaken) {     //pokud je název volný (není zabraný)
                                exampleName = exampleName.concat("-" + String.valueOf(i));
                                //Uložení importované image pod názvem exampleName-ČÍSLO do jedné ze složek
                                try {
                                    String restOfThePath;
                                    restOfThePath = examplesPath + "\\";
                                    String fileName = new File("").getAbsolutePath() + restOfThePath + exampleName + ".png";
                                    ImageIO.write(image, "png", new File(fileName));
                                    if (new File(fileName).exists()) {
                                        instructionLabel.setText("Succesfully saved as " + exampleName + ".png" + ".");
                                        saveButton.setEnabled(false);
                                    }

                                } catch (IOException ex) {
                                    Logger.getLogger(OCR_W.class.getName()).log(Level.SEVERE, null, ex);        //GNB
                                    instructionLabel.setText("Some exception has occured.");
                                }
                            }
                        }
                    } else {        //nezvolen jRadioButton1 (správné rozpoznání) a v correctingTextfieldu není právě jedno písmeno
                        saveButton.setEnabled(false);
                        instructionLabel.setText("Write only one letter!");
                    }
                }
        );

        //Reakce na tlačítko "Folders"
        foldersButton.addActionListener((ActionEvent e) -> {
            try {
                Desktop.getDesktop().open(new File(new File("").getAbsolutePath() + "\\src\\"));        //otevře složku projektu "src"
            } catch (IOException ex) {
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    //@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        pictureLabel = new javax.swing.JLabel();
        recognizedChar = new javax.swing.JTextField();
        colonLabel = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        correctingTextField = new javax.swing.JTextField();
        saveButton = new javax.swing.JButton();
        instructionLabel = new javax.swing.JLabel();
        foldersButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pictureLabel.setFont(new java.awt.Font("Verdana", 1, 18)); // NOI18N
        pictureLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        pictureLabel.setText("Drag and drop a picture here");
        pictureLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        recognizedChar.setEditable(false);
        recognizedChar.setBackground(new java.awt.Color(255, 255, 255));
        recognizedChar.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        recognizedChar.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        recognizedChar.setBorder(null);
        recognizedChar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recognizedCharActionPerformed(evt);
            }
        });

        colonLabel.setFont(new java.awt.Font("Verdana", 0, 14)); // NOI18N
        colonLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        colonLabel.setLabelFor(recognizedChar);
        colonLabel.setText("Recognized letter:");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("Save as this letter");
        jRadioButton1.setEnabled(false);

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("It is another letter:");
        jRadioButton2.setEnabled(false);

        correctingTextField.setEditable(false);
        correctingTextField.setEnabled(false);

        saveButton.setText("Save");
        saveButton.setEnabled(false);
        saveButton.setMaximumSize(new java.awt.Dimension(70, 23));
        saveButton.setMinimumSize(new java.awt.Dimension(70, 23));
        saveButton.setPreferredSize(new java.awt.Dimension(70, 23));

        instructionLabel.setFont(new java.awt.Font("Verdana", 0, 18)); // NOI18N
        instructionLabel.setForeground(new java.awt.Color(102, 102, 102));
        instructionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        foldersButton.setText("Folders");
        foldersButton.setMaximumSize(new java.awt.Dimension(78, 23));
        foldersButton.setMinimumSize(new java.awt.Dimension(78, 23));
        foldersButton.setPreferredSize(new java.awt.Dimension(78, 23));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(instructionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(foldersButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(pictureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(20, 20, 20))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(15, Short.MAX_VALUE)
                .addComponent(colonLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(recognizedChar, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButton1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jRadioButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(correctingTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(pictureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(instructionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 30, Short.MAX_VALUE)
                    .addComponent(foldersButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colonLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(recognizedChar, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButton2)
                            .addComponent(correctingTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(saveButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(19, 19, 19))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void recognizedCharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recognizedCharActionPerformed
    }//GEN-LAST:event_recognizedCharActionPerformed

    /**
     * Spustí aplikační okno.
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Sets the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(OCR_W.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(OCR_W.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(OCR_W.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(OCR_W.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>*/

        /* Creates and displays the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OCR_W().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel colonLabel;
    private javax.swing.JTextField correctingTextField;
    private javax.swing.JButton foldersButton;
    private javax.swing.JLabel instructionLabel;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JLabel pictureLabel;
    private javax.swing.JTextField recognizedChar;
    private javax.swing.JButton saveButton;
    // End of variables declaration//GEN-END:variables
}
