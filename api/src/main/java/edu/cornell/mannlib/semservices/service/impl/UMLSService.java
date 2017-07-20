/* $This file is distributed under the terms of the license in LICENSE$ */

package edu.cornell.mannlib.semservices.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.cornell.mannlib.semservices.bo.Concept;
import edu.cornell.mannlib.semservices.exceptions.ConceptsNotFoundException;
import edu.cornell.mannlib.semservices.service.ExternalConceptService;
import edu.cornell.mannlib.vitro.webapp.utils.json.JacksonUtils;

/**
 * @author jaf30
 *
 */
public class UMLSService implements ExternalConceptService {
   protected final Log logger = LogFactory.getLog(getClass());
   private static final String submissionUrl = "http://link.informatics.stonybrook.edu/MeaningLookup/MlServiceServlet?";
   private static final String baseUri = "http://link.informatics.stonybrook.edu/umls/CUI/";
   private static final String endpoint = "http://link.informatics.stonybrook.edu/sparql/";
   private static final String schemeURI = "http://link.informatics.stonybrook.edu/umls";
   
   
   
	@Override
	public List<Concept> getConcepts(String term) throws Exception {
		List<Concept> conceptList = new ArrayList<Concept>();

		String results = null;
		String dataUrl = submissionUrl + "textToProcess="
				+ URLEncoder.encode(term, "UTF-8") 
				+ "&format=json";

		try {

			StringWriter sw = new StringWriter();
			URL rss = new URL(dataUrl);

			BufferedReader in = new BufferedReader(new InputStreamReader(
					rss.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				sw.write(inputLine);
			}
			in.close();

			results = sw.toString();
			//System.out.println("results before processing: "+results);
			conceptList = processOutput(results);
			return conceptList;

		} catch (Exception ex) {
			logger.error("error occurred in servlet", ex);
			return null;
		}
	}

   public List<Concept> processResults(String term) throws Exception {
      String results = null;
      String dataUrl = submissionUrl + "textToProcess="
            + URLEncoder.encode(term, "UTF-8") + "&format=json";

      try {

         StringWriter sw = new StringWriter();
         URL rss = new URL(dataUrl);

         BufferedReader in = new BufferedReader(new InputStreamReader(rss.openStream()));
         String inputLine;
         while ((inputLine = in.readLine()) != null) {
            sw.write(inputLine);
         }
         in.close();

         results = sw.toString();
         //System.out.println("results before processing: "+results);
         List<Concept> conceptList = processOutput(results);
         return conceptList;

      } catch (Exception ex) {
         logger.error("error occurred in servlet", ex);
         return null;
      }

   }

   /**
    * @param uri URI
    */
	public List<Concept> getConceptsByURIWithSparql(String uri)
			throws Exception {
		// deprecating this method...just return an empty list
		List<Concept> conceptList = new ArrayList<Concept>();
		return conceptList;
	}

   /**
    * @param results Results to process
    */
   private List<Concept> processOutput(String results) throws Exception {

      List<Concept> conceptList = new ArrayList<Concept>();
      List<String> bestMatchIdList = new ArrayList<String>();
      String bestMatchId = new String();
      boolean bestMatchFound = false;
      boolean allFound = false;

      try {
         ObjectNode json = (ObjectNode) JacksonUtils.parseJson(results);
         //System.out.println(json.toString());
         if (json.has("Best Match")) {
            bestMatchFound = true;
            //System.out.println("Best Match");

            ArrayNode bestMatchArray = (ArrayNode) json.get("Best Match");
            int len = bestMatchArray.size();
            if (len > 1) {
               logger.debug("Found this many best matches: "+ len);
            }
            int i;
            for (i = 0; i < len; i++) {
               ObjectNode o = (ObjectNode) bestMatchArray.get(i);
               //System.out.println(o.toString());
               Concept concept = new Concept();
               concept.setDefinedBy(schemeURI);
               concept.setBestMatch("true");
               String cui = getJsonValue(o, "CUI");
               bestMatchIdList.add(cui);

               concept.setConceptId(cui);
               concept.setLabel(getJsonValue(o, "label"));
               concept.setType(getJsonValue(o, "type"));
               concept.setDefinition(getJsonValue(o, "definition"));
               concept.setUri(baseUri + cui);
               concept.setSchemeURI(schemeURI);
               conceptList.add(concept);
            }
         }
         if (json.has("All")) {
            allFound = true;
            ArrayNode allArray = (ArrayNode) json.get("All");
            int len = allArray.size();
            //System.out.println("size of best match array: "+ len);
            int i;
            for (i = 0; i < len; i++) {
               ObjectNode o = (ObjectNode) allArray.get(i);
               //System.out.println(o.toString());
               Concept concept = new Concept();
               concept.setDefinedBy(schemeURI);
               String cui = getJsonValue(o, "CUI");
               concept.setConceptId(cui);

               concept.setLabel(getJsonValue(o, "label"));
               concept.setType(getJsonValue(o, "type"));
               concept.setDefinition(getJsonValue(o, "definition"));
               concept.setUri(baseUri + cui);
               concept.setSchemeURI(schemeURI);
               // prevent duplicate concepts in list
               if (! bestMatchIdList.contains(cui)) {
                  concept.setBestMatch("false");
                  conceptList.add(concept);
               }
            }
         }
      } catch (Exception ex ) {
         ex.printStackTrace();
         logger.error("Could not get concepts", ex);
         throw ex;
      }
      if (! bestMatchFound && !allFound) {
         // we did not get a bestMatch or All element
         throw new ConceptsNotFoundException();
      }

      //
      return conceptList;

   }

   /**
    * Get a string from a json object or an empty string if there is no value for the given key
   * @param obj JSON Object
   * @param key Key to retrieve
   */
  protected String getJsonValue(ObjectNode obj, String key) {
      if (obj.has(key)) {
         return obj.get(key).asText();
      } else {
         return new String("");
      }
   }



  protected String stripConceptId(String uri) {
     String conceptId = new String();
     int lastslash = uri.lastIndexOf('/');
     conceptId = uri.substring(lastslash + 1, uri.length());
     return conceptId;
  }

}
