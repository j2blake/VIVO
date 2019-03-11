/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.library.scholars.webapp.controller.api.distribute.search;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * TODO
 */
public class SearchByUriDataDistributor extends AbstractSearchDataDistributor{
    private static final String[] NO_URI = {"NOBODY"};
    
    @Override
    public void writeOutput(OutputStream out) throws DataDistributorException {
        String uri = ddContext.getRequestParameters().getOrDefault("uri", NO_URI)[0];
        try {
            Writer writer = new OutputStreamWriter(out);
            writer.write(queryAndConvert(uri));
            writer.close();
        } catch (IOException e) {
            throw new DataDistributorException(e);
        }
    }

    private String queryAndConvert(String uri)
            throws DataDistributorException, JsonProcessingException {
        List<Map<String, ?>> responses = runTheQuery("URI:" + uri);
        return new ObjectMapper().writeValueAsString(responses);
    }
}
