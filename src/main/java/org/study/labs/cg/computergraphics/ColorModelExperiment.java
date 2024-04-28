package org.study.labs.cg.computergraphics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

public class ColorModelExperiment extends Application {

    private double startX, startY, endX, endY;
    private double newLuminanceValue = 0.5;

    private BufferedImage originalImage;
    private ImageView imageView = new ImageView();

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        Button openButton = new Button("Open Image");
        openButton.setOnAction(this::openImage);
        openButton.setStyle("-fx-text-fill: white; -fx-background-color: #6c8bbd;");

        Button convertButton = new Button("Convert");
        convertButton.setOnAction(this::convertImage);
        convertButton.setStyle("-fx-text-fill: white; -fx-background-color: #6c8bbd;");

        HBox buttonsBox = new HBox(10, openButton, convertButton);
        buttonsBox.setAlignment(Pos.TOP_CENTER);
        buttonsBox.setPadding(new Insets(20));
        buttonsBox.setStyle("-fx-text-fill: white; -fx-background-color: linear-gradient(to top, #c0c9d6, #6182b6);");


        root.setTop(buttonsBox);
        root.setCenter(imageView);

        Scene scene = new Scene(root, 1000, 1000);
        primaryStage.setTitle("Color Model Experiment");
        primaryStage.setScene(scene);
        primaryStage.show();

        imageView.setOnMousePressed(this::handleMousePressed);
        imageView.setOnMouseReleased(this::handleMouseReleased);
    }

    private void handleMousePressed(MouseEvent event) {
        startX = event.getX();
        startY = event.getY();
    }

    private void handleMouseReleased(MouseEvent event) {
        endX = event.getX();
        endY = event.getY();

        // Обробка вибраного фрагменту
        handleSelectedFragment();
    }

    private void handleSelectedFragment() {
        double minX = Math.min(startX, endX);
        double minY = Math.min(startY, endY);
        double maxX = Math.max(startX, endX);
        double maxY = Math.max(startY, endY);

        int width = (int) (maxX - minX);
        int height = (int) (maxY - minY);


        // Оновлюємо зображення з оновленим прямокутником
        Image selectedFXImage = SwingFXUtils.toFXImage(originalImage, null);
        imageView.setImage(selectedFXImage);

        // Запит нового значення яскравості від користувача
        TextInputDialog dialog = new TextInputDialog(Double.toString(newLuminanceValue));
        dialog.setHeaderText("Enter New Luminance Value");
        dialog.setContentText("New Luminance Value (0 to 1):");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(value -> {
            try {
                double newLuminance = Double.parseDouble(value);
                if (newLuminance >= 0 && newLuminance <= 1) {
                    // Змінюємо яскравість жовтих пікселів у вибраному фрагменті
                    changeYellowLuminance(originalImage, newLuminance);
                    // Оновлюємо зображення з оновленим фрагментом
                    Image updatedFXImage = SwingFXUtils.toFXImage(originalImage, null);
                    imageView.setImage(updatedFXImage);
                } else {
                    // Виводимо повідомлення про неправильне значення яскравості
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid luminance value! Please enter a value between 0 and 1.");
                    alert.showAndWait();
                }
            } catch (NumberFormatException e) {
                // Виводимо повідомлення про неправильний формат введеного значення
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Invalid input! Please enter a valid number.");
                alert.showAndWait();
            }
        });
    }

    private void changeYellowLuminance(BufferedImage image, double newLuminance) {
        // Перевірка, що значення яскравості знаходиться в межах від 0 до 1
        newLuminance = Math.max(0, Math.min(1, newLuminance));

        // Прохід по кожному пікселю у вибраному фрагменті
        for (int y = (int) startY; y < endY; y++) {
            for (int x = (int) startX; x < endX; x++) {
                // Отримання кольору пікселя
                Color color = new Color(image.getRGB(x, y));

                // Перевірка, чи є цей піксель жовтим
                if (isYellow(color)) {
                    // Зміна яскравості жовтого пікселя
                    Color newColor = changeLuminance(color, newLuminance);

                    // Оновлення кольору пікселя у вибраному фрагменті
                    image.setRGB(x, y, newColor.getRGB());
                }
            }
        }
    }

    // Метод для перевірки, чи є кольор жовтим
    private boolean isYellow(Color color) {
        float[] hsl = rgbToHsl(color.getRed(), color.getGreen(), color.getBlue());
        // Жовтий колір має відтінок у межах 0.12-0.16 (або 30-60 градусів на колірному колесі HSL)
        return hsl[0] >= 0.12 && hsl[0] <= 0.16;
    }

    // Метод для зміни яскравості кольору
    private Color changeLuminance(Color color, double newLuminance) {
        // Отримання значень компонентів кольору
        float[] hsl = rgbToHsl(color.getRed(), color.getGreen(), color.getBlue());

        // Зміна значення яскравості
        hsl[2] = (float) newLuminance;

        // Повернення нового кольору
        return hslToRgb(hsl[0], hsl[1], hsl[2]);
    }


    private void openImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Image File");
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                originalImage = ImageIO.read(file);
                Image image = SwingFXUtils.toFXImage(originalImage, null);
                imageView.setImage(image);

                // Змінюємо розмір зображення до розмірів вікна
                imageView.setFitWidth(imageView.getScene().getWidth() - 300);
                imageView.setFitHeight(imageView.getScene().getHeight() - 300);
            } catch (IOException e) {
                throw new RuntimeException("Something goes wrong..");
            }
        }
    }

    private void convertImage(ActionEvent event) {
        if (originalImage == null) {
            System.out.println("Open an image first.");
            return;
        }

        // Convert image color model (RGB to CMYK)
        BufferedImage cmykImage = convertRGBtoCMYK(originalImage);

        // Convert image color model (RGB to HSL)
        BufferedImage hslImage = convertRGBtoHSL(originalImage);
        BufferedImage hslFromCMYK = convertCMYKtoHSL(cmykImage);

        // Отримуємо шлях для збереження конвертованих зображень
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Images");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG files", "*.png"),
                new FileChooser.ExtensionFilter("All files", "*.*")
        );
        File saveLocation = fileChooser.showSaveDialog(null);

        if (saveLocation != null) {
            // Зберігаємо конвертовані зображення за обраним шляхом
            saveImageToFile(hslFromCMYK, saveLocation.getAbsolutePath() + "_hslFromCmyk.png");
            saveImageToFile(cmykImage, saveLocation.getAbsolutePath() + "_cmyk.png");
            saveImageToFile(hslImage, saveLocation.getAbsolutePath() + "_hsl.png");

            saveColorValuesToFile(originalImage, cmykImage, hslImage, hslFromCMYK);
        }
    }


    private void saveImageToFile(BufferedImage image, String fileName) {
        File file = new File(fileName);
        try {
            ImageIO.write(image, "png", file);
            System.out.println("Converted image saved successfully: " + fileName);
        } catch (IOException e) {
            throw new RuntimeException("Something goes wrong..");
        }
    }

    private BufferedImage convertRGBtoCMYK(BufferedImage rgbImage) {
        int width = rgbImage.getWidth();
        int height = rgbImage.getHeight();

        BufferedImage cmykImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color rgbColor = new Color(rgbImage.getRGB(x, y));

                float red = rgbColor.getRed() / 255f;
                float green = rgbColor.getGreen() / 255f;
                float blue = rgbColor.getBlue() / 255f;

                float black = 1 - Math.max(Math.max(red, green), blue);

                float cyan = (1 - red - black) / (1 - black);
                float magenta = (1 - green - black) / (1 - black);
                float yellow = (1 - blue - black) / (1 - black);

                // Перетворення CMYK в RGB
                Color cmykToRgbColor = CMYKToRGB(cyan, magenta, yellow, black);

                // Створення об'єкту Color з RGB значень
                Color cmykColor = new Color(cmykToRgbColor.getRGB());

                cmykImage.setRGB(x, y, cmykColor.getRGB());
            }
        }
        return cmykImage;
    }


    private BufferedImage convertRGBtoHSL(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage hslImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Отримання кольору пікселя у форматі RGB
                Color rgbColor = new Color(image.getRGB(x, y));

                // Конвертація кольору з RGB в HSL
                float[] hsl = rgbToHsl(rgbColor.getRed(), rgbColor.getGreen(), rgbColor.getBlue());

                // Створення нового кольору у форматі HSL
                Color hslColor = hslToRgb(hsl[0], hsl[1], hsl[2]);

                // Встановлення кольору пікселя у зображенні HSL
                hslImage.setRGB(x, y, hslColor.getRGB());
            }
        }
        return hslImage;
    }

    private BufferedImage convertCMYKtoHSL(BufferedImage cmykImage) {
        BufferedImage rgbFromCmyk = convertRGBtoCMYK(cmykImage);
        return convertRGBtoHSL(rgbFromCmyk);
    }

    private BufferedImage convertHSLtoCMYK(BufferedImage hslImage) {
        BufferedImage rgbFromHSL = convertRGBtoCMYK(hslImage);
        return convertRGBtoHSL(rgbFromHSL);
    }


    public static Color CMYKToRGB(double c, double m, double y, double k) {
        int r = (int) (255.0 * (1.0 - c) * (1.0 - k));
        int g = (int) (255.0 * (1.0 - m) * (1.0 - k));
        int b = (int) (255.0 * (1.0 - y) * (1.0 - k));

        // Корекція кольору для запобігання виходу за межі допустимого діапазону
        r = Math.max(0, Math.min(255, r));
        g = Math.max(0, Math.min(255, g));
        b = Math.max(0, Math.min(255, b));

        return new Color(r, g, b);
    }

    public static float[] rgbToHsl(int r, int g, int b) {
        // Нормалізація значень RGB до діапазону [0, 1]
        float rf = r / 255f;
        float gf = g / 255f;
        float bf = b / 255f;

        // Знаходження максимального та мінімального значень
        float max = Math.max(Math.max(rf, gf), bf);
        float min = Math.min(Math.min(rf, gf), bf);

        // Визначення яскравості
        float l = (max + min) / 2;

        float h, s;
        if (max == min) {
            // Якщо max == min, то колір відтінка сірий
            h = s = 0;
        } else {
            // Визначення насиченості
            float d = max - min;
            s = l > 0.5f ? d / (2 - max - min) : d / (max + min);

            // Визначення відтінка
            if (max == rf) {
                h = (gf - bf) / d + (gf < bf ? 6 : 0);
            } else if (max == gf) {
                h = (bf - rf) / d + 2;
            } else {
                h = (rf - gf) / d + 4;
            }

            h /= 6;
        }

        return new float[]{h, s, l};
    }
    public static Color hslToRgb(float h, float s, float l) {
        double r, g, b;

        if (s == 0) {
            r = g = b = l; // ахроматичний колір
        } else {
            double q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            double p = 2 * l - q;
            r = hueToRgb(p, q, h + 1.0 / 3.0);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1.0 / 3.0);
        }

        return new Color((int) Math.round(r * 255), (int) Math.round(g * 255), (int) Math.round(b * 255));
    }

    private static double hueToRgb(double p, double q, double t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1 / 6.0) return p + (q - p) * 6 * t;
        if (t < 1 / 2.0) return q;
        if (t < 2 / 3.0) return p + (q - p) * (2 / 3.0 - t) * 6;
        return p;
    }

    private void saveColorValuesToFile(BufferedImage originalImage, BufferedImage cmykImage,
                                       BufferedImage hslImage, BufferedImage hslFromCMYK) {
        try {
            // Створення файлу для запису значень кольорів
            File file = new File("_color_values.txt");
            FileWriter writer = new FileWriter(file);

            // Запис значень кольорів для кожного пікселя у файл
            writeColorValues(writer, originalImage, cmykImage, hslImage, hslFromCMYK);

            writer.close();
            System.out.println("Color values saved successfully: " + "_color_values.txt");
        } catch (IOException e) {
            throw new RuntimeException("Failed to save color values.");
        }
    }

    private void writeColorValues(FileWriter writer, BufferedImage originalImage,
                                  BufferedImage cmykImage,BufferedImage hslImage, BufferedImage hslFromCMYK) throws IOException {

        writer.write("Original Image:\n");
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                // Отримання кольору пікселя
                Color color = new Color(originalImage.getRGB(x, y));
                // Запис значень кольору у файл
                writer.write(String.format("(%d, %d, %d) ",
                        color.getRed(),
                        color.getGreen(),
                        color.getBlue()));
            }
            writer.write("\n");
        }

        writer.write("\nCMYK Image:\n");
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                // Отримання кольору пікселя
                Color rgbColor = new Color(cmykImage.getRGB(x, y));

                float red = rgbColor.getRed() / 255f;
                float green = rgbColor.getGreen() / 255f;
                float blue = rgbColor.getBlue() / 255f;

                float black = 1 - Math.max(Math.max(red, green), blue);

                float cyan = (1 - red - black) / (1 - black);
                float magenta = (1 - green - black) / (1 - black);
                float yellow = (1 - blue - black) / (1 - black);

                // Запис значень кольору у файл
                writer.write(String.format("(%f, %f, %f, %f) ", cyan, magenta,
                        yellow, black));
            }
            writer.write("\n");
        }

        writer.write("\nHSL Image:\n");
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                // Отримання кольору пікселя
                Color color = new Color(hslImage.getRGB(x, y));

                float[] hslFromRgb =
                        rgbToHsl(color.getRed(), color.getGreen(), color.getBlue());

                // Запис значень кольору у файл
                writer.write(String.format("(%f, %f, %f) ",
                        hslFromRgb[0],
                        hslFromRgb[1],
                        hslFromRgb[2]));
            }
            writer.write("\n");
        }

        writer.write("\nHSL From CMYK Image:\n");
        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                // Отримання кольору пікселя
                Color color = new Color(hslFromCMYK.getRGB(x, y));

                float[] hslFromRgb =
                        rgbToHsl(color.getRed(), color.getGreen(), color.getBlue());

                // Запис значень кольору у файл
                writer.write(String.format("(%f, %f, %f) ", hslFromRgb[0], hslFromRgb[1], hslFromRgb[2]));
            }
            writer.write("\n");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}





