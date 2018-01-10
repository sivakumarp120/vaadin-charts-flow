package com.vaadin.addon.charts.tests;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.vaadin.addon.charts.AbstractChartExample;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.ChartClickEvent;
import com.vaadin.addon.charts.PointClickEvent;
import com.vaadin.addon.charts.PointSelectEvent;
import com.vaadin.addon.charts.PointUnselectEvent;
import com.vaadin.addon.charts.SeriesCheckboxClickEvent;
import com.vaadin.addon.charts.SeriesHideEvent;
import com.vaadin.addon.charts.SeriesLegendItemClickEvent;
import com.vaadin.addon.charts.SeriesShowEvent;
import com.vaadin.addon.charts.examples.dynamic.ServerSideEvents;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.Series;
import com.vaadin.testbench.By;
import com.vaadin.tests.elements.ButtonElement;
import com.vaadin.tests.elements.ChartElement;
import com.vaadin.tests.elements.CheckboxElement;
import com.vaadin.tests.elements.LabelElement;
import com.vaadin.ui.event.ComponentEvent;

public class ServerSideEventsIT extends AbstractTBTest {

    @Override
    @Before
    public void setup() throws Exception {
        super.setup();
        driver.manage().window().setSize(new Dimension(1600, 1600));
    }

    @Override
    protected Class<? extends AbstractChartExample> getTestView() {
        return ServerSideEvents.class;
    }

    @Test
    public void chartClick_occured_eventIsFired() {
        WebElement chart = getChartElement();

        new Actions(driver).moveToElement(chart, 200, 200).click().build()
                .perform();

        assertLastEventIsType(ChartClickEvent.class);
    }

    @Test
    public void pointClick_occured_eventIsFired() {
        WebElement firstMarker = findLastDataPointOfTheFirstSeries();

        firstMarker.click();

        assertFirstHistoryEventIsType(PointClickEvent.class);
    }

    @Test
    public void legendItemClick_occuredWhileVisibilityTogglingDisabled_eventIsFired() {
        WebElement disableVisibilityToggling = findDisableVisibityToggle();
        disableVisibilityToggling.click();
        WebElement legendItem = findLegendItem();

        legendItem.click();

        assertLastEventIsType(SeriesLegendItemClickEvent.class);
    }

    @Test
    public void legendItemClick_occuredWhileVisibilityTogglingEnabled_eventAndSeriesHideEventAreFired() {
        WebElement legendItem = findLegendItem();

        legendItem.click();

        assertLastEventIsType(SeriesHideEvent.class);
        assertFirstHistoryEventIsType(SeriesLegendItemClickEvent.class);
    }

    @Test
    public void checkBoxClick_occured_eventIsFired() {
        WebElement checkBox = findCheckBox();

        checkBox.click();

        assertLastEventIsType(SeriesCheckboxClickEvent.class);
    }

    @Test
    public void checkBoxClick_secondCheckboxClicked_secondSeriesIsReturned() {
        WebElement secondCheckBox = findSecondCheckbox();

        secondCheckBox.click();

        SeriesCheckboxClickEvent checkboxClickEvent = readCheckboxEventDetails();
        Assert.assertEquals(1, checkboxClickEvent.getSeriesItemIndex());
    }

    @Test
    public void checkBoxClick_seriesWasNotSelected_checkBoxIsChecked() {
        WebElement secondCheckBox = findSecondCheckbox();

        secondCheckBox.click();

        SeriesCheckboxClickEvent checkboxClickEvent = readCheckboxEventDetails();
        Assert.assertTrue(checkboxClickEvent.isChecked());
    }

    @Test
    public void hideSeries_occuredFromLegendClick_eventIsFired() {
        WebElement legendItem = findLegendItem();

        legendItem.click();

        assertLastEventIsType(SeriesHideEvent.class);
    }

    @Test
    public void hideSeries_occuredFromServer_eventIsFired() {
        WebElement hideSeries = findHideFirstSeriesButton();

        hideSeries.click();

        assertLastEventIsType(SeriesHideEvent.class);
    }

    @Test
    public void showSeries_occuredFromLegendClick_eventIsFired() {
        WebElement legendItem = findLegendItem();
        legendItem.click();

        legendItem.click();

        assertLastEventIsType(SeriesShowEvent.class);
    }

    @Test
    public void showSeries_occuredFromServer_eventIsFired() {
        WebElement hideSeriesToggle = findHideFirstSeriesButton();
        hideSeriesToggle.click();

        hideSeriesToggle.click();

        assertLastEventIsType(SeriesShowEvent.class);
    }

    @Test
    public void unselect_occured_eventIsFired() {
        WebElement lastDataPointOfTheFirstSeries = findLastDataPointOfTheFirstSeries();

        lastDataPointOfTheFirstSeries.click();

        assertLastEventIsType(PointUnselectEvent.class);
    }

    @Test
    public void select_occured_eventIsFired() {
        ChartElement chart = getChartElement();
        List<WebElement> points = chart.getPoints();
        points.get(1).click();

        assertNthHistoryEventIsType(PointSelectEvent.class, 1);
    }

    private void assertLastEventIsType(
            Class<? extends ComponentEvent<Chart>> expectedEvent) {
        getCommandExecutor().waitForVaadin();
        LabelElement lastEvent = $(LabelElement.class).waitForFirst(); //id("lastEvent");
        Assert.assertEquals(expectedEvent.getSimpleName(), lastEvent.getText());
    }

    private void assertFirstHistoryEventIsType(
            Class<? extends ComponentEvent<Chart>> expectedEvent) {
        LabelElement lastEvent = $(LabelElement.class).id("event0");
        String eventHistory = lastEvent.getText();
        assertNotNull(eventHistory);
        String eventType = eventHistory.split(":")[0];
        Assert.assertEquals(expectedEvent.getSimpleName(), eventType);
    }

    private void assertNthHistoryEventIsType(
            Class<? extends ComponentEvent<Chart>> expectedEvent, int historyIndex) {
        LabelElement lastEvent = $(LabelElement.class).id(
                "event" + historyIndex);
        String eventHistory = lastEvent.getText();
        assertNotNull(eventHistory);
        String eventType = eventHistory.split(":")[0];
        Assert.assertEquals(expectedEvent.getSimpleName(), eventType);
    }

    private SeriesCheckboxClickEvent readCheckboxEventDetails() {
        String detailsJson = $(LabelElement.class).id("eventDetails").getText();

        Gson gson = new GsonBuilder().registerTypeAdapter(Series.class,
                new DataSeriesDeserializer()).create();

        return gson.fromJson(detailsJson, SeriesCheckboxClickEvent.class);
    }

    private WebElement findHideFirstSeriesButton() {
        return $(ButtonElement.class).waitForFirst();
    }

    private WebElement findLastDataPointOfTheFirstSeries() {
        return getElementFromShadowRoot(getChartElement(), By.cssSelector(".highcharts-markers > path"));
    }

    private WebElement findLegendItem() {
        return getElementFromShadowRoot(getChartElement(), By.className("highcharts-legend-item"));
    }

    private WebElement findCheckBox() {
        return findCheckBox(0);
    }

    private WebElement findSecondCheckbox() {
        return findCheckBox(1);
    }

    private WebElement findCheckBox(int index) {
        return getElementFromShadowRoot(getChartElement(), By.cssSelector("input[type=\"checkbox\"]"), index);
    }

    private WebElement findDisableVisibityToggle() {
        return $(CheckboxElement.class).id("visibilityToggler");
    }

    private static class DataSeriesDeserializer implements
            JsonDeserializer<Series> {
        @Override
        public Series deserialize(JsonElement series, Type type,
                                  JsonDeserializationContext jdc) throws JsonParseException {
            return new Gson().fromJson(series, DataSeries.class);
        }
    }
}