package org.fcrepo.indexer.kramerius;

import static com.google.common.base.Throwables.propagate;
import static com.google.common.collect.Maps.transformEntries;
import static com.google.common.util.concurrent.MoreExecutors.listeningDecorator;
import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.fcrepo.indexer.Indexer.IndexerType.NAMEDFIELDS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.fcrepo.indexer.AsynchIndexer;
import org.fcrepo.indexer.NamedFields;
import org.fcrepo.indexer.RELSEXTFields;
import org.fcrepo.indexer.RELSEXTFields.RELSEXTFieldsRelation;
import org.fcrepo.indexer.Indexer.IndexerType;
import org.fcrepo.indexer.solr.SolrIndexer;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Maps.EntryTransformer;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.gson.JsonObject;

/**
 * Dedicated for creating the kramerius support index
 * @author pstastny
 */
public class KrameriusSOLRIndexer extends AsynchIndexer<RELSEXTFields, List<JsonObject>> {

        /**
         * Number of threads to use for operating against the index.
         */
        private static final Integer THREAD_POOL_SIZE = 5;

        private ListeningExecutorService executorService =
            listeningDecorator(newFixedThreadPool(THREAD_POOL_SIZE));


        private static final Logger LOGGER = getLogger(SolrIndexer.class);

        private ProcessingIndexFeeder processingIndex;
        private String solrHost;

        /**
         * solrServer instance is autowired in indexer-core.xml
         * @param solrServer the solr server
         */
        @Autowired
        public KrameriusSOLRIndexer(String solrHost) {
            this.solrHost = solrHost;
            this.processingIndex = new ProcessingIndexFeeder();
        }

        @Override
        public Callable<List<JsonObject>> updateSynch(final URI id, final RELSEXTFields relsExt) {
            LOGGER.debug("Received request for update to: {}", id);
            return new Callable<List<JsonObject>>() {

                @Override
                public List<JsonObject> call() {
                    try {
                        List<JsonObject> uResp = new ArrayList<>();
                        processingIndex.deleteByPid(relsExt.getSource(), solrHost);
                        JsonObject feedDescriptionDocument = processingIndex.feedDescriptionDocument(relsExt.getSource(), relsExt.getSourceModel(), solrHost);
                        uResp.add(feedDescriptionDocument);
                        RELSEXTFieldsRelation[] relations = relsExt.getRelations();
                        for (RELSEXTFieldsRelation rel : relations) {
                            JsonObject jsonObject = processingIndex.feedRelationDocument(relsExt.getSource(), rel.getRelation(), rel.getTargetPid(), solrHost);
                            uResp.add(jsonObject);
                        }
                        return uResp;
                    } catch (final  IOException e) {
                        LOGGER.error("Update exception: {}!", e);
                        throw propagate(e);
                    }
                }
            };
        }

       

        

        @Override
        public Callable<List<JsonObject>> removeSynch(final URI uri) {
            LOGGER.debug("Received request for removal of: {}", uri);
            return new Callable<List<JsonObject>>() {

                @Override
                public List<JsonObject> call() {
//                    try {
                        /*
                        final UpdateResponse resp = server.deleteById(uri.toString());
                        if (resp.getStatus() == 0) {
                            LOGGER.debug("Remove request was successful for: {}",
                                    uri);
                            server.commit();

                        } else {
                            LOGGER.error(
                                    "Remove request has error, code: {} for uri: {}",
                                    resp.getStatus(), uri);
                        }
                        return resp;
                        */
                        return new ArrayList<>();
//                    } catch (final SolrServerException | IOException e) {
//                        LOGGER.error("Delete Exception: {}", e);
//                        throw propagate(e);
//                    }
                }
            };
        }

        @Override
        public IndexerType getIndexerType() {
            return IndexerType.KRAMERIUS_SOLR;
        }

        @Override
        public ListeningExecutorService executorService() {
            return executorService;
        }


}
