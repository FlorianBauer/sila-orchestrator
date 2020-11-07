package de.fau.clients.orchestrator.tree;

import de.fau.clients.orchestrator.Presentable;
import de.fau.clients.orchestrator.ctx.CommandContext;
import de.fau.clients.orchestrator.ctx.FeatureContext;
import de.fau.clients.orchestrator.ctx.PropertyContext;
import de.fau.clients.orchestrator.ctx.ServerContext;
import de.fau.clients.orchestrator.dnd.CommandNodeTransferHandler;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import lombok.NonNull;
import sila_java.library.core.models.Feature;
import sila_java.library.manager.ServerListener;
import sila_java.library.manager.models.Server;

/**
 * Tree class which shows the available servers and features in a hierarchical ordered view.
 */
@SuppressWarnings("serial")
public final class ServerFeatureTree extends JTree implements Presentable, ServerListener {

    private static final String NO_SERVER_STR = "No Server Available";
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
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    /**
     * Adds the given server list and all its features to the server tree.
     *
     * @param serverCtxList The list of servers to add to the tree.
     */
    public void addServersToTree(@NonNull final Collection<ServerContext> serverCtxList) {
        final DefaultTreeModel model = (DefaultTreeModel) this.treeModel;
        final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) model.getRoot();

        for (final ServerContext serverCtx : serverCtxList) {
            final UUID serverUuid = serverCtx.getServerUuid();
            final ServerTreeNode serverNode = new ServerTreeNode(serverCtx);
            serverMap.put(serverUuid, serverNode);
            serverNode.setUserObject(new TreeNodeType(serverNode));
            rootNode.add(serverNode);

            for (final FeatureContext featCtx : serverCtx.getFeatureCtxList()) {
                final Feature feature = featCtx.getFeature();
                final FeatureInfoTreeNode featureNode = new FeatureInfoTreeNode(featCtx);
                featureNode.setUserObject(new TreeNodeType(feature, featCtx.isCoreFeature()));
                serverNode.add(featureNode);

                final Collection<PropertyContext> propCtxList = featCtx.getPropertyCtxList();
                if (!propCtxList.isEmpty()) {
                    final DefaultMutableTreeNode propertyNode = new DefaultMutableTreeNode("Properties");
                    featureNode.add(propertyNode);
                    for (final PropertyContext propCtx : propCtxList) {
                        final PropertyTreeNode ptn = new PropertyTreeNode(propCtx);
                        ptn.setUserObject(new TreeNodeType(propCtx.getProperty()));
                        propertyNode.add(ptn);
                    }
                }

                final Collection<CommandContext> cmdCtxList = featCtx.getCommandCtxList();
                if (!cmdCtxList.isEmpty()) {
                    final DefaultMutableTreeNode commandNode = new DefaultMutableTreeNode("Commands");
                    featureNode.add(commandNode);
                    for (final CommandContext cmdCtx : cmdCtxList) {
                        final CommandTreeNode ctn = new CommandTreeNode(cmdCtx);
                        ctn.setUserObject(new TreeNodeType(cmdCtx.getCommand()));
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
