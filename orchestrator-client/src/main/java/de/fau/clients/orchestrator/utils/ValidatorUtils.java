package de.fau.clients.orchestrator.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fau.clients.orchestrator.ctx.CommandContext;
import de.fau.clients.orchestrator.ctx.FeatureContext;
import de.fau.clients.orchestrator.ctx.PropertyContext;
import de.fau.clients.orchestrator.ctx.ServerContext;
import de.fau.clients.orchestrator.nodes.FullyQualifiedIdentifier;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;
import sila_java.library.core.sila.utils.FullyQualifiedIdentifierUtils;

/**
 * Utility class for various validation checks.
 */
public final class ValidatorUtils {

    private static final Map<FullyQualifiedIdentifier,Pattern> StandardMapFqis = Map.of(
            FullyQualifiedIdentifier.FEATURE_IDENTIFIER, FullyQualifiedIdentifierUtils.FullyQualifiedFeatureIdentifierPattern,
            FullyQualifiedIdentifier.COMMAND_IDENTIFIER, FullyQualifiedIdentifierUtils.FullyQualifiedCommandIdentifierPattern,
            FullyQualifiedIdentifier.COMMAND_PARAMETER_IDENTIFIER, FullyQualifiedIdentifierUtils.FullyQualifiedCommandParameterIdentifierPattern,
            FullyQualifiedIdentifier.COMMAND_RESPONSE_IDENTIFIER, FullyQualifiedIdentifierUtils.FullyQualifiedCommandResponseIdentifierPattern,
            FullyQualifiedIdentifier.INTERMEDIATE_COMMAND_RESPONSEIDENTIFIER, FullyQualifiedIdentifierUtils.FullyQualifiedIntermediateCommandResponseIdentifierPattern,
            FullyQualifiedIdentifier.DEFINED_EXECUTION_ERROR_IDENTIFIER, FullyQualifiedIdentifierUtils.FullyQualifiedDefinedExecutionErrorIdentifierPattern,
            FullyQualifiedIdentifier.PROPERTY_IDENTIFIER, FullyQualifiedIdentifierUtils.FullyQualifiedPropertyIdentifierPattern,
            FullyQualifiedIdentifier.TYPE_IDENTIFIER, FullyQualifiedIdentifierUtils.FullyQualifiedCustomDataTypeIdentifierPattern,
            FullyQualifiedIdentifier.METADATA_IDENTIFIER, FullyQualifiedIdentifierUtils.FullyQualifiedMetadataIdentifierPattern
    );

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
     * @param constrainedFeatureCtx The feature context holding all type definitions.
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     */
    public static boolean isFullyQualifiedIdentifierValid(
            final FullyQualifiedIdentifier fqiType,
            final String fqiUri,
            final FeatureContext constrainedFeatureCtx
    ) {
        final boolean isValidFqi = StandardMapFqis.get(fqiType).matcher(fqiUri).matches();
        if (!isValidFqi) {
            return false;
        }
        final String[] sections = fqiUri.split("/");
        final String fullyQualifiedFeatureIdentifier = String.join("/", sections[0], sections[1], sections[2], sections[3]);
        final String callId = (sections.length > 5) ? sections[5] : null;
        final String param = (sections.length > 7) ? sections[7] : null;
        final ServerContext serverCtx = constrainedFeatureCtx.getServerCtx();
        if (serverCtx == null) {
            return false;
        }
        final FeatureContext featureCtx = serverCtx.getFeatureCtx(fullyQualifiedFeatureIdentifier);
        if (featureCtx == null) {
            return false;
        }
        final CommandContext commandCtx = (callId != null) ? featureCtx.getCommandCtx(callId) : null;
        final PropertyContext propertyCtx = (callId != null) ? featureCtx.getPropertyCtx(callId) : null;
        if (fqiType.equals(FullyQualifiedIdentifier.FEATURE_IDENTIFIER)) {
            return featureCtx.getFullyQualifiedIdentifier().equals(fqiUri);
        }
        if (fqiType.equals(FullyQualifiedIdentifier.COMMAND_IDENTIFIER)) {
            return commandCtx != null;
        }
        if (fqiType.equals(FullyQualifiedIdentifier.COMMAND_PARAMETER_IDENTIFIER)) {
            return (
                    commandCtx != null &&
                    commandCtx.getCommand().getParameter()
                            .stream()
                            .anyMatch(p -> p.getIdentifier().equalsIgnoreCase(param))
            );
        }
        if (fqiType.equals(FullyQualifiedIdentifier.COMMAND_RESPONSE_IDENTIFIER)) {
            return (
                    commandCtx != null &&
                            commandCtx.getCommand().getResponse()
                                    .stream()
                                    .anyMatch(r -> r.getIdentifier().equalsIgnoreCase(param))
            );
        }
        if (fqiType.equals(FullyQualifiedIdentifier.INTERMEDIATE_COMMAND_RESPONSEIDENTIFIER)) {
            return (
                    commandCtx != null &&
                            commandCtx.getCommand().getIntermediateResponse()
                                    .stream()
                                    .anyMatch(i -> i.getIdentifier().equalsIgnoreCase(param))
            );
        }
        if (fqiType.equals(FullyQualifiedIdentifier.DEFINED_EXECUTION_ERROR_IDENTIFIER)) {
            return (
                    featureCtx.getFeature().getDefinedExecutionError()
                            .stream()
                            .anyMatch(d -> d.getIdentifier().equalsIgnoreCase(callId))
            );
        }
        if (fqiType.equals(FullyQualifiedIdentifier.PROPERTY_IDENTIFIER)) {
            return propertyCtx != null;
        }
        if (fqiType.equals(FullyQualifiedIdentifier.TYPE_IDENTIFIER)) {
            return (
                    featureCtx.getFeature().getDataTypeDefinition()
                            .stream()
                            .anyMatch(t -> t.getIdentifier().equalsIgnoreCase(callId))
            );
        }
        if (fqiType.equals(FullyQualifiedIdentifier.METADATA_IDENTIFIER)) {
            return (
                    featureCtx.getFeature().getMetadata()
                            .stream()
                            .anyMatch(m -> m.getIdentifier().equalsIgnoreCase(callId))
            );
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
     * @param xml The XML input source to validate.
     * @param xsd The source of the XSD data to validate against.
     *
     * @return <code>true</code> if valid, otherwise <code>false</code>.
     *
     * @see https://www.edankert.com/validate.html
     * @see https://docs.oracle.com/javase/tutorial/jaxp/dom/validating.html
     * @see #isXmlWellFormed
     */
    @Deprecated
    public static boolean isXmlValid(final Source xml, final Source xsd) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final Schema schema = schemaFactory.newSchema(xsd);
            factory.setSchema(schema);
            final Validator validator = schema.newValidator();
            validator.validate(xml);
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

    /**
     * Checks if the given input has a valid UTF-8 encoding.
     *
     * @param input The input data to validate.
     * @return true if the input can be represented as UTF-8 string, otherwise false.
     */
    public static boolean isValidUtf8(byte[] input) {
        final CharsetDecoder cs = StandardCharsets.UTF_8.newDecoder();
        try {
            cs.decode(ByteBuffer.wrap(input));
        } catch (final CharacterCodingException ex) {
            return false;
        }
        return true;
    }
}
