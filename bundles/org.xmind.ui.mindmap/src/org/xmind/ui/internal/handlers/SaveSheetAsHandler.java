package org.xmind.ui.internal.handlers;

import java.io.File;
import java.util.Arrays;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.xmind.core.Core;
import org.xmind.core.ICloneData;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.editor.MME;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.MindMapUI;

public class SaveSheetAsHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor instanceof IGraphicalEditor) {
            IGraphicalEditorPage page = ((IGraphicalEditor) editor)
                    .getActivePageInstance();
            if (page != null) {
                saveSheetAs(page);
            }
        }
        return null;
    }

    private void saveSheetAs(final IGraphicalEditorPage page) {
        if (page == null)
            return;

        IMindMap mindmap = (IMindMap) page.getAdapter(IMindMap.class);
        if (mindmap == null)
            return;

        ISheet sheet = mindmap.getSheet();
        final IWorkbook newWorkbook = Core.getWorkbookBuilder()
                .createWorkbook();
        String tempLocation = createTempLocation();
        newWorkbook.setTempLocation(tempLocation);
        try {
            newWorkbook.saveTemp();
        } catch (Exception ignore) {
        }
        ICloneData clone = newWorkbook.clone(Arrays.asList(sheet));
        ISheet newSheet = (ISheet) clone.get(sheet);
        initSheet(newSheet);
        ITopic newCentralTopic = newWorkbook.findTopic(clone.getString(
                ICloneData.WORKBOOK_COMPONENTS, mindmap.getCentralTopic()
                        .getId()));
        if (newCentralTopic == null)
            //TODO should we log this?
            return;

        newSheet.replaceRootTopic(newCentralTopic);
        newWorkbook.addSheet(newSheet);
        newWorkbook.removeSheet(newWorkbook.getPrimarySheet());

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                final IEditorPart newEditor = page.getParentEditor().getSite()
                        .getPage()
                        .openEditor(MME.createLoadedEditorInput(newWorkbook),
                        //new WorkbookEditorInput(newWorkbook, null, true),
                                MindMapUI.MINDMAP_EDITOR_ID, true);
                // Forcely make editor saveable:
                if (newWorkbook instanceof ICoreEventSource2) {
                    ((ICoreEventSource2) newWorkbook)
                            .registerOnceCoreEventListener(
                                    Core.WorkbookPreSaveOnce,
                                    ICoreEventListener.NULL);
                }
                if (newEditor != null && newEditor instanceof ISaveablePart) {
                    Display.getCurrent().timerExec(500, new Runnable() {
                        public void run() {
                            ((ISaveablePart) newEditor).doSaveAs();
                        }
                    });
                }
            }
        });

    }

    public String createTempLocation() {
        String tempFile = Core.getIdFactory().createId()
                + MindMapUI.FILE_EXT_XMIND_TEMP;
        String hiberLoc = new File(Core.getWorkspace().getTempDir("workbooks"), //$NON-NLS-1$
                tempFile).getAbsolutePath();
        return hiberLoc;
    }

    private void initSheet(ISheet sheet) {
        initTopic(sheet.getRootTopic());
    }

    private void initTopic(ITopic topic) {
        for (ITopic child : topic.getAllChildren()) {
            initTopic(child);
        }
    }

}
