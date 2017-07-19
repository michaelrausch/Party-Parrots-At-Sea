package seng302.models.xml;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import seng302.server.messages.XMLMessageSubType;

import java.io.*;
import java.net.URISyntaxException;

public class XMLGenerator {
    private static final String XML_TEMPLATE_DIR = "/server_config/xml_templates";
    private static final String REGATTA_TEMPLATE_NAME = "regatta.ftlh";
    private Configuration configuration;
    private Regatta regatta;

    /**
     * Set up a configuration instance for Apache Freemake
     */
    private void setupConfiguration() {
        configuration = new Configuration(Configuration.VERSION_2_3_26);

        try {
            configuration.setDirectoryForTemplateLoading(new File(getClass().getResource(XML_TEMPLATE_DIR).toURI()));
        } catch (IOException e){
            System.out.println("[FATAL] Server could not read XML templates");
        } catch (URISyntaxException e) {
            System.out.println("[FATAL] Xml template directory URI is invalid");
        } catch (NullPointerException e){
            System.out.println("[FATAL] Server could not load XML Template directory, ensure this directory isn't empty");
        }
    }

    public XMLGenerator(){
        setupConfiguration();
    }

    public void setRegatta(Regatta regatta){
        this.regatta = regatta;
    }

    private String parseToXmlString(String templateName, XMLMessageSubType type) throws IOException, TemplateException {
        Template template;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(os);

        template = configuration.getTemplate(templateName);

        switch (type) {
            case REGATTA:
                template.process(regatta, writer);
                break;

            case BOAT:
                template.process(regatta, writer);
                break;

            case RACE:
                template.process(regatta, writer);
                break;

            default:
                throw new UnsupportedOperationException();
        }

        try {
            return os.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("[FATAL] UTF-8 Not supported");
            return null;
        }
    }

    public String getRegattaAsXml(){
        String result = null;

        if (regatta == null) return null;

        try {
            result = parseToXmlString(REGATTA_TEMPLATE_NAME, XMLMessageSubType.REGATTA);
        } catch (TemplateException e) {
            System.out.println("[FATAL] Error parsing regatta");
        } catch (IOException e) {
            System.out.println("[FATAL] Error reading regatta");
        }

        return result;
    }



}