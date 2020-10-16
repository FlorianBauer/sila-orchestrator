package de.fau.clients.orchestrator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.ctx.CommandContext;
import de.fau.clients.orchestrator.ctx.FeatureContext;
import de.fau.clients.orchestrator.nodes.FullyQualifiedIdentifier;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.SiLAElement;

/**
 * Utility class for various validation checks.
 */
public final class ValidatorUtils {

    private ValidatorUtils() {
        throw new UnsupportedOperationException("Instantiation not allowed.");
    }

    /**
     * Validates a <code>FullyQualifiedIdentifier</code>. Example:
     * <code>org.silastandard/core/SiLAService/v1</code>.
     *
     * @param fqiType The <code>FullyQualifiedIdentifier</code>-type to validate (e.g.
     * <code>FeatureIdentifier</code>).
     * @param fqiUri The FQI string to validate (e.g.
     * <code>org.silastandard/core/SiLAService/v1</code>).
     * @param featureCtx The feature context holding all type definitions.
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     */
    public static boolean isFullyQualifiedIdentifierValid(
            final String fqiType,
            final String fqiUri,
            final FeatureContext featureCtx
    ) {
        final Collection<FeatureContext> featList = featureCtx.getServerCtx().getFeatureCtxList();
        final String[] sections = fqiUri.split("/");
        if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.FEATURE_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.FEATURE_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final FeatureContext featCtx : featList) {
                if (featCtx.getFullyQualifiedIdentifier().equalsIgnoreCase(fqiUri)) {
                    return true;
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.COMMAND_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.COMMAND_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final FeatureContext featCtx : featList) {
                if (featCtx.getFeatureId().equalsIgnoreCase(sections[2])) {
                    for (final CommandContext cmd : featCtx.getCommandCtxList()) {
                        if (cmd.getCommand().getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.COMMAND_PARAMETER_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.COMMAND_PARAMETER_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final FeatureContext featCtx : featList) {
                if (featCtx.getFeatureId().equalsIgnoreCase(sections[2])) {
                    for (final CommandContext cmd : featCtx.getCommandCtxList()) {
                        if (cmd.getCommand().getIdentifier().equalsIgnoreCase(sections[5])) {
                            for (final SiLAElement param : cmd.getCommand().getParameter()) {
                                if (param.getIdentifier().equalsIgnoreCase(sections[7])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.COMMAND_RESPONSE_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.COMMAND_RESPONSE_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final FeatureContext featCtx : featList) {
                if (featCtx.getFeatureId().equalsIgnoreCase(sections[2])) {
                    for (final CommandContext cmd : featCtx.getCommandCtxList()) {
                        if (cmd.getCommand().getIdentifier().equalsIgnoreCase(sections[5])) {
                            for (final SiLAElement resp : cmd.getCommand().getResponse()) {
                                if (resp.getIdentifier().equalsIgnoreCase(sections[7])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.INTERMEDIATE_COMMAND_RESPONSEIDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.INTERMEDIATE_COMMAND_RESPONSEIDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final FeatureContext featCtx : featList) {
                if (featCtx.getFeatureId().equalsIgnoreCase(sections[2])) {
                    for (final CommandContext cmd : featCtx.getCommandCtxList()) {
                        if (cmd.getCommand().getIdentifier().equalsIgnoreCase(sections[5])) {
                            for (final SiLAElement interResp : cmd.getCommand().getIntermediateResponse()) {
                                if (interResp.getIdentifier().equalsIgnoreCase(sections[7])) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.DEFINED_EXECUTION_ERROR_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.DEFINED_EXECUTION_ERROR_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final FeatureContext featCtx : featList) {
                if (featCtx.getFeatureId().equalsIgnoreCase(sections[2])) {
                    for (final Feature.DefinedExecutionError err : featCtx.getFeature().getDefinedExecutionError()) {
                        if (err.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.PROPERTY_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.PROPERTY_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final FeatureContext featCtx : featList) {
                if (featCtx.getFeatureId().equalsIgnoreCase(sections[2])) {
                    for (final Feature.Property prop : featCtx.getFeature().getProperty()) {
                        if (prop.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.TYPE_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.TYPE_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final FeatureContext featCtx : featList) {
                if (featCtx.getFeatureId().equalsIgnoreCase(sections[2])) {
                    for (final SiLAElement dataTypeDef : featCtx.getFeature().getDataTypeDefinition()) {
                        if (dataTypeDef.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        } else if (fqiType.equalsIgnoreCase(FullyQualifiedIdentifier.METADATA_IDENTIFIER.toString())) {
            if (sections.length < FullyQualifiedIdentifier.METADATA_IDENTIFIER.getSectionCount()) {
                return false;
            }
            for (final FeatureContext featCtx : featList) {
                if (featCtx.getFeatureId().equalsIgnoreCase(sections[2])) {
                    for (final Feature.Metadata meta : featCtx.getFeature().getMetadata()) {
                        if (meta.getIdentifier().equalsIgnoreCase(sections[5])) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the given XML input contains correct, well-formed XML syntax. To validate against a
     * schema, please see <code> isXmlValid()</code>.
     *
     * @param xml The XML input to check.
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     *
     * @see #isXmlValid
     */
    public static boolean isXmlWellFormed(final InputStream xml) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            final DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(null); // suppress error prints
            builder.parse(xml);
            return true;
        } catch (IOException | ParserConfigurationException | SAXException ex) {
            return false;
        }
    }

    /**
     * Validates the given XML data against the provided XML Schema Definition (XSD). If just a
     * check on correct XML syntax is needed, please use <code>isXmlWellFormed()</code> instead.
     *
     * @deprecated This code is experimental and is most likely not conform with the intended
     * behavior described in the SiLA 2 standard (v1.0). However, it serves as a useful placeholder
     * and may be rewritten, extended or removed in the future. (2020-09-05,
     * florian.bauer.dev@gmail.com)
     *
     * @param xml The XML input to validate.
     * @param xsd The stream source of the XSD data to validate against.
     *
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     *
     * @see https://www.edankert.com/validate.html
     * @see https://docs.oracle.com/javase/tutorial/jaxp/dom/validating.html
     * @see #isXmlWellFormed
     */
    @Deprecated
    public static boolean isXmlValid(final InputStream xml, final StreamSource xsd) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(xsd);
            factory.setSchema(schema);
            final Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xml));
            return true;
        } catch (IOException | SAXException ex) {
            return false;
        }
    }

    /**
     * Checks if the given JSON string is valid.
     *
     * @deprecated A proper schema validation is not done yet. This may change in the future when
     * the standardization of JSON-schema is final. A library to do this can be found here:
     * https://github.com/networknt/json-schema-validator (2020-09-05, florian.bauer.dev@gmail.com)
     *
     * @param jsonStr The JSON string to validate.
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     */
    @Deprecated
    public static boolean isJsonValid(final String jsonStr) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonStr);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
