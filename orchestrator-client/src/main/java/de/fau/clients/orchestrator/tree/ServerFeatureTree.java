package de.fau.clients.orchestrator.tree;

import de.fau.clients.orchestrator.Presentable;
import de.fau.clients.orchestrator.dnd.CommandNodeTransferHandler;
import de.fau.clients.orchestrator.nodes.TypeDefLut;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import sila_java.library.core.models.Feature;
import sila_java.library.manager.ServerListener;
import sila_java.library.manager.ServerManager;
import sila_java.library.manager.models.Server;

/**
 * Tree class which shows the available servers and features in a hierarchical ordered view.
 */
@SuppressWarnings("serial")
public final class ServerFeatureTree extends JTree implements Presentable, ServerListener {

    private static final String NO_SERVER_STR = "No Server Available";
    private static final String CATEGORY_CORE = "core";
    private final HashMap<UUID, ServerTreeNode> serverMap = new HashMap<>();

    /**
     * Constructor.
     */
    public ServerFeatureTree() {
        final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(NO_SERVER_STR);
        this.setModel(new DefaultTreeModel(rootNode));
        this.setCellRenderer(new TreeNodeRenderer());
        this.setDragEnabled(true);
        this.setRowHeight(-1);
        this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.setTransferHandler(new CommandNodeTransferHandler());
        this.setDropTarget(null);
        this.setEnabled(false);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 0));
    }

    /**
     * Adds the given server list and all its features to the server tree.
     *
     * @param serverManager The server manager instance.
     * @param serverList The list of servers to add to the tree.
     */
    public void addServersToTree(
            final ServerManager serverManager,
            final Collection<Server> serverList
    ) {
        final DefaultTreeModel model = (DefaultTreeModel) this.treeModel;
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();

        for (final Server server : serverList) {
            final UUID serverUuid = server.getConfiguration().getUuid();
            final ServerTreeNode serverNode = new ServerTreeNode(serverManager, serverUuid);
            serverMap.put(serverUuid, serverNode);
            serverNode.setUserObject(new TreeNodeType(serverNode));
            rootNode.add(serverNode);

            // Sort the feature list and put core freatures at the end.
            server.getFeatures().sort((Feature t, Feature t1) -> {
                if (t.getCategory().startsWith(CATEGORY_CORE)) {
                    return 1;
                } else if (t1.getCategory().startsWith(CATEGORY_CORE)) {
                    return -1;
                }
                return 0;
            });

            for (final Feature feature : server.getFeatures()) {
                final boolean isCoreFeat = feature.getCategory().startsWith(CATEGORY_CORE);
                final FeatureInfoTreeNode featureNode = new FeatureInfoTreeNode(feature);
                featureNode.setUserObject(new TreeNodeType(feature, isCoreFeat));
                serverNode.add(featureNode);

                final TypeDefLut typeDefs = new TypeDefLut(server, feature);
                if (feature.getProperty() != null && !feature.getProperty().isEmpty()) {
                    final DefaultMutableTreeNode propertyNode = new DefaultMutableTreeNode("Properties");
                    featureNode.add(propertyNode);
                    for (final Feature.Property prop : feature.getProperty()) {
                        final PropertyTreeNode ptn = new PropertyTreeNode(
                                server.getConfiguration().getUuid(),
                                feature.getIdentifier(),
                                typeDefs,
                                prop);
                        ptn.setUserObject(new TreeNodeType(prop));
                        propertyNode.add(ptn);
                    }
                }

                if (feature.getCommand() != null && !feature.getCommand().isEmpty()) {
                    final DefaultMutableTreeNode commandNode = new DefaultMutableTreeNode("Commands");
                    featureNode.add(commandNode);
                    for (final Feature.Command command : feature.getCommand()) {
                        final CommandTreeNode ctn = new CommandTreeNode(
                                server.getConfiguration().getUuid(),
                                typeDefs,
                                command);
                        ctn.setUserObject(new TreeNodeType(command));
                        commandNode.add(ctn);
                    }
                }
            }

        }
        model.reload();
        // Expand all nodes in the tree.
        for (int i = 0; i < this.getRowCount(); i++) {
            this.expandRow(i);
        }
    }

    /**
     * Gets the presenter of the selected tree node for the context sensitive view panel.
     *
     * @return The presenter panel according to the selected node.
     */
    @Override
    public JPanel getPresenter() {
        if (isSelectionEmpty()) {
            return null;
        }

        JPanel presenter = null;
        final Object node = getLastSelectedPathComponent();
        if (node instanceof CommandTreeNode) {
            presenter = CommandTreeNode.COMMAND_USAGE_PANEL;
        } else if (node instanceof PropertyTreeNode) {
            final PropertyTreeNode propNode = (PropertyTreeNode) node;
            propNode.requestPropertyData();
            presenter = propNode.getPresenter();
        } else if (node instanceof FeatureInfoTreeNode) {
            final FeatureInfoTreeNode featNode = (FeatureInfoTreeNode) node;
            presenter = featNode.getPresenter();
        } else if (node instanceof ServerTreeNode) {
            final ServerTreeNode serverNode = (ServerTreeNode) node;
            presenter = serverNode.getPresenter();
        }
        return presenter;
    }

    /**
     * Listener for server status (online/offline) which changes the server tree render symbols
     * accordingly.
     *
     * @param uuid The UUID of the changing server.
     * @param server The changing server instance.
     */
    @Override
    public void onServerChange(UUID uuid, Server server) {
        final ServerTreeNode serverNode = serverMap.get(uuid);
        if (serverNode != null) {
            final Object obj = serverNode.getUserObject();
            if (!(obj instanceof TreeNodeType)) {
                return;
            }
            final TreeNodeType ftt = (TreeNodeType) obj;
            if (server.getStatus() == Server.Status.OFFLINE) {
                ftt.setTreeRenderSymbol(TreeRenderSymbol.SERVER_OFFLINE);
            } else {
                ftt.setTreeRenderSymbol(TreeRenderSymbol.SERVER_ONLINE);
            }
            ftt.setDescription(serverNode.getDescription());
            this.repaint();
        }
    }
}
