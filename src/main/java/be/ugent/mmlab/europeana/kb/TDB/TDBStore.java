package be.ugent.mmlab.europeana.kb.TDB;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.tdb.TDBFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ghaesen
 * Date: 10/28/13
 * Time: 9:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class TDBStore {

    private final Dataset dataset;

    public TDBStore (final String tdbDirectory) {
        dataset = TDBFactory.createDataset(tdbDirectory);
    }

    /*public Model getModel() {
        return dataset.getDefaultModel();
    } */

    public Dataset getDataset() {
        return dataset;
    }

    /**
     * Read rdf, triples, turle, ... statements from the given file and put them in the current model.
     * This is done in one transaction: either everything is ok and added, either nothing happened.
     * Mind that this is slower than bulk-loading from the command-line.
     * @param fileName  The name of the input file. Files can be plain text or compressed with gzip, bzip2, xz.
     * @param lang      The type (language) of the data. Can be "RDF/XML", "N-TRIPLE",
     *                  "TURTLE" (or "TTL") and "N3"
     * @exception IOException   Reading from the given file went wrong. No data is added.
     */
    public void addFromFile(final String fileName, final String lang) throws IOException {
        InputStream cin = null;
        try {
            try {
                cin = new CompressorStreamFactory().createCompressorInputStream(new BufferedInputStream(new FileInputStream(fileName)));
            } catch (CompressorException e) {
                cin = new BufferedInputStream(new FileInputStream(fileName));
            }
            dataset.begin(ReadWrite.WRITE);
            dataset.getDefaultModel().read(cin, "http://localhost/", lang);
            dataset.commit();
        } finally {
            if (cin != null) {
                cin.close();
            }
            dataset.end();
        }
    }

    /**
     * Read rdf, triples, turle, ... statements from the given file and put them in the current model. The language is
     * guessed from the file name extension.
     * The loading is done in one transaction: either everything is ok and added, either nothing happened.
     * Mind that this is slower than bulk-loading from the command-line.
     * @param fileName  The name of the input file. Files can be plain text or compressed with gzip, bzip2, xz.
     * @exception IOException   Reading from the given file went wrong. No data is added.
     */
    public void addFromFile(final String fileName) throws IOException {
        addFromFile(fileName, determineInput(fileName));
    }

    private String determineInput(String inputFile) {
        String type = "N-TRIPLE";
        if (inputFile.contains(".rdf")) type = "RDF/XML";
        else if (inputFile.contains(".n3")) type = "N3";
        else if (inputFile.contains(".ttl")) type = "TURTLE";
        return type;
    }
}
