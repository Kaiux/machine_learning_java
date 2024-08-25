package cn.kaiux.machine_learning.graph;

import com.google.common.collect.Lists;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

/**
 * @author TechieYu
 * @date 2024/8/25
 */
public class MathPlotter2D {

    private final List<ScatterSeries> scatterData = Lists.newLinkedList();
    private final List<FunctionSeries> functions = Lists.newLinkedList();
    private final int boarder = 30;
    private double xMin = -10;
    private double xMax = 10;
    private double yMin = -5;
    private double yMax = 5;
    private int pointSize = 8;
    private String pointStr = "█";

    public void addScatterData(double[][] data, Color color) {
        scatterData.add(new ScatterSeries(data, color));
        updateRanges(data);
    }

    public void addFunction(Function<Double, Double> function, String name, Color color) {
        functions.add(new FunctionSeries(function, name, color));
        updateRanges(function);
    }

    public void setPointStr(String pointStr) {
        this.pointStr = pointStr;
    }

    public void setPointSize(int pointSize) {
        this.pointSize = pointSize;
    }

    private void updateRanges(double[][] data) {
        for (double[] point : data) {
            double x = point[0];
            double y = point[1];
            if (x < xMin) {
                xMin = x;
                xMax = -x;
            }
            if (x > xMax) {
                xMax = x;
                xMin = -x;
            }
            if (y < yMin) {
                yMin = y;
                yMax = -y;
            }
            if (y > yMax) {
                yMin = -y;
                yMax = y;
            }
        }
    }

    private void updateRanges(Function<Double, Double> function) {
        // 100步长来评估函数
        double step = (xMax - xMin) / 100;
        for (double x = xMin; x <= xMax; x += step) {
            double y = function.apply(x);
            if (x < xMin) xMin = x;
            if (x > xMax) xMax = x;
            if (y < yMin) yMin = y;
            if (y > yMax) yMax = y;
        }
    }

    public JPanel createPlotPanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                drawAxes(g2d);
                drawScale(g2d);
                drawFunctions(g2d);
                drawScatterData(g2d);
            }

            private void drawAxes(Graphics2D g2d) {
                int width = getWidth();
                int height = getHeight();
                g2d.setColor(Color.GRAY);

                // 绘制X轴
                g2d.drawLine(0, height / 2, width, height / 2);

                // 绘制Y轴
                g2d.drawLine(width / 2, 0, width / 2, height);
            }

            private void drawScale(Graphics2D g2d) {
                int width = getWidth() - boarder;
                int height = getHeight() - boarder;
                int widthReal = getWidth();
                int heightReal = getHeight();
                g2d.setColor(Color.BLACK);

                // X轴刻度
                int xStep = width / 10;
                for (int i = 0; i <= 10; i++) {
                    double xValue = xMin + i * (xMax - xMin) / 10;
                    int xPixel = boarder / 2 + i * xStep;
                    g2d.drawLine(xPixel, heightReal / 2 - 5, xPixel, heightReal / 2 + 5);
                    g2d.drawString(String.format("%.1f", xValue), xPixel - 15, heightReal / 2 + 20);
                }

                // Y轴刻度
                int yStep = height / 10;
                for (int i = 0; i <= 10; i++) {
                    double yValue = yMax - i * (yMax - yMin) / 10;
                    int yPixel = boarder / 2 + i * yStep;
                    g2d.drawLine(widthReal / 2 - 5, yPixel, widthReal / 2 + 5, yPixel);
                    g2d.drawString(String.format("%.1f", yValue), width / 2 + 25, yPixel + 5);
                }
            }

            private void drawFunctions(Graphics2D g2d) {

                double step = (xMax - xMin) / 500; // 分辨率

                for (FunctionSeries series : functions) {
                    g2d.setColor(series.color);
                    double prevX = xMin;
                    double prevY = series.function.apply(prevX);

                    for (double x = xMin + step; x <= xMax; x += step) {
                        double y = series.function.apply(x);
                        int xPixel1 = findXPixel(prevX);
                        int yPixel1 = findYPixel(prevY);
                        int xPixel2 = findXPixel(x);
                        int yPixel2 = findYPixel(y);
                        g2d.drawLine(xPixel1, yPixel1, xPixel2, yPixel2);
                        prevX = x;
                        prevY = y;
                    }
                }
            }

            private int findXPixel(double xValue) {
                int width = getWidth() - boarder;
                double step = (xMax - xMin) / 10;
                double pixelStep = step / (width / 10.0);
                return (int) ((double) boarder / 2 + (xValue - xMin) / pixelStep);
            }

            private int findYPixel(double yValue) {
                int height = getHeight() - boarder;
                double step = (yMax - yMin) / 10;
                double pixelStep = step / (height / 10.0);
                return (int) ((double) boarder / 2 + (yMax - yValue) / pixelStep);
            }

            private void drawScatterData(Graphics2D g2d) {
                for (ScatterSeries series : scatterData) {
                    g2d.setColor(series.color);
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, pointSize)); // 设置字体为等宽字体

                    for (double[] point : series.data) {
                        double x = point[0];
                        double y = point[1];

                        int xPixel = findXPixel(x);
                        int yPixel = findYPixel(y);

                        g2d.drawString(pointStr, xPixel - pointSize / 2, yPixel + pointSize / 2);
                    }
                }
            }
        };
    }

    public void plot() {
        JFrame frame = new JFrame("MathPlotter2D");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(createPlotPanel());
        frame.setSize(800 + boarder, 600 + boarder);
        frame.setVisible(true);
    }

    private static class ScatterSeries {
        double[][] data;
        Color color;

        ScatterSeries(double[][] data, Color color) {
            this.data = data;
            this.color = color;
        }
    }

    private static class FunctionSeries {
        Function<Double, Double> function;
        String name;
        Color color;

        FunctionSeries(Function<Double, Double> function, String name, Color color) {
            this.function = function;
            this.name = name;
            this.color = color;
        }
    }


    public static void main(String[] args) {

        MathPlotter2D plot = new MathPlotter2D();

        plot.setPointStr("X");
        plot.setPointSize(15);

        plot.addScatterData(new double[][]{
                {1, 2},
                {3, 4},
                {5, 6}
        }, Color.RED);

        plot.addScatterData(new double[][]{
                {-2, -3},
                {0, 0}
        }, Color.GREEN);

        // 添加多个函数，指定名称和颜色
        plot.addFunction(Math::sin, "sin(x)", Color.BLUE);
        plot.addFunction(Math::cos, "cos(x)", Color.ORANGE);

        // 绘制图像
        plot.plot();
    }
}