package org.openmetromaps.maps;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmetromaps.maps.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReplacementServicesUnitTests_FourStationsMulti {

    ModelData model;

    Station stationA;
    Station stationB;
    Station stationC;
    Station stationD;

    Line line1;
    Line line2;
    Line line3;
    Line line4;


    /* CREATE MAP

    A       B       C       D
    * ------------- *               1
            * ----- *               2
            * ------------- *       3
    * --------------------- *       4

    */
    @Before
    public void createMap() {
        List<Stop> stationAStops = new ArrayList<>();
        stationA = new Station(0, "A", new Coordinate(47.4891, 19.0614), stationAStops);

        List<Stop> stationBStops = new ArrayList<>();
        stationB = new Station(1, "B", new Coordinate(47.4891, 19.0714), stationBStops);

        List<Stop> stationCStops = new ArrayList<>();
        stationC = new Station(2, "C", new Coordinate(47.4891, 19.0814), stationCStops);

        List<Stop> stationDStops = new ArrayList<>();
        stationD = new Station(3, "D", new Coordinate(47.4891, 19.0914), stationDStops);

        List<Stop> line1Stops = new ArrayList<>();
        line1 = new Line(4, "1", "#009EE3", false, line1Stops);

        List<Stop> line2Stops = new ArrayList<>();
        line2 = new Line(5, "2", "#009EE3", false, line2Stops);

        List<Stop> line3Stops = new ArrayList<>();
        line3 = new Line(6, "3", "#009EE3", false, line3Stops);

        List<Stop> line4Stops = new ArrayList<>();
        line4 = new Line(7, "4", "#009EE3", false, line4Stops);


        // Line 1 Spanning from A to C

        Stop line1AStop = new Stop(stationA, line1);
        stationAStops.add(line1AStop);
        line1Stops.add(line1AStop);

        Stop line1BStop = new Stop(stationB, line1);
        stationBStops.add(line1BStop);
        line1Stops.add(line1BStop);

        Stop line1CStop = new Stop(stationC, line1);
        stationCStops.add(line1CStop);
        line1Stops.add(line1CStop);


        // Line 2 Spanning from B to C

        Stop line2BStop = new Stop(stationB, line2);
        stationBStops.add(line2BStop);
        line2Stops.add(line2BStop);

        Stop line2CStop = new Stop(stationC, line2);
        stationCStops.add(line2CStop);
        line2Stops.add(line2CStop);


        // Line 3 Spanning from B to D

        Stop line3BStop = new Stop(stationB, line3);
        stationBStops.add(line3BStop);
        line3Stops.add(line3BStop);

        Stop line3CStop = new Stop(stationC, line3);
        stationCStops.add(line3CStop);
        line3Stops.add(line3CStop);

        Stop line3DStop = new Stop(stationD, line3);
        stationDStops.add(line3DStop);
        line3Stops.add(line3DStop);


        // Line 4 Spanning from A to D (All stations)

        Stop line4AStop = new Stop(stationA, line4);
        stationAStops.add(line4AStop);
        line4Stops.add(line4AStop);

        Stop line4BStop = new Stop(stationB, line4);
        stationBStops.add(line4BStop);
        line4Stops.add(line4BStop);

        Stop line4CStop = new Stop(stationC, line4);
        stationCStops.add(line4CStop);
        line4Stops.add(line4CStop);

        Stop line4DStop = new Stop(stationD, line4);
        stationDStops.add(line4DStop);
        line4Stops.add(line4DStop);

        model = new ModelData(new ArrayList<>(List.of(line1)), new ArrayList<>(List.of(stationA, stationB, stationC, stationD)));
    }

    /* ASSERT MAP
    A       B       C       D
    * ----- *                       1
                                    2 (No stops, thus removed)
                    * ----- *       3
    * ----- *                       4-1
                    * ----- *       4-2
            * ----- *               P-1
     */
    @Test
    public void testReplacementServices_multiLine() {

        ReplacementServices.createReplacementService(model, List.of(stationB, stationC), List.of(line1, line2, line3, line4));
        assertModel(model)
                .hasExactStations("A", "B", "C", "D")
                .hasExactLines("1", "3", "4-1", "4-2", "P-1")

                .hasStationWithExactLines("A", "1", "4-1")
                .hasStationWithExactLines("B", "1", "4-1", "P-1")
                .hasStationWithExactLines("C", "3", "4-2", "P-1")
                .hasStationWithExactLines("D", "3", "4-2")

                .hasLineWithExactStations("1", "A", "B")
                .hasLineWithExactStations("3", "C", "D")
                .hasLineWithExactStations("4-1", "A", "B")
                .hasLineWithExactStations("4-2", "C", "D")
                .hasLineWithExactStations("P-1", "B", "C");
    }

    private static ModelAsserter assertModel(ModelData data) {
        return new ModelAsserter(data);
    }

    private static class ModelAsserter {
        private final ModelData model;

        private ModelAsserter(ModelData model) {
            this.model = model;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasStations(int num) {
            Assert.assertEquals("There are more or fewer stations lines than expected", num, model.stations.size());
            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasExactStations(String... stationNames) {
            assertStationListEquals(getStationsFromNames(stationNames), model.stations);
            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasStations(String... stationNames) {
            for(Station station : getStationsFromNames(stationNames)) {
                Assert.assertTrue("The station was not present: " + station.getName() + ")", model.stations.contains(station));
            }
            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasStation(String stationName) {
            hasStations(stationName);
            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasStation(String stationName, Function<Station, Void> assertStation) {
            hasStations(stationName);
            assertStation.apply(getStationFromName(stationName));
            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasStationWithExactLines(String stationName, String... lineNames) {
            hasStations(stationName);

            Station station = getStationFromName(stationName);
            List<Line> lines = getLinesFromNames(lineNames);

            assertLineListEquals(lines, station.getStops().stream().map(Stop::getLine).toList());

            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasStationWithLines(String stationName, String... lineNames) {
            hasStations(stationName);

            Station station = getStationFromName(stationName);
            List<Line> lines = getLinesFromNames(lineNames);
            List<Line> stationLines = station.getStops().stream().map(Stop::getLine).toList();

            for(Line line : lines) {
                Assert.assertTrue(
                        "Line (" + line.getName() + ") does not stop at Station (" + station.getName() + ")",
                        stationLines.contains(line)
                );
            }

            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasLines(int num) {
            Assert.assertEquals("There are more or fewer lines than expected", num, model.lines.size());
            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasExactLines(String... lineNames) {
            assertLineListEquals(getLinesFromNames(lineNames), model.lines);
            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasLines(String... lineNames) {
            for(Line line : getLinesFromNames(lineNames)) {
                Assert.assertTrue("The line was not present (" + line + ")", model.lines.contains(line));
            }
            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasLine(String lineName) {
            hasLines(lineName);
            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasLine(String lineName, Function<Line, Void> assertLine) {
            hasLines(lineName);
            assertLine.apply(getLineFromName(lineName));
            return this;
        }

        public ReplacementServicesUnitTests_FourStationsMulti.ModelAsserter hasLineWithExactStations(String lineName, String... stationNames) {
            hasLines(lineName);

            Line line = getLineFromName(lineName);
            List<Station> stations = getStationsFromNames(stationNames);
            List<Station> lineStations = line.getStops().stream().map(Stop::getStation).toList();

            String expectedStationNames = stations.stream().map(Station::getName).collect(Collectors.joining(", "));
            String actualStationNames = lineStations.stream().map(Station::getName).collect(Collectors.joining(", "));

            Assert.assertTrue(
                    "Stations differ on line (" +
                            line.getName() +
                            "). Expected: " +
                            expectedStationNames +
                            ". Actual: "
                            + actualStationNames,
                    stations.equals(lineStations) || stations.equals(Lists.reverse(lineStations))
            );

            return this;
        }

        private List<Station> getStationsFromNames(String... stationNames) {
            return getStationsFromNames(List.of(stationNames));
        }

        private List<Station> getStationsFromNames(List<String> stationNames) {
            return stationNames.stream()
                    .map(this::getStationFromName)
                    .toList();
        }

        private Station getStationFromName(String stationName) {
            return model.stations.stream()
                    .filter(s -> s.getName().equals(stationName))
                    .findFirst()
                    .orElseThrow(() -> {
                        Assert.fail("Station with name is not in the model: " + stationName);
                        return new RuntimeException();
                    });
        }

        private void assertStationListEquals(List<Station> expected, List<Station> actual) {
            String expectedStations = expected.stream().map(Station::getName).collect(Collectors.joining(", "));
            String actualStations = actual.stream().map(Station::getName).collect(Collectors.joining(", "));

            Assert.assertEquals("There are more or fewer stations than expected", expected.size(), actual.size());
            Assert.assertTrue(
                    "There are stations that were not expected but are present. Expected: " +
                            expectedStations +
                            ". Actual: " +
                            actualStations,
                    expected.containsAll(actual)
            );
            Assert.assertTrue(
                    "There are stations that were expected but are not present. Expected: " +
                            expectedStations +
                            ". Actual: " +
                            actualStations,
                    actual.containsAll(expected)
            );
        }

        private List<Line> getLinesFromNames(String... lineNames) {
            return getLinesFromNames(List.of(lineNames));
        }

        private List<Line> getLinesFromNames(List<String> lineNames) {
            return lineNames.stream()
                    .map(this::getLineFromName)
                    .toList();
        }

        private Line getLineFromName(String lineName) {
            return model.lines.stream()
                    .filter(l -> l.getName().equals(lineName))
                    .findFirst()
                    .orElseThrow(() -> {
                        Assert.fail("Line with name is not in the model: " + lineName);
                        return new RuntimeException();
                    });
        }

        private void assertLineListEquals(List<Line> expected, List<Line> actual) {
            String expectedLines = expected.stream().map(Line::getName).collect(Collectors.joining(", "));
            String actualLines = actual.stream().map(Line::getName).collect(Collectors.joining(", "));

            Assert.assertEquals("There are more or fewer lines than expected", expected.size(), actual.size());
            Assert.assertTrue(
                    "There are lines that were not expected but are present. Expected: " +
                            expectedLines +
                            ". Actual: " +
                            actualLines,
                    expected.containsAll(actual)
            );
            Assert.assertTrue(
                    "There are lines that were expected but are not present. Expected: " +
                            expectedLines +
                            ". Actual: " +
                            actualLines,
                    actual.containsAll(expected)
            );
        }
    }

}