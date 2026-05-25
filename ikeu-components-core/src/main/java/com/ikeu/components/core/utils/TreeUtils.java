package com.ikeu.components.core.utils;

import com.ikeu.components.core.tree.TreeNode;
import lombok.NonNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tree utilities: build tree from flat list, extract leaves, sort.
 *
 * <h3>Usage — building a tree</h3>
 * <pre>{@code
 * // Flat department list from DB
 * List<Dept> flatList = deptMapper.selectAll();
 *
 * // Build tree (rootParentId = 0 for top-level)
 * List<Dept> tree = TreeUtils.buildTree(
 *         flatList,
 *         Dept::getId,         // id function
 *         Dept::getParentId,   // parentId function
 *         Dept::setChildren,   // children setter
 *         0L);                 // root parent id value
 *
 * // Extract leaf node IDs
 * List<Long> leafIds = TreeUtils.getLeafIds(rootNode);
 *
 * // Sort tree by order field
 * TreeUtils.sortTree(tree, Comparator.comparing(Dept::getSort), Dept::getChildren);
 * }</pre>
 *
 * <h3>Caveats</h3>
 * <ul>
 *   <li>No cycle detection — ensure the input is acyclic (a DAG or tree)</li>
 *   <li>The {@code childrenFn} parameter is {@code BiConsumer<T, List<? extends T>>}
 *       to support {@code TreeNode::setChildren} method references</li>
 * </ul>
 *
 * @author ikeu
 * @since 1.0.0
 */
public final class TreeUtils {

    private TreeUtils() {
    }

    /**
     * Build a tree from a flat list, linking children to parents by parentId.
     *
     * @param flatList      the flat list of nodes
     * @param idFn          function to get the node's id
     * @param parentIdFn    function to get the node's parent id
     * @param childrenFn    setter for children list
     * @param rootParentId  the parent id value that indicates a root node (e.g. 0 or null)
     * @param <T>           node type
     * @param <ID>          id type
     * @return list of root nodes with children populated
     */
    public static <T, ID> List<T> buildTree(@NonNull List<T> flatList,
                                            @NonNull Function<T, ID> idFn,
                                            @NonNull Function<T, ID> parentIdFn,
                                            @NonNull BiConsumer<T, List<? extends T>> childrenFn,
                                            ID rootParentId) {
        Map<ID, List<T>> parentMap = flatList.stream()
                .collect(Collectors.groupingBy(parentIdFn));
        List<T> roots = new ArrayList<>();
        for (T node : flatList) {
            ID parentId = parentIdFn.apply(node);
            if (Objects.equals(parentId, rootParentId)) {
                roots.add(node);
            }
            List<T> children = parentMap.get(idFn.apply(node));
            if (children != null) {
                childrenFn.accept(node, children);
            } else {
                childrenFn.accept(node, Collections.emptyList());
            }
        }
        return roots;
    }

    /**
     * Extract all leaf node IDs from a tree.
     */
    public static <ID> List<ID> getLeafIds(@NonNull TreeNode<ID> root) {
        List<ID> leafIds = new ArrayList<>();
        collectLeaves(root, leafIds);
        return leafIds;
    }

    private static <ID> void collectLeaves(TreeNode<ID> node, List<ID> leafIds) {
        List<? extends TreeNode<ID>> children = node.getChildren();
        if (children == null || children.isEmpty()) {
            leafIds.add(node.getId());
        } else {
            for (TreeNode<ID> child : children) {
                collectLeaves(child, leafIds);
            }
        }
    }

    /**
     * Sort a tree recursively at each level using the given comparator.
     */
    public static <T> void sortTree(List<T> tree, Comparator<T> comparator,
                                    Function<T, List<T>> childrenFn) {
        if (tree == null || tree.isEmpty()) {
            return;
        }
        tree.sort(comparator);
        for (T node : tree) {
            List<T> children = childrenFn.apply(node);
            if (children != null && !children.isEmpty()) {
                sortTree(children, comparator, childrenFn);
            }
        }
    }
}
