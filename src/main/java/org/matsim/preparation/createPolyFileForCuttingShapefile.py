import geopandas as gpd

def write_poly(df, path, geometry_column = "geometry"):
    df = df.to_crs("EPSG:4326")

    df["aggregate"] = 0
    area = df.dissolve(by = "aggregate")[geometry_column].values[0]

    if not hasattr(area, "exterior"):
        print("Selected area is not connected -> Using convex hull.")
        area = area.convex_hull

    data = []
    data.append("polyfile")
    data.append("polygon")

    for coordinate in area.exterior.coords:
        data.append("    %e    %e" % coordinate)

    data.append("END")
    data.append("END")

    with open(path, "w+") as f:
        f.write("\n".join(data))
        
df = gpd.read_file("boundary.gpkg")
write_poly(df, "boundary.poly")