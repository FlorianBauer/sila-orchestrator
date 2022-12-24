package de.fau.clients.orchestrator.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import lombok.NonNull;
import sila_java.library.core.models.Constraints;
import sila_java.library.core.models.DataTypeType;

/**
 * Class with helper functions to manage XML related data.
 */
public final class XmlUtils {

    private static final XmlMapper xmlMapper;

    static {
        final JacksonXmlModule module = new JacksonXmlModule();
        module.setDefaultUseWrapper(false);
        xmlMapper = new XmlMapper(module);
        xmlMapper.registerModule(new JaxbAnnotationModule());
    }

    /**
     * Deserializes the given XML string into a SilA <code>DataTypeType</code>.
     *
     * @param typeAsXml The type encoded as XML string.
     * @return The <code>DataTypeType</code> object form the unmarshalled XML string on success.
     * @throws JsonProcessingException
     * @see ..BasicNodeFactory#createAnyType
     */
    public static DataTypeType parseXmlDataType(@NonNull final String typeAsXml)
            throws JsonProcessingException {
        return xmlMapper.readValue(typeAsXml, DataTypeType.class);
    }

    /**
     * Deserializes the given XML string into a SilA <code>Constraints</code> object.
     *
     * @param constraintsAsXml The constraints type encoded as XML string.
     * @return The <code>Constraints</code> object form the unmarshalled XML string on success.
     * @throws JsonProcessingException
     */
    public static Constraints parseXmlConstraints(@NonNull final String constraintsAsXml)
            throws JsonProcessingException {
        return xmlMapper.readValue(constraintsAsXml, Constraints.class);
    }
}
