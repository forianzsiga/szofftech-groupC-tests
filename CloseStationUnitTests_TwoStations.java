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

public class CloseStationUnitTests_TwoStations {

    ModelData model;

    Station stationA;
    Station stationB;

    Line line1;

    /* CREATE MAP

    A       B
    * ----- *

    */
    @Before
    public void createMap() {
        List<Stop> stationAStops = new ArrayList<>();
        stationA = new Station(0, "A", new Coordinate(47.4891, 19.0614), stationAStops);

        List<Stop> stationBStops = new ArrayList<>();
        stationB = new Station(1, "B", new Coordinate(47.4891, 19.0714), stationBStops);

        List<Stop> line1Stops = new ArrayList<>();
        line1 = new Line(3, "1", "#009EE3", false, line1Stops);

        Stop line1AStop = new Stop(stationA, line1);
        stationAStops.add(line1AStop);
        line1Stops.add(line1AStop);

        Stop line1BStop = new Stop(stationB, line1);
        stationBStops.add(line1BStop);
        line1Stops.add(line1BStop);

        model = new ModelData(new ArrayList<>(List.of(line1)), new ArrayList<>(List.of(stationA, stationB)));
    }

    /* ASSERT

    A       B (empty)

    */

    @Test
    public void testCloseStation_removeLeftStation() {

        ReplacementServices.closeStation(model, stationA, List.of(line1));

        assertModel(model)
                .hasLines(0)
                .hasStations(0);
    }

    /* ASSERT

    A       B (empty)

     */
    @Test
    public void testCloseStation_removeRightStation() {

        ReplacementServices.closeStation(model, stationB, List.of(line1));

        assertModel(model)
                .hasLines(0)
                .hasStations(0);
    }


    private static ModelAsserter assertModel(ModelData data) {
        return new ModelAsserter(data);
    }

    private static class ModelAsserter {
        private final ModelData model;

        private ModelAsserter(ModelData model) {
            this.model = model;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasStations(int num) {
            Assert.assertEquals("There are more or fewer stations lines than expected", num, model.stations.size());
            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasExactStations(String... stationNames) {
            assertStationListEquals(getStationsFromNames(stationNames), model.stations);
            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasStations(String... stationNames) {
            for(Station station : getStationsFromNames(stationNames)) {
                Assert.assertTrue("The station was not present: " + station.getName() + ")", model.stations.contains(station));
            }
            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasStation(String stationName) {
            hasStations(stationName);
            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasStation(String stationName, Function<Station, Void> assertStation) {
            hasStations(stationName);
            assertStation.apply(getStationFromName(stationName));
            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasStationWithExactLines(String stationName, String... lineNames) {
            hasStations(stationName);

            Station station = getStationFromName(stationName);
            List<Line> lines = getLinesFromNames(lineNames);

            assertLineListEquals(lines, station.getStops().stream().map(Stop::getLine).toList());

            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasStationWithLines(String stationName, String... lineNames) {
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

        public CloseStationUnitTests_TwoStations.ModelAsserter hasLines(int num) {
            Assert.assertEquals("There are more or fewer lines than expected", num, model.lines.size());
            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasExactLines(String... lineNames) {
            assertLineListEquals(getLinesFromNames(lineNames), model.lines);
            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasLines(String... lineNames) {
            for(Line line : getLinesFromNames(lineNames)) {
                Assert.assertTrue("The line was not present (" + line + ")", model.lines.contains(line));
            }
            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasLine(String lineName) {
            hasLines(lineName);
            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasLine(String lineName, Function<Line, Void> assertLine) {
            hasLines(lineName);
            assertLine.apply(getLineFromName(lineName));
            return this;
        }

        public CloseStationUnitTests_TwoStations.ModelAsserter hasLineWithExactStations(String lineName, String... stationNames) {
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