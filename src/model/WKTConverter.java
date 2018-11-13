package model;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

public class WKTConverter extends AbstractBeanField<Geometry> {

    @Override
    protected Geometry convert(String wkt) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        WKTReader reader = new WKTReader();
        try {
            return reader.read(wkt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
