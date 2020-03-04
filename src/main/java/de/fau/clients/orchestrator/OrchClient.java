package de.fau.clients.orchestrator;

import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import sila2.org.silastandard.SiLAFramework;
import sila2.org.silastandard.core.silaservice.v1.SiLAServiceGrpc;
import sila2.org.silastandard.core.silaservice.v1.SiLAServiceOuterClass;
import sila_java.library.core.models.Feature;
import sila_java.library.core.models.Feature.Command;
import sila_java.library.core.models.Feature.Metadata;
import sila_java.library.core.models.Feature.Property;
import sila_java.library.core.models.SiLAElement;
import sila_java.library.core.sila.clients.ChannelFactory;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;

@Slf4j
public class OrchClient {

    public static void logSilaElementList(final List<SiLAElement> elements) {
        for (final SiLAElement elem : elements) {
            String tail = "\n\t* getDataType: null";
            if (elem.getDataType() != null) {
                if (elem.getDataType().getBasic() != null) {
                    // SiLA basic type
                    tail = "\n\t* getDataTypeBasic: " + elem.getDataType().getBasic().name();
                } else {
                    // SiLA derived type
                    if (elem.getDataType().getConstrained() != null) {
                        tail = "\n\t* getDataTypeConstrained: " + elem.getDataType().getConstrained().toString();
                    } else if (elem.getDataType().getList() != null) {
                        tail = "\n\t* getDataTypeConstrained: " + elem.getDataType().getList().toString();
                    } else if (elem.getDataType().getStructure() != null) {
                        tail = "\n\t* getDataTypeConstrained: " + elem.getDataType().getStructure().toString();
                    } else {
                        // this is illegal by SiLA standard
                        log.warn("SiLAElement: invalid SiLA derived type");
                    }
                }
            } else {
                // TODO: This should not be possible. -> Throw error.
                log.warn("SiLAElement: getDataType() = null");
            }
            log.info("SiLAElement: "
                    + "\n\t* DisplayName: " + elem.getDisplayName()
                    + "\n\t* Description: " + elem.getDescription()
                    + "\n\t* Identifier: " + elem.getIdentifier()
                    + tail);
        }
    }

    public void findServer() throws InterruptedException {
        try (final ServerManager serverManager = ServerManager.getInstance()) {
            serverManager.getDiscovery().scanNetwork();

            ArrayList<Server> serverList = new ArrayList<>(serverManager.getServers().values());
            log.info(serverList.size() + " server found.");

            for (final Server server : serverList) {
                final ManagedChannel serviceChannel = ChannelFactory.withEncryption(server.getHost(), server.getPort());
                try {
                    final SiLAServiceGrpc.SiLAServiceBlockingStub serviceStub = SiLAServiceGrpc.newBlockingStub(serviceChannel);

                    System.out.println("Found Features:");
                    final List<SiLAFramework.String> featureIdentifierList = serviceStub
                            .getImplementedFeatures(SiLAServiceOuterClass.Get_ImplementedFeatures_Parameters.newBuilder().build())
                            .getImplementedFeaturesList();

                    featureIdentifierList.forEach(featureIdentifier
                            -> System.out.println("\t" + featureIdentifier.getValue())
                    );
                } finally {
                    serviceChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
                }

                for (final Feature feat : server.getFeatures()) {
                    log.info("Feature Category: " + feat.getCategory()
                            + "\n\t* DisplayName: " + feat.getDisplayName()
                            + "\n\t* Description: " + feat.getDescription()
                            + "\n\t* FeatureVersion: " + feat.getFeatureVersion()
                            + "\n\t* Identifier: " + feat.getIdentifier()
                            + "\n\t* Locale: " + feat.getLocale()
                            + "\n\t* MaturityLevel: " + feat.getMaturityLevel()
                            + "\n\t* Originator: " + feat.getOriginator()
                            + "\n\t* SiLA2Version: " + feat.getSiLA2Version());

                    for (final Command com : feat.getCommand()) {
                        log.info("Command DisplayName: " + com.getDisplayName()
                                + "\n\t* Description: " + com.getDescription()
                                + "\n\t* Identifier: " + com.getIdentifier()
                                + "\n\t* Observable: " + com.getObservable());

                        OrchClient.logSilaElementList(com.getParameter());
                        OrchClient.logSilaElementList(com.getIntermediateResponse());
                        OrchClient.logSilaElementList(com.getResponse());
                    }

                    OrchClient.logSilaElementList(feat.getDataTypeDefinition());

                    for (final Property prop : feat.getProperty()) {
                        log.info("Property DisplayName: " + prop.getDisplayName()
                                + "\n\t* Description: " + prop.getDescription()
                                + "\n\t* Identifier: " + prop.getIdentifier()
                                + "\n\t* Observable: " + prop.getObservable());
                    }

                    for (final Metadata meta : feat.getMetadata()) {
                        log.info("DisplayName: " + meta.getDisplayName()
                                + "\n\t* Description: " + meta.getDescription()
                                + "\n\t* Identifier: " + meta.getIdentifier());
                    }
                }
            }
        }
    }
}
