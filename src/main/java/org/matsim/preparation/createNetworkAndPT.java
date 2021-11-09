package org.matsim.preparation;

public class createNetworkAndPT {

    public static void main(String[] args) {

//        OSM file downloaded from: http://download.geofabrik.de/europe/belgium.html
//        After downloaded, transfer the Brussels shapefile to "poly" file using python code from https://gist.github.com/sebhoerl/9a19135ffeeaede9f0abd4cdfedea3bc (or @createPolyFileForCuttingShapefile.py in this folder)

//        Cut the Belgium OSM map by OSMOSIS using below command code:
//        osmosis --read-pbf file=belgium-latest.osm.pbf --bounding-polygon file="boundary.poly" completeWays=yes completeRelations=yes --write-pbf file="city.osm.pbf"

//        Add the large roads of non-brussels regions as we also have outside-Brussels travel in the plan, by command below:
//        osmosis --read-pbf-fast file=belgium-latest.osm.pbf --tf accept-ways highway=motorway,motorway_link,trunk,trunk_link,primary,primary_link --used-node --write-pbf bigroads.osm.pbf

//        Merge the two pbf files and write an OSM file for further process in the BrusselsPT2MATSim
//        osmosis --rb file=bigroads.osm.pbf --read-pbf-fast city.osm.pbf --merge --write-xml merged-network.osm

//        More details about the discussions at: https://github.com/matsim-org/pt2matsim/issues/160

        System.out.println("We use pt2MATSim for OSM and GTFS Conversion as pt and cars share the link in the network produced by pt2matsim");
        System.out.println("You can find more details at: https://github.com/jingjunL/brusselsPT2MATSim");
    }

}
