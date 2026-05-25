package com.ikeu.components.core.tree;

import java.util.List;

/**
 * Tree node interface for use with {@link com.ikeu.components.core.utils.TreeUtils}.
 * @author ikeu
 * @since 1.0.0
 */
public interface TreeNode<ID> {

    ID getId();

    ID getParentId();

    void setChildren(List<? extends TreeNode<ID>> children);

    List<? extends TreeNode<ID>> getChildren();
}
