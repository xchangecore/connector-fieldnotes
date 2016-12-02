<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">
    
    <xsl:output method="xml" indent="yes" encoding="ISO-8859-1" omit-xml-declaration="no" />
    
    <xsl:param name="previds"  />
    <xsl:param name="incidentid">not-provided</xsl:param> 
    
    <xsl:template match="/">
        <foo>
            <xsl:for-each select="JSON/features/e[not(id = $previds )]">
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                    xmlns:sen="http://uicds.org/SensorService">
                    <soapenv:Header />
		    <soapenv:Body>
                        <sen:CreateSOIRequest>
			    <sen:incidentID>
			        <xsl:choose>
                                   <xsl:when test="properties/igid[not(text())]">
				      <xsl:value-of select="$incidentid" />
			           </xsl:when>
			           <xsl:otherwise>
					   <xsl:value-of select="properties/igid/text()"/>
			           </xsl:otherwise>
			        </xsl:choose>		   
			    </sen:incidentID>
                            <sen:SensorObservationInfo>
                                <sen:sosURN>http://uicds.us/SensorService</sen:sosURN>
                                <sen:SensorInfo>
                                    <sen:id>SENSOR_<xsl:value-of select="id/text()"/></sen:id>
                                    <sen:name><xsl:value-of select="properties/form/text()"/></sen:name>
                                    <xsl:call-template name="description"/>
                                    <sen:latitude><xsl:value-of select="geometry/coordinates/e[2]/text()"/></sen:latitude>
                                    <sen:longitude><xsl:value-of select="geometry/coordinates/e[1]/text()"/></sen:longitude>
                                </sen:SensorInfo>
                            </sen:SensorObservationInfo>
                        </sen:CreateSOIRequest>
                    </soapenv:Body>
                </soapenv:Envelope>
            </xsl:for-each>
        </foo>
    </xsl:template>
    
    <xsl:template name="description">
        <sen:description xmlns:sen="http://uicds.org/SensorService">
            &lt;br /&gt;
            <xsl:choose>
		<xsl:when test="properties/form/text()='Fault Rupture'">
	            <xsl:apply-templates select="properties/form" />		    
		    <xsl:apply-templates select="properties/site" />
		    <xsl:apply-templates select="properties/operator" />
                    <xsl:apply-templates select="properties/surface" />
                    <xsl:apply-templates select="properties/offset" />
                    <xsl:apply-templates select="properties/azimuth" />
                    <xsl:apply-templates select="properties/plunge" />
                    <xsl:apply-templates select="properties/length" />
                    <xsl:apply-templates select="properties/notes" />
                </xsl:when>
		<xsl:when test="properties/form/text()='Liquefaction'">
		    <xsl:apply-templates select="properties/form" />	
		    <xsl:apply-templates select="properties/site" />
		    <xsl:apply-templates select="properties/operator" />
		    <xsl:apply-templates select="properties/blows" />
                    <xsl:apply-templates select="properties/settlement" />
                    <xsl:apply-templates select="properties/spreading" />
                    <xsl:apply-templates select="properties/horizontal" />
                    <xsl:apply-templates select="properties/lateral" />
                    <xsl:apply-templates select="properties/notes" />
                </xsl:when>
		<xsl:when test="properties/form/text()='Landslide'">
		    <xsl:apply-templates select="properties/form" /> 	 
		    <xsl:apply-templates select="properties/site" />
                    <xsl:apply-templates select="properties/operator" />
                    <xsl:apply-templates select="properties/slide" />
                    <xsl:apply-templates select="properties/material" />
                    <xsl:apply-templates select="properties/area" />
                    <xsl:apply-templates select="properties/facilities" />
                    <xsl:apply-templates select="properties/notes" />
	         </xsl:when>
		 <xsl:when test="properties/form/text()='Tsunami'">
		    <xsl:apply-templates select="properties/form" />	 
		    <xsl:apply-templates select="properties/site" />
                    <xsl:apply-templates select="properties/operator" />
                    <xsl:apply-templates select="properties/photo" />
                    <xsl:apply-templates select="properties/inundation" />
                    <xsl:apply-templates select="properties/height" />
                    <xsl:apply-templates select="properties/peaktrough" />
		    <xsl:apply-templates select="properties/cycle" />
                    <xsl:apply-templates select="properties/damage" />
		    <xsl:apply-templates select="properties/notes" />
	        </xsl:when>
		<xsl:when test="properties/form/text()='Lifelines'">
		    <xsl:apply-templates select="properties/form" />	
		    <xsl:apply-templates select="properties/recorded" />
		    <xsl:apply-templates select="properties/operator" />
		    <xsl:apply-templates select="properties/site" />
                    <xsl:apply-templates select="properties/photo" />
                    <xsl:apply-templates select="properties/communication" />
                    <xsl:apply-templates select="properties/power" />
                    <xsl:apply-templates select="properties/other" />
		    <xsl:apply-templates select="properties/functionality" />
		    <xsl:apply-templates select="properties/repair" />
		    <xsl:apply-templates select="properties/investigation" />
		    <xsl:apply-templates select="properties/notes" />
		    <xsl:apply-templates select="properties/description" />
		    <xsl:apply-templates select="properties/accuracy" />
	         </xsl:when>
		 <xsl:when test="properties/form/text()='Buildings'">
		    <xsl:apply-templates select="properties/form" />	 
		    <xsl:apply-templates select="properties/recorded" />
                    <xsl:apply-templates select="properties/synced" />
                    <xsl:apply-templates select="properties/operator" />
                    <xsl:apply-templates select="properties/site" />
                    <xsl:apply-templates select="properties/photo" />
                    <xsl:apply-templates select="properties/type" />
		    <xsl:apply-templates select="properties/use" />
                    <xsl:apply-templates select="properties/stories" />
		    <xsl:apply-templates select="properties/damage" />
		    <xsl:apply-templates select="properties/investigation" />
		    <xsl:apply-templates select="properties/notes" />
		    <xsl:apply-templates select="properties/description" />
		    <xsl:apply-templates select="properties/accuracy" />
	     </xsl:when>
	     <xsl:when test="properties/form/text()='General'">
	        <xsl:apply-templates select="properties/notes" />  
	     </xsl:when>
            </xsl:choose>
        </sen:description>
    </xsl:template>

    <xsl:template match="properties/form">
        &lt;b&gt;Form:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
 
    <xsl:template match="properties/site">
        &lt;b&gt;Site:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/operator">
        &lt;b&gt;Operator:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/surface">
        &lt;b&gt;Surface:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/offset">
        &lt;b&gt;Offset:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/azimuth">
        &lt;b&gt;Azimuth:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/plunge">
        &lt;b&gt;Plunge:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/length">
        &lt;b&gt;Length:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/notes">
        &lt;b&gt;Notes:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/blows">
        &lt;b&gt;Blows:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/settlement">
        &lt;b&gt;Settlement:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/spreading">
        &lt;b&gt;Spreading:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/horizontal">
        &lt;b&gt;Horizontal:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/lateral">
        &lt;b&gt;Lateral:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/slide">
        &lt;b&gt;Slide:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/material">
        &lt;b&gt;Material:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/area">
        &lt;b&gt;Area:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>
    
    <xsl:template match="properties/facilities">
        &lt;b&gt;Facilities:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/photo">
        &lt;b&gt;Photo:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/inundation">
        &lt;b&gt;Inundation:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

     <xsl:template match="properties/height">
        &lt;b&gt;Height:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/peaktrough">
        &lt;b&gt;Peaktrough:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

     <xsl:template match="properties/cycle">
        &lt;b&gt;Cycle:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/damage">
        &lt;b&gt;Damage:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/recorded">
        &lt;b&gt;Recorded:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/synced">
        &lt;b&gt;Synced:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/communication">
        &lt;b&gt;Communication:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/power">
        &lt;b&gt;Power:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/other">
        &lt;b&gt;Other:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/functionality">
        &lt;b&gt;Functionality:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/repair">
        &lt;b&gt;Repair:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/investigation">
        &lt;b&gt;Investigation:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/description">
        &lt;b&gt;Location Description:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/accuracy">
        &lt;b&gt;Accuracy:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/type">
        &lt;b&gt;Type:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template> 
   
    <xsl:template match="properties/use">
        &lt;b&gt;Use:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

    <xsl:template match="properties/stories">
        &lt;b&gt;Stories:&lt;/b&gt; <xsl:value-of select="text()"/> &lt;br /&gt;
    </xsl:template>

</xsl:stylesheet>
