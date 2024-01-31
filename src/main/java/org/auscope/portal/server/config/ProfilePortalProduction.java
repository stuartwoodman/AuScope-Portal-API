package org.auscope.portal.server.config;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.yaml.snakeyaml.Yaml;

/**
 * Definitions for all known layers
 */

@Configuration
@Profile("prod")
public class ProfilePortalProduction {

private final Log log = LogFactory.getLog(getClass());
	
	@Value("${layersFile:layers.yaml}")
    private String layersFile;
    private boolean layersLoaded = false;
    
    public KnownLayer knownType(Map<String, Object> yamlLayer, String id) {
        LayerFactory lf = new LayerFactory(yamlLayer, layersLoaded);
        KnownLayer layer = lf.annotateLayer(id);
        return layer;
    }

    @SuppressWarnings("unchecked")
	@Bean
    public ArrayList<KnownLayer> knownTypes() {
        ArrayList<KnownLayer> knownLayers = new ArrayList<KnownLayer>();
        layersLoaded = true;
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(layersFile);
        Map<String, Object> yamlMenuAndLayers = yaml.load(inputStream);

        if (yamlMenuAndLayers.containsKey("layers")) {
        	ArrayList<Map<String, Object>> yamlLayers = (ArrayList<Map<String, Object>>)yamlMenuAndLayers.get("layers");
        	for (Map<String, Object> yamlLayer: yamlLayers) {
        		yamlLayer.forEach((k, v) -> {
                    String id = k.toString();
                    KnownLayer l =  knownType(yamlLayer, id);
                    if (!l.isHidden()) knownLayers.add(l);
                });
        	}
        } else {
        	log.error("Unable to locate \"layers\" in " + layersFile);
        }        
        return knownLayers;
    }
    
}
