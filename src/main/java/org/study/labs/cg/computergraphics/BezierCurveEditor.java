package org.study.labs.cg.computergraphics;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class BezierCurveEditor extends Application {

    private final List<Point2D> controlPoints = new ArrayList<>();
    private final List<Point2D> bezierCurvePoints = new ArrayList<>();
    private double tStep = 0.01;
    public static final String FILE_NAME = "coefficient_matrix.txt";
    private Point2D draggedPoint = null;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(800, 600);
        primaryStage.setResizable(false);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawGrid(gc);

        createMouseActions(canvas, gc);
        MenuBar menuBar = createMenu(gc);

        VBox vbox = new VBox();
        vbox.getChildren().addAll(menuBar, canvas);

        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Bezier Curve");
        primaryStage.show();
    }

    private void drawControlPoints(GraphicsContext gc) {
        gc.setFill(Color.RED);
        for (Point2D point : controlPoints) {
            gc.fillOval(point.getX() - 3, point.getY() - 3, 6, 6);
        }
    }

    private void drawBezierCurve(GraphicsContext gc) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1.5);

        if (controlPoints.size() > 1) {
            bezierCurvePoints.clear();
            for (double t = 0; t <= 1; t += tStep) {
                Point2D point = calculateBezierPoint(controlPoints, t);
                bezierCurvePoints.add(point);
            }

            Point2D prevPoint = bezierCurvePoints.get(0);
            for (Point2D point : bezierCurvePoints) {
                gc.strokeLine(prevPoint.getX(), prevPoint.getY(), point.getX(), point.getY());
                prevPoint = point;
            }
        }
    }

    private void drawCharacteristicPolygon(GraphicsContext gc) {
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1.5);

        if (controlPoints.size() > 1) {
            Point2D prevPoint = controlPoints.get(0);
            for (int i = 1; i < controlPoints.size(); i++) {
                Point2D currentPoint = controlPoints.get(i);
                gc.strokeLine(prevPoint.getX(), prevPoint.getY(), currentPoint.getX(), currentPoint.getY());
                prevPoint = currentPoint;
            }
        }
    }

    private Point2D calculateBezierPoint(List<Point2D> points, double t) {
        int n = points.size() - 1;
        double[][] matrix = new double[2][n + 1];

        for (int i = 0; i <= n; i++) {
            double binomial = binomialCoefficient(n, i);
            double term = binomial * Math.pow(t, i) * Math.pow(1 - t, n - i);
            matrix[0][i] = term * points.get(i).getX();
            matrix[1][i] = term * points.get(i).getY();
        }

        double x = 0, y = 0;
        for (int i = 0; i <= n; i++) {
            x += matrix[0][i];
            y += matrix[1][i];
        }

        return new Point2D(x, y);
    }

    private int binomialCoefficient(int n, int k) {
        return factorial(n) / (factorial(k) * factorial(n - k));
    }

    private int factorial(int n) {
        if (n <= 1)
            return 1;
        return n * factorial(n - 1);
    }

    private void saveCoefficientMatrix() {
        try {
            FileWriter writer = new FileWriter(FILE_NAME);
            for (Point2D point : controlPoints) {
                writer.write(point.getX() + ", " + point.getY() + "\n");
            }
            writer.write("\n");
            for (Point2D point : bezierCurvePoints) {
                writer.write(point.getX() + ", " + point.getY() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("Can't write coefficients to file: " + FILE_NAME);
        }
    }

    private MenuBar createMenu(GraphicsContext gc) {
        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Options");

        MenuItem clearMenuItem = new MenuItem("Clear Canvas");
        clearMenuItem.setOnAction(event -> {
            controlPoints.clear();
            bezierCurvePoints.clear();
            gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
            drawGrid(gc);
        });

        MenuItem computeMenuItem = new MenuItem("Compute Bezier Curve with Step");
        computeMenuItem.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog(Double.toString(tStep));
            dialog.setTitle("Bezier Curve Step");
            dialog.setHeaderText("Enter the step for computing Bezier Curve points:");
            dialog.setContentText("Step:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(step -> {
                try {
                    tStep = Double.parseDouble(step);
                    if (tStep < 0 || tStep > 1) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Invalid Step Value");
                        alert.setHeaderText(null);
                        alert.setContentText("Step value should be between 0 and 1.");
                        alert.showAndWait();
                    } else {
                        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
                        drawGrid(gc);
                        drawControlPoints(gc);
                        drawCharacteristicPolygon(gc);
                        drawBezierCurve(gc);
                        saveCoefficientMatrix();
                    }
                } catch (NumberFormatException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Invalid Step Value");
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid step value.");
                    alert.showAndWait();
                }
            });
        });


        menu.getItems().addAll(computeMenuItem, clearMenuItem);
        menuBar.getMenus().add(menu);
        return menuBar;
    }

    private void createMouseActions(Canvas canvas, GraphicsContext gc) {
        canvas.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();
            boolean overPoint = false;
            for (Point2D point : controlPoints) {
                if (Math.abs(point.getX() - x) <= 3 && Math.abs(point.getY() - y) <= 3) {
                    overPoint = true;
                    break;
                }
            }
            if (!overPoint) {
                controlPoints.add(new Point2D(x, y));
                gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
                drawGrid(gc);
                drawControlPoints(gc);
                drawCharacteristicPolygon(gc);
                drawBezierCurve(gc);
                saveCoefficientMatrix();
            }
        });

        canvas.setOnMouseMoved(event -> {
            double x = event.getX();
            double y = event.getY();
            boolean overPoint = false;
            for (Point2D point : controlPoints) {
                if (Math.abs(point.getX() - x) <= 3 && Math.abs(point.getY() - y) <= 3) {
                    overPoint = true;
                    break;
                }
            }
            if (overPoint) {
                canvas.setCursor(javafx.scene.Cursor.HAND);
            } else {
                canvas.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });

        canvas.setOnMousePressed(event -> {
            double x = event.getX();
            double y = event.getY();
            for (Point2D point : controlPoints) {
                if (Math.abs(point.getX() - x) <= 3 && Math.abs(point.getY() - y) <= 3) {
                    draggedPoint = point;
                    canvas.setCursor(javafx.scene.Cursor.CLOSED_HAND);
                    return;
                }
            }
        });

        canvas.setOnMouseDragged(event -> {
            if (draggedPoint != null) {
                double x = event.getX();
                double y = event.getY();
                if (controlPoints.contains(draggedPoint)) {
                    int index = controlPoints.indexOf(draggedPoint);
                    controlPoints.set(index, new Point2D(x, y));
                    draggedPoint = new Point2D(x, y);
                    gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
                    drawGrid(gc);
                    drawControlPoints(gc);
                    drawCharacteristicPolygon(gc);
                    drawBezierCurve(gc);
                    saveCoefficientMatrix();
                }
            }
        });

        canvas.setOnMouseReleased(event -> {
            if (draggedPoint != null) {
                draggedPoint = null;
                canvas.setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
    }

    private void drawGrid(GraphicsContext gc) {
        gc.setStroke(Color.LIGHTGRAY);
        for (double x = 20; x < gc.getCanvas().getWidth(); x += 20) {
            gc.strokeLine(x, 0, x, gc.getCanvas().getHeight());
        }
        for (double y = 20; y < gc.getCanvas().getHeight(); y += 20) {
            gc.strokeLine(0, y, gc.getCanvas().getWidth(), y);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}