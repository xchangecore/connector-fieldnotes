package com.saic.uicds.structuredreader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.uicds.incident.UICDSIncidentType;
import org.uicds.incidentManagementService.CreateIncidentRequestDocument;
import org.uicds.incidentManagementService.CreateIncidentResponseDocument;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.CharacterData;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import com.saic.precis.x2009.x06.base.ProcessingStateType;
import com.saic.precis.x2009.x06.base.PropertiesType;
import com.saic.precis.x2009.x06.base.ProcessingStateType.Enum;
import com.saic.uicds.clients.util.Common;



public class JSONReader {
	
  private static WebServiceTemplate wsClient;
  private static final String APP_CONTEXT_FILE = "json2soi-context.xml";
  
  private static XmlObject sendAndReceive(XmlObject request) {

      XmlObject response = null;
      try {
          response = (XmlObject) wsClient.marshalSendAndReceive(request);
      } catch (Throwable e) {
          System.out.println("WebServiceClient: Request:\n" +
                       request.toString() +
                       "\nFailure: " +
                       e.getMessage());

          //can not find that one, so, a null point warning, not failure.
          //logger.warn("WebServiceClient: Request:\n" + request.toString() + "\nWarning: "
          //      + e.getMessage());
      }
      return response;
  }
  
  public static String getCharacterDataFromElement(Element e) {
	    Node child = e.getFirstChild();
	    if (child instanceof CharacterData) {
	      CharacterData cd = (CharacterData) child;
	      return cd.getData();
	    }
	    return "";
	  }
	

  public static String Document2String(Document doc) throws IOException, TransformerException {

      final ByteArrayOutputStream baos = new ByteArrayOutputStream();
      final TransformerFactory tf = TransformerFactory.newInstance();
      final Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

      transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(baos,
          "UTF-8")));
      return baos.toString("UTF-8");
  }
  
  private static String createOnCore(UICDSIncidentType incident) {

      String incidentID = null;
      CreateIncidentRequestDocument request = CreateIncidentRequestDocument.Factory.newInstance();
      
      request.addNewCreateIncidentRequest().setIncident(incident);
      System.out.println("incident data: " + incident.toString());

      try {

          CreateIncidentResponseDocument response = (CreateIncidentResponseDocument) sendAndReceive(request);

          Enum status = response.getCreateIncidentResponse().getWorkProductPublicationResponse().getWorkProductProcessingStatus().getStatus();

          // If the create was accepted then get the id and all the associated
          // documents
          if (status == ProcessingStateType.ACCEPTED) {

              // Get the incident id
              PropertiesType properties = Common.getPropertiesElement(response.getCreateIncidentResponse().getWorkProductPublicationResponse().getWorkProduct());
              if (properties != null
                  && properties.getAssociatedGroups().sizeOfIdentifierArray() > 0) {
                  incidentID = properties.getAssociatedGroups().getIdentifierArray(0).getStringValue();
              } else {
                  System.out.println("Properties not found for new incident work product");
              }
          }

      } catch (ClassCastException e) {
          System.out.println("Error casting response to CreateIncidentResponseDocument");
          incidentID = null;
      }

      return incidentID;
  }
  
  public static void main(String[] args) throws Exception {
	  
	ApplicationContext context = getApplicationContext();  
	
    String incidentID =  "";

	System.out.println("Initializing Saxon for XLST 2.0...");
	// set up saxon for xslt 2.0 use
    System.setProperty("javax.xml.transform.TransformerFactory",
			   "net.sf.saxon.TransformerFactoryImpl");
	  
	String previdStr = "";
	ArrayList<Integer> previds = new ArrayList<Integer>();
	
    // Load the config file	  
	System.out.println("Loading config file " + args[0]);
    Properties props = new Properties();
    String fileName = args[0];
    InputStream is = new FileInputStream(fileName);
    props.load(is);

    // create temp file if it doesnt exist
    System.out.println("Using temp file " + props.getProperty("tmpfile"));
	File f = new File(props.getProperty("tmpfile"));
	System.out.println(f.getAbsolutePath());
	f.createNewFile();

    // and then read the ids from it.
    try {
        is = new FileInputStream(props.getProperty("tmpfile"));
	    InputStreamReader isReader = new InputStreamReader(is);  
	    BufferedReader tmpReader = new BufferedReader(isReader);
	    previdStr = tmpReader.readLine();

	    if (previdStr==null) {
	    	// make ids an empty set so we don't break the xslt
	    	System.out.println("No previous ids found.");
	    } else {
		    // construct previous id sequence
	    	String[] split = previdStr.split(",");
	    	for (int i=0; i < split.length; i++) {
	    		previds.add(new Integer(split[i]));
	    	}
		    System.out.println("Found previous id list: " + previds);
	    }
    } catch (Exception e) {
    	System.err.println("An error occurred reading the temp file.");
    }
    
    // transform JSON to XML
    InputStream jsonIS = null;
    if (props.getProperty("jsonurl") != null) {
    	System.out.println("Reading json from url: " + props.getProperty("jsonurl"));
    	URL url = new URL(props.getProperty("jsonurl"));
    	jsonIS = url.openStream();
    } else {
    	System.out.println("Reading json from file: " + props.getProperty("jsonfile"));
    	jsonIS = new FileInputStream(props.getProperty("jsonfile"));
    }
    String jsonData = IOUtils.toString(jsonIS);
    
    XMLSerializer serializer = new XMLSerializer(); 
    JSON json = JSONSerializer.toJSON( jsonData ); 
    
    serializer.setRootName("JSON");
    serializer.setTypeHintsEnabled(false);
    String xml = serializer.write( json );  

    if (props.containsKey("debug")) {
    	System.out.println("\n\n\n" + xml + "\n\n\n");
    }
    
    // get the new id list and write to the temp file.
    // convert the json string to an inputsource
    System.out.println("Extracting new id list...");
    InputSource jsonSource = new InputSource(new StringReader(xml));
    // Select ids via xpath
    XPathFactory xPathFactory = XPathFactory.newInstance();
    XPath xPath = xPathFactory.newXPath();
    String expression = "//id/text()";
    XPathExpression xPathExpression = xPath.compile(expression);
    Object xpathresult = xPathExpression.evaluate(jsonSource, XPathConstants.NODESET);
    NodeList nodeList = (NodeList) xpathresult;
    String newids = "";
    for (int i = 0; i < nodeList.getLength(); i++) {
    	newids = newids + nodeList.item(i).getNodeValue();
    	if (i < nodeList.getLength()-1)
    			newids = newids + ",";
    }
    
    // write the newids back to the temp file
    System.out.println("Found the following ids: " + newids);
    System.out.println("Writing ids to file: " + props.getProperty("tmpfile"));
    BufferedWriter writer = null;
    try
    {
            writer = new BufferedWriter( new FileWriter( props.getProperty("tmpfile")));
            writer.write( newids);
    }
    catch ( IOException e)
    {
    }
    finally
    {
            try
            {
                    if ( writer != null)
                            writer.close( );
            }
            catch ( IOException e)
            {
            }
    }
    
    // setup xsl transformation
    System.out.println("Setting xsl transform: " + props.getProperty("xslt"));    
    // xml stream source (from xml'd json string)
    StreamSource xmlInput = new StreamSource(new ByteArrayInputStream(xml.getBytes()));
    
    // xslt source (from a file)
    File xsltFile = new File(props.getProperty("xslt"));
    StreamSource xslInput = new StreamSource(xsltFile);
    
    // xml output sink
    StreamResult xmlOutput = new StreamResult(new StringWriter());
    
    Transformer transformer = TransformerFactory.newInstance().newTransformer(xslInput);
    
    int[] grr = new int[previds.size()];
    for (int i=0; i < previds.size(); i++) {
    	grr[i] = previds.get(i).intValue();
    }
    
    transformer.setParameter("previds", grr ) ;
    
    if (props.containsKey("incidentid")) {
    	transformer.setParameter("incidentid", props.getProperty("incidentid") ) ;
    }

    // Execute the transform
    transformer.transform(xmlInput, xmlOutput);

    // get the result as a string (inefficient?)
    String xmlText = xmlOutput.getWriter().toString();

    // convert to an inputsource
    InputSource source2 = new InputSource(new StringReader(xmlText)); 
    
    // Create a namespace context
    NamespaceContext nscontext = new NamespaceContext() {
        public String getNamespaceURI(String prefix) {
            String uri;
            if (prefix.equals("soapenv"))
                uri = "http://schemas.xmlsoap.org/soap/envelope/";
            else
                uri = null;
            return uri;
        }
       
        // Dummy implementation - not used!
        public Iterator getPrefixes(String val) {
            return null;
        }
       
        // Dummy implementation - not used!
        public String getPrefix(String uri) {
            return null;
        }
    };
    // Select each SOI result via xpath
    xPath.setNamespaceContext(nscontext);
    expression = "//soapenv:Envelope";
    xPathExpression = xPath.compile(expression);
    xpathresult = xPathExpression.evaluate(source2, XPathConstants.NODESET);
    
    nodeList = (NodeList) xpathresult;
    System.out.println("Processing " + nodeList.getLength() + " items...");
    for (int i = 0; i < nodeList.getLength(); i++) {
    	StringWriter sw = new StringWriter(); 
    	Transformer tmp = TransformerFactory.newInstance().newTransformer(); 
    	tmp.transform(new DOMSource(nodeList.item(i)), new StreamResult(sw));	
    	String finalXML = sw.toString();
    	if (props.containsKey("createIncident")) {
    		//This will create an incident for each SOI that does not have 
    		//an associated ig
    		System.out.println("contain props key createIncident");
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		Document doc = dBuilder.newDocument();
    		doc = dBuilder.parse(new InputSource(new StringReader(finalXML)));
    		doc.getDocumentElement().normalize();
    		NodeList nList = doc.getElementsByTagName("sen:CreateSOIRequest");
    		Node nNode = nList.item(0);
    		Element element = (Element)nNode;
    		NodeList incidentid = element.getElementsByTagName("sen:incidentID");
    		Element incid = (Element)incidentid.item(0);
    		//System.out.println("element to string " + element.toString());
    		NodeList name = element.getElementsByTagName("sen:name");
    		Element nm = (Element)name.item(0);
    		NodeList latitude = element.getElementsByTagName("sen:latitude");
    		Element latit = (Element)latitude.item(0);
    		NodeList longitude = element.getElementsByTagName("sen:longitude");
    		Element longit = (Element)longitude.item(0);
    		NodeList description = element.getElementsByTagName("sen:description");
    		Element desc = (Element)description.item(0);
    		
    		
    		
    		if (getCharacterDataFromElement(incid).isEmpty()){
    			
    		//create incident
    			if (props.containsKey("debug")) {
        	    	System.out.println("incident id is empty"  );
        	    }
    	    	String incidentName = "Clearinghouse Field Notes-" + getCharacterDataFromElement(nm);
    	    	UICDSIncidentType incident = UICDSIncidentType.Factory.newInstance();
    	    	String activityCategory = "Observation-" + getCharacterDataFromElement(nm);
    	        if (incident.sizeOfActivityCategoryTextArray() < 1) {
    	            incident.addNewActivityCategoryText();
    	            incident.getActivityCategoryTextArray(0).setStringValue(activityCategory);
    	        }
    	        incident.addNewActivityName().setStringValue(incidentName);
    	        incident.addNewActivityDescriptionText().setStringValue(getCharacterDataFromElement(desc));
    	        incident.addNewIncidentLocation().addNewLocationArea().addNewAreaCircularRegion().addNewCircularRegionCenterCoordinate().addNewGeographicCoordinateLatitude().addNewLatitudeDegreeValue();
    	        incident.addNewIncidentEvent().addNewActivityCategoryText();
    			if (incident.sizeOfIncidentLocationArray() == 0) {
    				incident.addNewIncidentLocation();
    			}
    			if (incident.getIncidentLocationArray(0).sizeOfLocationAreaArray() < 1) {
    				incident.getIncidentLocationArray(0).addNewLocationArea();
    			}
    			if (incident.getIncidentLocationArray(0).getLocationAreaArray(0)
    					.sizeOfAreaCircularRegionArray() < 1) {
    				incident.getIncidentLocationArray(0).getLocationAreaArray(0)
    						.addNewAreaCircularRegion();
    			}

    			incident.getIncidentLocationArray(0).getLocationAreaArray(0)
    					.getAreaCircularRegionArray(0)
    					.set(Common.createCircle(getCharacterDataFromElement(latit), getCharacterDataFromElement(longit)));
    			
    		    //incident.getIncidentEventArray(0).getActivityCategoryTextArray(0).setStringValue("");
    			incidentID = createOnCore(incident);
    			incid.setTextContent(incidentID);
    			System.out.println("Created IG = " + incidentID);
    		}
    		System.out.println("The Assoc IG = " + getCharacterDataFromElement(incid));    		
    		finalXML = Document2String(doc);
    	}
    	if (props.containsKey("debug")) {
    		//just print out messages
    		System.out.println("---------\n" + finalXML + "\n\n");
    	} else {
    		// HTTP Post to UICDS
   			String connectionURL = props.getProperty("protocol") +
    									"://" + 
    									props.getProperty("hostname") +
    									"/uicds/core/ws/services";
    			System.out.println("Using connection: " + connectionURL);
    			
    			DefaultHttpClient client = new DefaultHttpClient();
    			
    			UsernamePasswordCredentials credentials = 
    					new UsernamePasswordCredentials(props.getProperty("user"), props.getProperty("password"));
    			
    			client.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
    			HttpPost post = new HttpPost(connectionURL);
    			
    			post.setHeader("Content-type", "text/xml");
    			post.setEntity(new StringEntity(finalXML));
    			
    			HttpResponse response = client.execute(post);
    			System.out.println("Received HTTP response: " + response.getStatusLine());
    	}
    }
  
    System.out.println("Completed.");
  }
  
  public void setWsClient(WebServiceTemplate wsClient) {

      this.wsClient = wsClient;
  }
  
  private static ApplicationContext getApplicationContext() {

      ApplicationContext context = null;
      try {
          context = new FileSystemXmlApplicationContext("./" + APP_CONTEXT_FILE);
          System.out.println("Using local application context file: " + APP_CONTEXT_FILE);
      } catch (BeansException e) {
          if (e.getCause() instanceof FileNotFoundException) {
              System.out.println("Local application context file not found so using file from jar: contexts/"
                  + APP_CONTEXT_FILE);
          } else {
              // System.out.println("Error reading local file context: " +
              // e.getCause().getMessage());
              e.printStackTrace();
          }
      }

      if (context == null) {
          context = new ClassPathXmlApplicationContext(new String[] { "contexts/"
              + APP_CONTEXT_FILE });
      }

      return context;
  }
}
