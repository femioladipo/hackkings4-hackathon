package model.index;

import model.AirbnbListing;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.store.Directory;
import org.locationtech.spatial4j.context.SpatialContext;

import java.io.IOException;
import java.util.List;

public class AirbnbListingIndex {
    Directory directory;

    StandardAnalyzer analyzer = new StandardAnalyzer();

    SpatialStrategy strategy = new RecursivePrefixTreeStrategy(new GeohashPrefixTree(SpatialContext.GEO, 22), "lat_lng");

    public AirbnbListingIndex(Directory directory, List<AirbnbListing> listings) {
        try {
            this.directory = directory;

            if (directory.listAll().length > 0) {
                return;
            }

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter w = new IndexWriter(directory, config);

            Integer idx = 0;
            for (AirbnbListing listing : listings) {
                addAirbnbListing(w, listing, idx);
                idx += 1;
            }
            w.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println(exception.getLocalizedMessage());
        }
    }

    private void addAirbnbListing(IndexWriter w, AirbnbListing lisitng, Integer index) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", index.toString(), Field.Store.YES));
        doc.add(new TextField("name", lisitng.getName(), Field.Store.YES));
        doc.add(new TextField("host_name", lisitng.getHost_name(), Field.Store.YES));
        doc.add(new StringField("district", lisitng.getNeighbourhood(), Field.Store.YES));
        doc.add(new NumericDocValuesField("price", lisitng.getPrice()));
        doc.add(new NumericDocValuesField("reviews", lisitng.getNumberOfReviews()));
        doc.add(new DoubleDocValuesField("reviewsPerMonth", lisitng.getReviewsPerMonth()));

        for (Field field : strategy.createIndexableFields(SpatialContext.GEO.makePoint(lisitng.getLatitude(), lisitng.getLongitude()))) {
            doc.add(field);
        }

        w.addDocument(doc);
    }

    public Directory getDirectory() {
        return directory;
    }

    public void setDirectory(Directory directory) {
        this.directory = directory;
    }

    public StandardAnalyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(StandardAnalyzer analyzer) {
        this.analyzer = analyzer;
    }
}
