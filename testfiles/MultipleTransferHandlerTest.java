//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.frapuccino.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author redsolo
 */
public class MultipleTransferHandlerTest{

    private MultipleTransferHandler handler;
    private DataFlavorTransferHandler filelistHandler;
    private DataFlavorTransferHandler stringHandler;
    private DataFlavorTransferHandler imageHandler;

    /** {@inheritDoc} */
    @Before
    public void setUp() throws Exception {
        handler = new MultipleTransferHandler();

        filelistHandler = new DataFlavorTransferHandler(DataFlavor.javaFileListFlavor, TransferHandler.NONE);
        stringHandler = new DataFlavorTransferHandler(DataFlavor.stringFlavor, TransferHandler.COPY_OR_MOVE);
        imageHandler = new DataFlavorTransferHandler(DataFlavor.imageFlavor, TransferHandler.MOVE);
        handler.addTransferHandler(filelistHandler);
        handler.addTransferHandler(stringHandler);
        handler.addTransferHandler(imageHandler);
    }

    /**
     * Tests the add/remove methods.
     */
    @Test
    public void testListTransferHandlers() {
        handler = new MultipleTransferHandler();

        handler.addTransferHandler(imageHandler);
        handler.addTransferHandler(new DataFlavorTransferHandler(DataFlavor.javaFileListFlavor));
        Assert.assertEquals("The transfer handlers list is not the correct size.", 2, handler.getTransferHandlers().size());
        handler.addTransferHandler(imageHandler);
        Assert.assertEquals("The transfer handlers list is not the correct size.", 2, handler.getTransferHandlers().size());
        handler.removeTransferHandler(imageHandler);
        Assert.assertEquals("The transfer handlers list is not the correct size.", 1, handler.getTransferHandlers().size());
        handler.removeTransferHandler(new DataFlavorTransferHandler(null));
        Assert.assertEquals("The transfer handlers list is not the correct size.", 1, handler.getTransferHandlers().size());
    }

    /**
     * Test that the multiple transfer handler returns correct on the canImport() method.
     */
    @Test
    public void testCanImport() {
        Assert.assertTrue("The handler should return true on importing file lists.",
                handler.canImport(null, new DataFlavor[] {DataFlavor.javaFileListFlavor}));
        Assert.assertTrue("The handler should return true on importing images.",
                handler.canImport(null, new DataFlavor[] {DataFlavor.imageFlavor}));
        Assert.assertFalse("The handler should return false on importing unicode text.",
                handler.canImport(null, new DataFlavor[] {DataFlavor.getTextPlainUnicodeFlavor()}));
    }

    /**
     * Tests the getSourceAction().
     */
    @Test
    public void testImportData() {
        MockTransferable transferable = new MockTransferable();
        handler.importData(null, transferable);
        Assert.assertSame("The transferable did not return the data from the right data flavor",
                DataFlavor.imageFlavor,
                transferable.returnedTransferDataFlavor);
        Assert.assertEquals("The importData was not called in the image handler", 1, imageHandler.importDataCallCount);
        Assert.assertEquals("The importData was called in the file list handler", 0, filelistHandler.importDataCallCount);
        Assert.assertEquals("The importData was called in the string handler", 0, stringHandler.importDataCallCount);
    }

    /**
     * Tests the getSourceAction().
     * This returns only the default handlers actions.
     */
    @Test
    public void testSourceActions() {
        Assert.assertEquals("Expected no actions for the file list handler.", TransferHandler.NONE, handler.getSourceActions(null));
        handler.setDragSourceTransferHandler(imageHandler);
        Assert.assertEquals("Expected no actions for the file list handler.", TransferHandler.MOVE, handler.getSourceActions(null));
    }

    /**
     * test that the drag source is correct.
     */
    @Test
    public void testDragSourceHandler() {
        Assert.assertSame("Expected the file list as the source handler.", filelistHandler, handler.getDragSourceTransferHandler());
        handler.setDragSourceTransferHandler(imageHandler);
        Assert.assertSame("Expected the image as the source handler.", imageHandler, handler.getDragSourceTransferHandler());
    }

    /**
     * Fake transferhandler.
     */
    class DataFlavorTransferHandler extends TransferHandler {
        private DataFlavor supportedFlavor;
        private int importDataCallCount = 0;
        private int exportDataCallCount = 0;
        private int actions;

        /**
         * Constructs a transfer handler that supports the specified flavor.
         * @param flavor the flavor that this transfer handler supports.
         */
        DataFlavorTransferHandler(DataFlavor flavor) {
            supportedFlavor = flavor;
        }

        /**
         * Constructs a transfer handler that supports the specified flavor.
         * @param flavor the flavor that this transfer handler supports.
         * @param supportedActions the source actions that this transfer handler supports.
         */
        DataFlavorTransferHandler(DataFlavor flavor, int supportedActions) {
            supportedFlavor = flavor;
            actions = supportedActions;
        }

        /** {@inheritDoc} */
        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            boolean canImport = false;
            for (int i = 0; i < transferFlavors.length; i++) {
                canImport = canImport || transferFlavors[i].equals(supportedFlavor);
            }
            return canImport;
        }
        /** {@inheritDoc} */
        @Override
        protected Transferable createTransferable(JComponent c) {
            return super.createTransferable(c);
        }
        /** {@inheritDoc} */
        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            exportDataCallCount++;
        }
        /** {@inheritDoc} */
        @Override
        public int getSourceActions(JComponent c) {
            return actions;
        }
        /** {@inheritDoc} */
        @Override
        public boolean importData(JComponent comp, Transferable t) {
            boolean wasImported = false;
            importDataCallCount++;
            if (t.isDataFlavorSupported(supportedFlavor)) {
                try {
                    t.getTransferData(supportedFlavor);
                } catch (Exception e) {
                    Assert.fail("Exception: " + e);
                }
                wasImported = true;
            }
            return wasImported;
        }
    }

    /**
     * Fake transferable.
     */
    class MockTransferable implements Transferable {
        private DataFlavor returnedTransferDataFlavor;

        /** {@inheritDoc} */
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[] {DataFlavor.imageFlavor, DataFlavor.getTextPlainUnicodeFlavor()};
        }

        /** {@inheritDoc} */
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return true;
        }

        /** {@inheritDoc} */
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            returnedTransferDataFlavor = flavor;
            return null;
        }
    }
}
