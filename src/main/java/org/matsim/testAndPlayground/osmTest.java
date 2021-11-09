package org.matsim.testAndPlayground;

import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.osm.networkReader.LinkProperties;
import org.matsim.contrib.osm.networkReader.OsmTags;
import org.matsim.contrib.osm.networkReader.SupersonicOsmNetworkReader;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.utils.gis.shp2matsim.ShpGeometryUtils;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class osmTest {
    private static String BelgiumCRS = "EPSG:31370";
    private static Path input = Paths.get("scenarios/BrusselsNetworkFromOSM/belgium-latest.osm.pbf");
    private static Path filterShape = Paths.get("scenarios/BrusselsShapefile/UrbAdm_MONITORING_DISTRICT.shp");

    public static void main(String[] args) throws MalformedURLException {
        new osmTest().create();
    }

    private void create() throws MalformedURLException {

        // choose an appropriate coordinate transformation. OSM Data is in WGS84. When working in Belgium,
        // EPSG:31370 as target system is a good choice (and it is the general CRS of MATSim Brussels scenario)
        CoordinateTransformation transformation = TransformationFactory.getCoordinateTransformation(
                TransformationFactory.WGS84, BelgiumCRS
        );

        // load the geometries of the shape file, so they can be used as a filter during network creation
        // using PreparedGeometry instead of Geometry increases speed a lot (usually)
        List<PreparedGeometry> filterGeometries = ShpGeometryUtils.loadPreparedGeometries(filterShape.toUri().toURL());

        // create an osm network reader with a filter
        SupersonicOsmNetworkReader reader = new SupersonicOsmNetworkReader.Builder()
                .setCoordinateTransformation(transformation)
                .setIncludeLinkAtCoordWithHierarchy((coord, hierarchyLevel) -> {
                    return true;
                })
                .setAfterLinkCreated((link, osmTags, direction) -> {

                    // if the original osm-link contains a cycleway tag, add bicycle as allowed transport mode
                    // although for serious bicycle networks use OsmBicycleNetworkReader
                    if (osmTags.containsKey(OsmTags.CYCLEWAY)) {
                        Set<String> modes = new HashSet<>(link.getAllowedModes());
                        modes.add(TransportMode.bike);
                        link.setAllowedModes(modes);
                    }
                })
                .build();

        // the actual work is done in this call. Depending on the data size this may take a long time
        Network network = reader.read(input.toString());

        // clean the network to remove unconnected parts where agents might get stuck
        new NetworkCleaner().run(network);

        // write out the network into a file
        new NetworkWriter(network).write("scenarios/BrusselsScenario/networkWithoutPT.xml.gz");
    }

}
