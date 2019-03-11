/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.cornell.library.scholars.webapp.controller.api.distribute.AbstractDataDistributor;
import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngine;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchEngineException;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchQuery;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResponse;
import edu.cornell.mannlib.vitro.webapp.modules.searchEngine.SearchResultDocument;

/**
 * TODO
 */
public abstract class AbstractSearchDataDistributor
        extends AbstractDataDistributor {
    @Override
    public String getContentType() throws DataDistributorException {
        return "application/json";
    }

    protected List<Map<String, ?>> runTheQuery(String queryString)
            throws DataDistributorException {
        try {
            SearchEngine searcher = ApplicationUtils.instance()
                    .getSearchEngine();
            SearchQuery query = searcher.createQuery(queryString);
            SearchResponse response = searcher.query(query);
            return convertDocumentsToMaps(response);
        } catch (SearchEngineException e) {
            throw new DataDistributorException(e);
        }
    }

    private List<Map<String, ?>> convertDocumentsToMaps(
            SearchResponse response) {
        List<Map<String, ?>> list = new ArrayList<>();
        for (SearchResultDocument doc : response.getResults()) {
            list.add(convertDocumentToMap(doc));
        }
        return list;
    }

    private Map<String, List<Object>> convertDocumentToMap(
            SearchResultDocument doc) {
        Map<String, List<Object>> map = new HashMap<>();
        Map<String, Collection<Object>> fieldsMap = doc.getFieldValuesMap();
        for (String name : fieldsMap.keySet()) {
            map.put(name, convertValuesToList(fieldsMap.get(name)));
        }
        return map;
    }

    private List<Object> convertValuesToList(Collection<Object> values) {
        return new ArrayList<Object>(values);
    }

    @Override
    public void close() throws DataDistributorException {
        // Nothing to do
    }

}
