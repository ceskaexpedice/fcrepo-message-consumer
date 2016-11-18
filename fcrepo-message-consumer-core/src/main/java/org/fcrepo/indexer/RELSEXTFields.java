package org.fcrepo.indexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;

/**
 * RELS-EXT item represantion dedicated for feeding process index
 * @author pstastny
 */
public class RELSEXTFields {
    // source pid
    private String source;
    // source model
    private String sourceModel;
    // relations
    private List<RELSEXTFieldsRelation> relations = new ArrayList<>();
    
    /**
     * Add new RELS-EXT relation
     * @param relation type of relation (hasPage, hasVolume,...)
     * @param targetPid Target pid
     */
    public void addRelation(String relation, String targetPid) {
        this.addRelation(new RELSEXTFieldsRelation(relation, targetPid));
    }
    /**
     * Add new RELS-EXT relation
     * @param r
     */
    public void addRelation(RELSEXTFieldsRelation r) {
        this.relations.add(r);
    }
    
    /**
     * Remove RELS-EXT relation
     * @param r
     */
    public void removeRelation(RELSEXTFieldsRelation r) {
        this.relations.remove(r);
    }
    
    /**
     * Return  all relations
     * @return
     */
    public RELSEXTFieldsRelation[] getRelations() {
        return this.relations.toArray(new RELSEXTFieldsRelation[this.relations.size()]);
    }
    
    /**
     * Represents RELS-EXT relation
     */
    public class RELSEXTFieldsRelation {

        private String relation;
        private String targetPid;

        
        public RELSEXTFieldsRelation(String relation, String targetPid) {
            super();
            this.relation = relation;
            this.targetPid = targetPid;
        }

        /**
         * Return type of relation
         * @return
         */
        public String getRelation() {
            return relation;
        }
        
        
        /**
         * Set type of relation
         * @param relation
         */
        public void setRelation(String relation) {
            this.relation = relation;
        }
        
        /**
         * Get target field
         * @return
         */
        public String getTargetPid() {
            return targetPid;
        }
        
        
        /**
         * Sets target pid
         * @param targetPid
         */
        public void setTargetPid(String targetPid) {
            this.targetPid = targetPid;
        }
    }
    
    /**
     * Return source pid
     * @return
     */
    public String getSource() {
        return source;
    }

    /**
     * Set source pid
     * @param source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Get source model
     * @return
     */
    public String getSourceModel() {
        return sourceModel;
    }

    /**
     * Set source model
     * @param sourceModel
     */
    public void setSourceModel(String sourceModel) {
        this.sourceModel = sourceModel;
    }
}
