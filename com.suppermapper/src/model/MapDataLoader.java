package model;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapDataLoader {

    public static final GeometryFactory geometryFactory = new GeometryFactory();

    public List<MapData> load() {
        System.out.print("Begin loading the london map...");
        List<Geometry> geometries = new ArrayList<>();
        List<MapData> mapDataList;

        try {
            URL url = getClass().getResource("map-london_geo_accurate.csv");
            try (Reader reader = new FileReader(new File(url.toURI()).getAbsolutePath())) {
                CsvToBean<MapData> csvToBean = new CsvToBeanBuilder(reader).withType(MapData.class).withIgnoreLeadingWhiteSpace(true).build();

                mapDataList = csvToBean.parse();
                for (MapData mapData : mapDataList) {
                    mapData.getGeometry().setUserData(mapData);
                }

            }

        } catch (IOException | URISyntaxException e) {
            System.out.println("Failure! Something went wrong");
            e.printStackTrace();
            return null;
        }


        System.out.println("Success! Number of loaded records: " + mapDataList.size());
        return mapDataList;
    }
}


