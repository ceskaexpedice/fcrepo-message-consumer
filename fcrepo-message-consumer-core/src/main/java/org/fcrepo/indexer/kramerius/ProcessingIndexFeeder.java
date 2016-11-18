package org.fcrepo.indexer.kramerius;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpUtils;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Element;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonReader;


/**
 * This is the helper. It is dedicated ofr creating supporting index which should replace 
 * resource index in the future. 
 * @author pstastny
 *
 */
public class ProcessingIndexFeeder {

    private static final String TYPE_RELATION = "relation";
    private static final String TYPE_DESC = "description";

    public static final Logger LOGGER = Logger.getLogger(ProcessingIndexFeeder.class.getName());
        
    private HttpClient client;

    public ProcessingIndexFeeder() {
        super();
        this.client = HttpClients.createDefault();
    }
    public JsonObject deleteByPid(String pid, String solrHost) throws ClientProtocolException, IOException {
        JsonObject deleteObject = new JsonObject();
        JsonObject queryObject = new JsonObject();
        queryObject.add("query", new JsonPrimitive("source:\""+pid+"\""));
        deleteObject.add("delete", queryObject);
        return deleteByPid(deleteObject, solrHost);
    }    
    
    public JsonObject deleteByPid(JsonObject jsonObj, String solrHost) throws ClientProtocolException, IOException {
        return post(jsonObj, solrHost);
    }

    
    
    public JsonObject feedDescriptionDocument(String pid, String model, String solrHost) throws ClientProtocolException, IOException {
        JsonObject docObject = new JsonObject();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("source", new JsonPrimitive(pid));
        jsonObject.add("type", new JsonPrimitive(TYPE_DESC));
        jsonObject.add("model",new JsonPrimitive(model));
        docObject.add("doc", jsonObject);
        return feedDescriptionDocument(docObject, solrHost);
    }


    public JsonObject feedDescriptionDocument(JsonObject jsonObj, String solrHost) throws ClientProtocolException, IOException {
        JsonObject addOperation = new JsonObject();
        addOperation.add("add", jsonObj);
        return post(addOperation, solrHost);
    }

    private JsonObject post(JsonObject jsonObj, String solrHost) throws IOException, ClientProtocolException {
        String updateEndpoint = solrHost+"/update";
        HttpPost post = new HttpPost(updateEndpoint);
        post.addHeader("Content-Type", "application/json");
        StringEntity ent =  new StringEntity(jsonObj.toString(), StandardCharsets.UTF_8);
        ent.setContentType("application/json");
        post.setEntity(ent);
        HttpResponse response = this.client.execute(post);
        String strresponse = EntityUtils.toString(response.getEntity());
        System.out.println(strresponse);
        JsonReader jsonReader = new JsonReader(new StringReader(strresponse));
        jsonReader.setLenient(true);
        JsonObject fromJson = new Gson().fromJson(jsonReader, JsonObject.class);
        return fromJson;
    }

    public JsonObject feedRelationDocument(String sourePid,  String relation, String targetPid, String solrHost) throws ClientProtocolException, IOException {
        JsonObject docObject = new JsonObject();
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("source", new JsonPrimitive(sourePid));
        jsonObject.add("type", new JsonPrimitive(TYPE_RELATION));
        //jsonObject.put("sourceModel", sourceModel);
        jsonObject.add("relation", new JsonPrimitive(relation));
        jsonObject.add("targetPid", new JsonPrimitive(targetPid));
        docObject.add("doc", jsonObject);
        return feedRelationDocument(docObject, solrHost);
    }

    public JsonObject feedRelationDocument(JsonObject jsonObj, String solrHost) throws ClientProtocolException, IOException {
        JsonObject addOperation = new JsonObject();
        addOperation.add("add", jsonObj);
        return post(addOperation, solrHost);
    }
    
    public static void main(String[] args) throws ClientProtocolException, IOException {
        ProcessingIndexFeeder f = new ProcessingIndexFeeder();
        f.feedDescriptionDocument("uuid:xxxx", "page", "http://localhost:8983/solr/processing");
        f.deleteByPid("uuid:xxxx", "http://localhost:8983/solr/processing");
//        f.feedRelationDocument("xxx","hasPage","yyyy","http://localhost:8983/solr/processing");
        //        String host = KConfiguration.getInstance().getConfiguration().getString("processingSolrHost");
//        (new ProcessingIndexFeeder()).feedDescriptionDocument("xxxx","page", host);
//        (new ProcessingIndexFeeder()).feedRelationDocument("xxxx", "hasPage", "yyyy", host);
    }
}
