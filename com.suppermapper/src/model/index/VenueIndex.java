package model.index;

import model.AirbnbListing;
import model.LondonCultureVenue;
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

public class VenueIndex {
    Directory directory;

    StandardAnalyzer analyzer = new StandardAnalyzer();

    public SpatialStrategy strategy = new RecursivePrefixTreeStrategy(new GeohashPrefixTree(SpatialContext.GEO, 22), "lat_lng");

    public VenueIndex(Directory directory, List<LondonCultureVenue> listings) {
        try {
            this.directory = directory;

            if (directory.listAll().length > 0) {
                return;
            }

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter w = new IndexWriter(directory, config);

            Integer idx = 0;
            for (LondonCultureVenue venue : listings) {
                addDocument(w, venue, idx);
                idx += 1;
            }
            w.close();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println(exception.getLocalizedMessage());
        }
    }

    private void addDocument(IndexWriter w, LondonCultureVenue venue, Integer index) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("id", index.toString(), Field.Store.YES));
        doc.add(new TextField("name", venue.getName(), Field.Store.YES));
        doc.add(new StringField("category", venue.getCategory(), Field.Store.YES));


        for (Field field : strategy.createIndexableFields(SpatialContext.GEO.makePoint(venue.getLongitude(), venue.getLatitude()))) {
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
