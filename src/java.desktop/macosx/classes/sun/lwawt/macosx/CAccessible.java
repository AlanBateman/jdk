/*
 * Copyright (c) 2011, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package sun.lwawt.macosx;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;

import static javax.accessibility.AccessibleContext.ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY;
import static javax.accessibility.AccessibleContext.ACCESSIBLE_CARET_PROPERTY;
import static javax.accessibility.AccessibleContext.ACCESSIBLE_SELECTION_PROPERTY;
import static javax.accessibility.AccessibleContext.ACCESSIBLE_STATE_PROPERTY;
import static javax.accessibility.AccessibleContext.ACCESSIBLE_TABLE_MODEL_CHANGED;
import static javax.accessibility.AccessibleContext.ACCESSIBLE_TEXT_PROPERTY;
import static javax.accessibility.AccessibleContext.ACCESSIBLE_NAME_PROPERTY;
import static javax.accessibility.AccessibleContext.ACCESSIBLE_VALUE_PROPERTY;

import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import sun.awt.AWTAccessor;


final class CAccessible extends CFRetainedResource implements Accessible {

    public static CAccessible getCAccessible(final Accessible a) {
        if (a == null) return null;
        AccessibleContext context = a.getAccessibleContext();
        AWTAccessor.AccessibleContextAccessor accessor
                = AWTAccessor.getAccessibleContextAccessor();
        final CAccessible cachedCAX = (CAccessible) accessor.getNativeAXResource(context);
        if (cachedCAX != null) {
            return cachedCAX;
        }
        final CAccessible newCAX = new CAccessible(a);
        accessor.setNativeAXResource(context, newCAX);
        return newCAX;
    }

    private static native void unregisterFromCocoaAXSystem(long ptr);
    private static native void valueChanged(long ptr);
    private static native void selectedTextChanged(long ptr);
    private static native void selectionChanged(long ptr);
    private static native void titleChanged(long ptr);
    private static native void menuOpened(long ptr);
    private static native void menuClosed(long ptr);
    private static native void menuItemSelected(long ptr);
    private static native void treeNodeExpanded(long ptr);
    private static native void treeNodeCollapsed(long ptr);
    private static native void selectedCellsChanged(long ptr);
    private static native void tableContentCacheClear(long ptr);

    private Accessible accessible;

    private AccessibleContext activeDescendant;

    private CAccessible(final Accessible accessible) {
        super(0L, true); // real pointer will be poked in by native

        if (accessible == null) throw new NullPointerException();
        this.accessible = accessible;

        if (accessible instanceof Component) {
            addNotificationListeners((Component)accessible);
        }
    }

    @Override
    protected synchronized void dispose() {
        if (ptr != 0) unregisterFromCocoaAXSystem(ptr);
        super.dispose();
    }

    @Override
    public AccessibleContext getAccessibleContext() {
        return accessible.getAccessibleContext();
    }

    public void addNotificationListeners(Component c) {
        if (c instanceof Accessible) {
            AccessibleContext ac = ((Accessible)c).getAccessibleContext();
            ac.addPropertyChangeListener(new AXChangeNotifier());
        }
    }

    private final class AXChangeNotifier implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            String name = e.getPropertyName();
            if ( ptr != 0 ) {
                Object newValue = e.getNewValue();
                Object oldValue = e.getOldValue();
                if (name.equals(ACCESSIBLE_CARET_PROPERTY)) {
                    selectedTextChanged(ptr);
                } else if (name.equals(ACCESSIBLE_TEXT_PROPERTY)) {
                    AccessibleContext thisAC = accessible.getAccessibleContext();
                    Accessible parentAccessible = thisAC.getAccessibleParent();
                    if (!(parentAccessible instanceof JSpinner.NumberEditor)) {
                        valueChanged(ptr);
                    }
                } else if (name.equals(ACCESSIBLE_SELECTION_PROPERTY)) {
                    selectionChanged(ptr);
                } else if (name.equals(ACCESSIBLE_TABLE_MODEL_CHANGED)) {
                    valueChanged(ptr);
                    if (CAccessible.getSwingAccessible(CAccessible.this) != null) {
                        Accessible a = CAccessible.getSwingAccessible(CAccessible.this);
                        AccessibleContext ac = a.getAccessibleContext();
                        if ((ac != null) && (ac.getAccessibleRole() == AccessibleRole.TABLE)) {
                            tableContentCacheClear(ptr);
                        }
                    }
                } else if (name.equals(ACCESSIBLE_ACTIVE_DESCENDANT_PROPERTY)) {
                    if (newValue instanceof AccessibleContext) {
                        activeDescendant = (AccessibleContext)newValue;
                        if (newValue instanceof Accessible) {
                            Accessible a = (Accessible)newValue;
                            AccessibleContext ac = a.getAccessibleContext();
                            if (ac !=  null) {
                                Accessible p = ac.getAccessibleParent();
                                if (p != null) {
                                    AccessibleContext pac = p.getAccessibleContext();
                                    if ((pac != null) && (pac.getAccessibleRole() == AccessibleRole.TABLE)) {
                                        selectedCellsChanged(ptr);
                                    }
                                }
                            }
                        }
                    }
                } else if (name.equals(ACCESSIBLE_STATE_PROPERTY)) {
                    AccessibleContext thisAC = accessible.getAccessibleContext();
                    AccessibleRole thisRole = thisAC.getAccessibleRole();
                    Accessible parentAccessible = thisAC.getAccessibleParent();
                    AccessibleRole parentRole = null;
                    if (parentAccessible != null) {
                        parentRole = parentAccessible.getAccessibleContext().getAccessibleRole();
                    }

                    if (newValue == AccessibleState.EXPANDED) {
                        treeNodeExpanded(ptr);
                    } else if (newValue == AccessibleState.COLLAPSED) {
                        treeNodeCollapsed(ptr);
                    }

                    if (thisRole == AccessibleRole.POPUP_MENU) {
                        if ( newValue != null &&
                                ((AccessibleState)newValue) == AccessibleState.VISIBLE ) {
                            menuOpened(ptr);
                        } else if ( oldValue != null &&
                                ((AccessibleState)oldValue) == AccessibleState.VISIBLE ) {
                            menuClosed(ptr);
                        }
                    } else if (thisRole == AccessibleRole.MENU_ITEM ||
                            (thisRole == AccessibleRole.MENU)) {
                        if ( newValue != null &&
                                ((AccessibleState)newValue) == AccessibleState.FOCUSED ) {
                            menuItemSelected(ptr);
                        }
                    }

                    // Do send check box state changes to native side
                    if (thisRole == AccessibleRole.CHECK_BOX) {
                        if (!Objects.equals(newValue, oldValue)) {
                            valueChanged(ptr);
                        }

                        // Notify native side to handle check box style menuitem
                        if (parentRole == AccessibleRole.POPUP_MENU && newValue != null
                                && ((AccessibleState)newValue) == AccessibleState.FOCUSED) {
                            menuItemSelected(ptr);
                        }
                    }

                    // Do send radio button state changes to native side
                    if (thisRole == AccessibleRole.RADIO_BUTTON) {
                        if (newValue != null && !newValue.equals(oldValue)) {
                            valueChanged(ptr);
                        }

                        // Notify native side to handle radio button style menuitem
                        if (parentRole == AccessibleRole.POPUP_MENU && newValue != null
                            && ((AccessibleState)newValue) == AccessibleState.FOCUSED) {
                            menuItemSelected(ptr);
                        }
                    }

                    // Do send toggle button state changes to native side
                    if (thisRole == AccessibleRole.TOGGLE_BUTTON) {
                        if (!Objects.equals(newValue, oldValue)) {
                            valueChanged(ptr);
                        }
                    }
                } else if (name.equals(ACCESSIBLE_NAME_PROPERTY)) {
                    //for now trigger only for JTabbedPane.
                    if (e.getSource() instanceof JTabbedPane) {
                        titleChanged(ptr);
                    }
                } else if (name.equals(ACCESSIBLE_VALUE_PROPERTY)) {
                    AccessibleRole thisRole = accessible.getAccessibleContext()
                                                        .getAccessibleRole();
                    if (thisRole == AccessibleRole.SLIDER ||
                            thisRole == AccessibleRole.PROGRESS_BAR) {
                        valueChanged(ptr);
                    }
                }
            }
        }
    }

    static Accessible getSwingAccessible(final Accessible a) {
        return (a instanceof CAccessible) ? ((CAccessible)a).accessible : a;
    }

    static AccessibleContext getActiveDescendant(final Accessible a) {
        return (a instanceof CAccessible) ? ((CAccessible)a).activeDescendant : null;
    }

}
