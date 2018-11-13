package model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import org.locationtech.jts.geom.Geometry;


public class MapData {
    // WKT,objectid,lad16cd,lad16nm,lad16nmw,bng_e,bng_n,long,lat,st_areasha,st_lengths
    @CsvCustomBindByName(converter = WKTConverter.class, column = "WKT")
    private Geometry geometry; // well known text

    @CsvBindByName
    private int objectid; // object id

    @CsvBindByName
    private String name; // district text

    public MapData(Geometry geometry, int objectid, String name) {
        this.geometry = geometry;
        this.objectid = objectid;
        this.name = name;
    }

    public MapData() {

    }

    public int getObjectId() {
        return objectid;
    }

    public void setObjectId(int objectid) {
        this.objectid = objectid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    @Override
    public String toString() {
        return "MapData{" +
                "objectid=" + objectid +
                ", name='" + name + '\'' +
                '}';
    }
}
