package com.ikeu.components.core.utils;

import com.ikeu.components.core.tree.TreeNode;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class TreeUtilsTest {

    @Test
    void buildTree_basic() {
        List<Dept> flatList = Arrays.asList(
                new Dept(1L, 0L, "A"),
                new Dept(2L, 0L, "B"),
                new Dept(3L, 1L, "A-1"),
                new Dept(4L, 1L, "A-2"),
                new Dept(5L, 3L, "A-1-1")
        );

        List<Dept> tree = TreeUtils.buildTree(flatList,
                Dept::getId, Dept::getParentId, (node, children) -> node.setChildren((List) children), 0L);

        assertEquals(2, tree.size(), "2 root nodes");
        Dept rootA = tree.get(0);
        assertEquals(1L, rootA.getId());
        assertEquals(2, rootA.getChildren().size());

        Dept a1 = rootA.getChildren().get(0);
        assertEquals(3L, a1.getId());
        assertEquals(1, a1.getChildren().size());
        assertEquals(5L, a1.getChildren().get(0).getId());
    }

    @Test
    void buildTree_singleRoot() {
        List<Dept> flatList = Collections.singletonList(
                new Dept(1L, 0L, "Only")
        );
        List<Dept> tree = TreeUtils.buildTree(flatList,
                Dept::getId, Dept::getParentId, (node, children) -> node.setChildren((List) children), 0L);
        assertEquals(1, tree.size());
        assertEquals(1L, tree.get(0).getId());
    }

    @Test
    void buildTree_emptyList() {
        List<Dept> tree = TreeUtils.buildTree(Collections.emptyList(),
                Dept::getId, Dept::getParentId, (node, children) -> node.setChildren((List) children), 0L);
        assertTrue(tree.isEmpty());
    }

    @Test
    void getLeafIds() {
        Dept root = new Dept(1L, 0L, "A");
        Dept child = new Dept(2L, 1L, "A-1");
        Dept leaf = new Dept(3L, 2L, "A-1-1");
        root.setChildren(Collections.singletonList(child));
        child.setChildren(Collections.singletonList(leaf));
        leaf.setChildren(Collections.emptyList());

        List<Long> leafIds = TreeUtils.getLeafIds(root);
        assertEquals(1, leafIds.size());
        assertEquals(3L, leafIds.get(0));
    }

    @Test
    void getLeafIds_rootIsLeaf() {
        Dept root = new Dept(1L, 0L, "A");
        root.setChildren(Collections.emptyList());
        List<Long> leafIds = TreeUtils.getLeafIds(root);
        assertEquals(1, leafIds.size());
        assertEquals(1L, leafIds.get(0));
    }

    @Test
    void sortTree() {
        Dept child2 = new Dept(3L, 1L, "Z-item");
        Dept child1 = new Dept(2L, 1L, "A-item");
        Dept root = new Dept(1L, 0L, "Root");
        root.setChildren(Arrays.asList(child2, child1));

        List<Dept> tree = Collections.singletonList(root);
        TreeUtils.sortTree(tree, Comparator.comparing(Dept::getName), Dept::getChildren);

        assertEquals("A-item", root.getChildren().get(0).getName());
        assertEquals("Z-item", root.getChildren().get(1).getName());
    }

    @Test
    void sortTree_nullInput() {
        TreeUtils.sortTree(null, Comparator.comparing(o -> 0), null); // no exception
    }

    @Test
    void sortTree_emptyInput() {
        TreeUtils.sortTree(Collections.emptyList(), Comparator.comparing(o -> 0), null);
    }

    // ── Test TreeNode ──

    @SuppressWarnings("unchecked")
    static class Dept implements TreeNode<Long> {
        private Long id;
        private Long parentId;
        private String name;
        private List<Dept> children = Collections.emptyList();

        Dept() {}
        Dept(Long id, Long parentId, String name) {
            this.id = id;
            this.parentId = parentId;
            this.name = name;
        }

        @Override public Long getId() { return id; }
        @Override public Long getParentId() { return parentId; }
        @Override public List<Dept> getChildren() { return children; }
        @Override public void setChildren(List<? extends TreeNode<Long>> children) {
            this.children = (List<Dept>) children;
        }
        String getName() { return name; }
    }
}