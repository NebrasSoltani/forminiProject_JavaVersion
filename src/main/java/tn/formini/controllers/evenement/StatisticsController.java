package tn.formini.controllers.evenement;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.embed.swing.SwingFXUtils;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ArrayList;

@Component
@Scope("prototype")
public class StatisticsController implements Initializable {

    @FXML private StackPane chartContainer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void hideCloseButton() {
        // No longer needed
    }

    public void setBarData(String title, Map<String, Integer> data) {
        CategoryChart chart = new CategoryChartBuilder()
                .width(400).height(350).title(title)
                .xAxisTitle("Catégories").yAxisTitle("Total")
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNE);
        chart.getStyler().setLabelsVisible(true);
        chart.getStyler().setPlotGridLinesVisible(false);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setSeriesColors(new Color[]{new Color(105, 116, 232)});

        if (data != null && !data.isEmpty()) {
            chart.addSeries("Données", new ArrayList<>(data.keySet()), new ArrayList<>(data.values()));
        }

        renderChart(chart);
    }

    public void setPieData(String title, Map<String, Integer> data) {
        PieChart chart = new PieChartBuilder()
                .width(400).height(350).title(title)
                .build();

        chart.getStyler().setLegendPosition(Styler.LegendPosition.OutsideS);
        chart.getStyler().setPlotContentSize(.7);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);

        if (data != null) {
            data.forEach(chart::addSeries);
        }

        renderChart(chart);
    }

    private void renderChart(org.knowm.xchart.internal.chartpart.Chart<?, ?> chart) {
        new Thread(() -> {
            try {
                BufferedImage bufferedImage = BitmapEncoder.getBufferedImage(chart);
                Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                
                Platform.runLater(() -> {
                    ImageView imageView = new ImageView(fxImage);
                    imageView.setPreserveRatio(true);
                    imageView.fitWidthProperty().bind(chartContainer.widthProperty().subtract(40));
                    imageView.fitHeightProperty().bind(chartContainer.heightProperty().subtract(40));
                    
                    chartContainer.getChildren().clear();
                    chartContainer.getChildren().add(imageView);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
