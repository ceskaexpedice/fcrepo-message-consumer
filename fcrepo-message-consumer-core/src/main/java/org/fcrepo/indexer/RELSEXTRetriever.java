package org.fcrepo.indexer;

import static com.google.common.base.Throwables.propagate;
import static org.apache.http.HttpStatus.SC_OK;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cz.incad.kramerius.FedoraNamespaces;
import cz.incad.kramerius.utils.XMLUtils;

/**
 * Returns RELS-EXT retriever. 
 * It is able to parse RELS-EXT and generate set of RELS-EXT items
 * @author pstastny
 */
public class RELSEXTRetriever implements Supplier<RELSEXTFields> {
    private static final Logger LOGGER = getLogger(NamedFieldsRetriever.class);
    
    // INFO Default prefix
    private static final String INFO_FEDORA_PREFIX = "info:fedora/";

    // traverse tree predicates
    public static final List<String> TREE_PREDICATES=Arrays.asList(
                "hasPage",
                "hasPart",
                "hasVolume",
                "hasItem",
                "hasUnit",
                "hasIntCompPart",
                "isOnPage"
    );

    
    
    // uri for item
    private final URI uri;
    
    // http client 
    private final HttpClient httpClient;

    // parsing json
    private Gson gson;

 
    public RELSEXTRetriever(final URI uri, final HttpClient client) {
        this.uri = uri;
        this.httpClient = client;
        final NamedFieldsDeserializer deserializer =
            new NamedFieldsDeserializer();
        this.gson =
            new GsonBuilder().create();
        deserializer.setGson(gson);
    }

    @Override
    public RELSEXTFields get() {
        try {
            final HttpGet getRequest = new HttpGet(uri);
            final HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() == SC_OK) {
                try (
                    Reader r =
                        new InputStreamReader(
                                response.getEntity().getContent(), "UTF8")) {
                    return createRelsExt(r);
                }
            } else {
                throw new HttpException(response.getStatusLine().toString());
            }
        } catch (IOException | HttpException | ParserConfigurationException | SAXException   e) {
            throw propagate(e);
        }
    }

    /**
     * Parse and created RELS-EXT from given reader
     * @param r Reader
     * @return Parsed RELSEXTFields
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private RELSEXTFields createRelsExt(Reader r) throws ParserConfigurationException, SAXException, IOException {
        StringWriter strWriter = new StringWriter();
        IOUtils.copy(r, strWriter);
        StringReader strReader = new StringReader(strWriter.toString());
        
        RELSEXTFields rFields = new RELSEXTFields();
        Document relsExt = XMLUtils.parseDocument(strReader,true);
        Element descEl = XMLUtils.findElement(relsExt.getDocumentElement(), "Description",
                FedoraNamespaces.RDF_NAMESPACE_URI);
        Element modelEl = XMLUtils.findElement(descEl, "hasModel",
                FedoraNamespaces.FEDORA_MODELS_URI);
        String sourcePid = cutCommonPrefix(descEl.getAttribute("rdf:about"));
        String model = cutCommonPrefix((modelEl!= null)  ? modelEl.getAttribute("rdf:resource") : "uknown");
        model = (model.contains("model:")) ? model.substring("model:".length()) : model;        
        
        rFields.setSource(sourcePid);
        rFields.setSourceModel(model);
        
        List<Element> els = XMLUtils.getElements(descEl);
        for (Element el : els) {
            String localName = el.getLocalName();
            if (TREE_PREDICATES.contains(localName)) {
                if (el.hasAttribute("rdf:resource")) {
                    String relation = el.getLocalName();
                    String targetPid = cutCommonPrefix(el.getAttributes().getNamedItem("rdf:resource").getNodeValue());
                    rFields.addRelation(relation, targetPid);
                }
            }
        }

        return rFields;
    }

    private String cutCommonPrefix(String sourcePid) {
        return sourcePid.contains(INFO_FEDORA_PREFIX) ? sourcePid.substring(INFO_FEDORA_PREFIX.length()) : sourcePid;
    }
}
