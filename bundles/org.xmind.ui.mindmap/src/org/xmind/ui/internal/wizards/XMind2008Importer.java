/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.wizards;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_ALIGN;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_HEIGHT;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_HREF;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_LINE_TAPERED;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_MULTI_LINE_COLORS;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_NUMBER_FORMAT;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_SRC;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STRUCTURE_CLASS;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_FAMILY;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_STYLE_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_THEME;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_TYPE;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_WIDTH;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_X;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_Y;
import static org.xmind.core.internal.dom.DOMConstants.TAG_BOUNDARIES;
import static org.xmind.core.internal.dom.DOMConstants.TAG_BOUNDARY;
import static org.xmind.core.internal.dom.DOMConstants.TAG_IMG;
import static org.xmind.core.internal.dom.DOMConstants.TAG_LABEL;
import static org.xmind.core.internal.dom.DOMConstants.TAG_LABELS;
import static org.xmind.core.internal.dom.DOMConstants.TAG_MARKER;
import static org.xmind.core.internal.dom.DOMConstants.TAG_NOTES;
import static org.xmind.core.internal.dom.DOMConstants.TAG_NUMBERING;
import static org.xmind.core.internal.dom.DOMConstants.TAG_POSITION;
import static org.xmind.core.internal.dom.DOMConstants.TAG_PREFIX;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RELATIONSHIP;
import static org.xmind.core.internal.dom.DOMConstants.TAG_RELATIONSHIPS;
import static org.xmind.core.internal.dom.DOMConstants.TAG_SUFFIX;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TITLE;
import static org.xmind.core.internal.dom.DOMConstants.TAG_TOPIC;
import static org.xmind.core.internal.dom.DOMConstants.VAL_NONE;
import static org.xmind.core.internal.dom.DOMConstants.VAL_TAPERED;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xmind.core.Core;
import org.xmind.core.IAdaptable;
import org.xmind.core.IBoundary;
import org.xmind.core.IFileEntry;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.IImage;
import org.xmind.core.IImageSpan;
import org.xmind.core.IManifest;
import org.xmind.core.INotes;
import org.xmind.core.INumbering;
import org.xmind.core.IParagraph;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITextSpan;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.core.internal.dom.MarkerGroupImpl;
import org.xmind.core.internal.dom.MarkerImpl;
import org.xmind.core.internal.dom.MarkerSheetImpl;
import org.xmind.core.internal.dom.NumberUtils;
import org.xmind.core.internal.dom.StyleSheetImpl;
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.DOMUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.wizards.MindMapImporter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XMind2008Importer extends MindMapImporter implements ErrorHandler {

    private static final String TAG_MAP = "map"; //$NON-NLS-1$
    private static final String TAG_ROOT_TOPIC = "root-topic"; //$NON-NLS-1$
    private static final String TAG_MARKERS = "markers"; //$NON-NLS-1$
    private static final String TAG_ATTACHED_TOPICS = "attached-topics"; //$NON-NLS-1$
    private static final String TAG_DETACHED_TOPICS = "detached-topics"; //$NON-NLS-1$
    private static final String TAG_PLAIN = "plain"; //$NON-NLS-1$
    private static final String TAG_RICH = "rich"; //$NON-NLS-1$

    private static final String ATTR_FOLDED = "folded"; //$NON-NLS-1$
    private static final String ATTR_FLOATING_TYPE = "floating-type"; //$NON-NLS-1$
    private static final String ATTR_INHERITED = "inherited"; //$NON-NLS-1$
    private static final String ATTR_FROM = "from-id"; //$NON-NLS-1$
    private static final String ATTR_TO = "to-id"; //$NON-NLS-1$
    private static final String ATTR_END_INDEX = "end-index"; //$NON-NLS-1$
    private static final String ATTR_START_INDEX = "start-index"; //$NON-NLS-1$

    private static final String ATTR_RAINBOWCOLOR = "rainbowcolor"; //$NON-NLS-1$
    private static final String ATTR_SPINYLINES = "spinylines"; //$NON-NLS-1$

    private static final String ATTR_FROM_POINT = "control-point1"; //$NON-NLS-1$
    private static final String ATTR_TO_POINT = "control-point2"; //$NON-NLS-1$

    private static final String VAL_CENTRAL = "central"; //$NON-NLS-1$
    private static final String VAL_MULTI_LINE_COLORS = "#ac6060 #acac60 #60ac60 #60acac #6060ac #ac60ac"; //$NON-NLS-1$

    private static final String VAL_USER = "User"; //$NON-NLS-1$
    private static final String VAL_ATTACHMENT = "Attachment"; //$NON-NLS-1$

    private static final String PROP_BACKGROUND = "background"; //$NON-NLS-1$

    private IWorkbook sourceWorkbook;

    private IWorkbook targetWorkbook;

    private IStorage storage;

    private MarkerSheetImpl markerSheet;

    private String defaultMarkerGroupId;

    public XMind2008Importer(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    public void build() throws InvocationTargetException, InterruptedException {
        try {
            storage = createStorage();
            sourceWorkbook = Core.getWorkbookBuilder().loadFromPath(
                    getSourcePath(), storage, null);
            targetWorkbook = getTargetWorkbook();
            readStyles();
            Document contentDoc = parseStyles("content.xml"); //$NON-NLS-1$
            loadWorkbook((WorkbookImpl) targetWorkbook, contentDoc);
        } catch (Exception e) {
            throw new InvocationTargetException(e);
        }
    }

    private void readStyles() {
        Document styleDoc = parseStyles("styles.xml"); //$NON-NLS-1$
        if (styleDoc == null)
            return;
        upgradeStyleSheet(styleDoc);
        StyleSheetImpl ss = new StyleSheetImpl(styleDoc);
        ((WorkbookImpl) targetWorkbook).setStyleSheet(ss);
        ss.getAllStyles();
    }

    private void upgradeStyleSheet(Document styleDoc) {
        Element element = styleDoc.getDocumentElement();
        if (element != null)
            upgradeStyles(element);
    }

    private void upgradeStyles(Element element) {
        upgradeStyle(element);
        Iterator<Element> it = DOMUtils.childElementIter(element);
        while (it.hasNext()) {
            Element subElement = it.next();
            upgradeStyles(subElement);
        }
    }

    private void upgradeStyle(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        Map<String, String> added = null;
        for (int i = 0; i < attributes.getLength(); i++) {
            Attr attr = (Attr) attributes.item(i);
            String name = attr.getName();
            String value = attr.getValue();
            if (ATTR_TYPE.equals(name)) {
                attr.setValue(value.toLowerCase());
            } else if (ATTR_STYLE_FAMILY.equals(name)) {
                attr.setValue(value.toLowerCase());
            } else if (ATTR_RAINBOWCOLOR.equals(name)) {
                if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
                    added = add(added, ATTR_MULTI_LINE_COLORS,
                            VAL_MULTI_LINE_COLORS);
                }
            } else if (ATTR_SPINYLINES.equals(name)) {
                if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
                    added = add(added, ATTR_LINE_TAPERED, VAL_TAPERED);
                }
            }
            if ("floatingMainTopic".equals(value)) { //$NON-NLS-1$
                attr.setValue("floatingTopic"); //$NON-NLS-1$
            } else if ("$none$".equals(value)) { //$NON-NLS-1$
                attr.setValue(VAL_NONE);
            }
        }
        if (added != null) {
            for (Entry<String, String> e : added.entrySet()) {
                element.setAttribute(e.getKey(), e.getValue());
            }
        }
    }

    private Map<String, String> add(Map<String, String> map, String key,
            String value) {
        if (map == null)
            map = new HashMap<String, String>();
        map.put(key, value);
        return map;
    }

    private void loadWorkbook(WorkbookImpl workbook, Document wbDocument) {
        Element wbEle = wbDocument.getDocumentElement();
        Iterator<Element> sheetIter = DOMUtils.childElementIterByTag(wbEle,
                TAG_MAP);
        boolean primary = true;
        while (sheetIter.hasNext()) {
            Element sheetEle = sheetIter.next();
            ISheet sheet;
            if (primary) {
                sheet = workbook.getPrimarySheet();
                primary = false;
            } else {
                sheet = workbook.createSheet();
                workbook.addSheet(sheet);
            }
            loadSheet(sheet, sheetEle, workbook);
        }
    }

    private void loadTitle(ITitled titled, Element oldElement) {
        String title = DOMUtils.getTextContentByTag(oldElement, TAG_TITLE);
        titled.setTitleText(title);
    }

    private void loadId(IAdaptable elementAdaptable, Element oldElement,
            WorkbookImpl workbook) {
        Element newElement = getElement(elementAdaptable);
        if (newElement != null) {
            String id = DOMUtils.getAttribute(oldElement, ATTR_ID);
            if (id != null) {
//                workbook.getElementRegistry().unregisterByKey(id);
                Document doc = newElement.getOwnerDocument();
                workbook.getAdaptableRegistry()
                        .unregisterById(elementAdaptable,
                                newElement.getAttribute(ATTR_ID), doc);
                DOMUtils.replaceId(newElement, id);
                workbook.getAdaptableRegistry().registerById(elementAdaptable,
                        id, doc);
//                workbook.getElementRegistry().registerByKey(id,
//                        elementAdaptable);
            }
        }
    }

    private Element getElement(IAdaptable elementAdaptable) {
        return (Element) elementAdaptable.getAdapter(Element.class);
    }

    private void loadStyle(IStyled styled, Element oldElement) {
        String styleId = DOMUtils.getAttribute(oldElement, ATTR_STYLE_ID);
        styled.setStyleId(styleId);
    }

    private void loadSheet(ISheet sheet, Element sheetEle, WorkbookImpl workbook) {
        loadId(sheet, sheetEle, workbook);
        loadTitle(sheet, sheetEle);
        loadSheetStyle(sheet, sheetEle);
        String themeId = DOMUtils.getAttribute(sheetEle, ATTR_THEME);
        sheet.setThemeId(themeId);

        Element topicEle = DOMUtils.getFirstChildElementByTag(sheetEle,
                TAG_ROOT_TOPIC);
        if (topicEle != null) {
            loadTopic(sheet.getRootTopic(), topicEle, workbook);
        }

        Element relsEle = DOMUtils.getFirstChildElementByTag(sheetEle,
                TAG_RELATIONSHIPS);
        if (relsEle != null) {
            Iterator<Element> relIter = DOMUtils.childElementIterByTag(relsEle,
                    TAG_RELATIONSHIP);
            while (relIter.hasNext()) {
                Element relEle = relIter.next();
                IRelationship rel = workbook.createRelationship();
                sheet.addRelationship(rel);
                loadRelationship(rel, relEle, workbook);
            }
        }
    }

    private void loadSheetStyle(ISheet sheet, Element sheetEle) {
        String styleId = DOMUtils.getAttribute(sheetEle, ATTR_STYLE_ID);
        IStyleSheet styleSheet = targetWorkbook.getStyleSheet();
        IStyle style = styleSheet.findStyle(styleId);
        if (style == null)
            return;
        String type = style.getType();
        if ("map".equals(type)) { //$NON-NLS-1$
            String p = style.getProperty(PROP_BACKGROUND);
            String url = findAttachmentUrl(p);
            style.setProperty(PROP_BACKGROUND, url);
        }
        sheet.setStyleId(styleId);
    }

    private void loadTopic(ITopic topic, Element topicEle, WorkbookImpl workbook) {
        loadId(topic, topicEle, workbook);
        loadTitle(topic, topicEle);
        loadStyle(topic, topicEle);
        loadFolded(topic, topicEle);
        loadPosition(topic, topicEle);
        loadHyperlink(topic, topicEle);
        loadStructureClass(topic, topicEle);
        loadLabels(topic, topicEle);
        loadImage(topic, topicEle, workbook);
        loadMarkers(topic, topicEle, workbook);
        loadNotes(topic, topicEle, workbook);
        loadNumbering(topic, topicEle);
        loadSubTopics(topic, topicEle, TAG_ATTACHED_TOPICS, ITopic.ATTACHED,
                workbook);
        loadSubTopics(topic, topicEle, TAG_DETACHED_TOPICS, ITopic.DETACHED,
                workbook);
        loadBoundaries(topic, topicEle, workbook);
    }

    private void loadHyperlink(ITopic topic, Element topicEle) {
        String url = DOMUtils.getAttribute(topicEle, ATTR_HREF);
        if (url != null) {
            url = readAttachment(url);
        }
        topic.setHyperlink(url);
    }

    private String readAttachment(String url) {
        String path = HyperlinkUtils.toAttachmentPath(url);
        if (path.startsWith("#")) //$NON-NLS-1$
            path = path.substring(1);
        IFileEntry entry = findAttachmentEntry(path);
        if (entry != null) {
            if (entry != null) {
                InputStream is = entry.getInputStream();
                String entryName = entry.getPath();
                try {
                    IFileEntry fileEntry = targetWorkbook.getManifest()
                            .createAttachmentFromStream(is, entryName);
                    return HyperlinkUtils.toAttachmentURL(fileEntry.getPath());
                } catch (IOException e) {
                    log(e, "failed to create attachment from: " + path); //$NON-NLS-1$
                }
            }
        }
        return path;
    }

    private void loadFolded(ITopic topic, Element topicEle) {
        String folded = DOMUtils.getAttribute(topicEle, ATTR_FOLDED);
        topic.setFolded(Boolean.TRUE.toString().equalsIgnoreCase(folded));
    }

    private void loadPosition(ITopic topic, Element topicEle) {
        Element positionEle = DOMUtils.getFirstChildElementByTag(topicEle,
                TAG_POSITION);
        if (positionEle != null) {
            String x = DOMUtils.getAttribute(positionEle, ATTR_X);
            String y = DOMUtils.getAttribute(positionEle, ATTR_Y);
            if (x != null && y != null) {
                try {
                    int xValue = Integer.parseInt(x);
                    int yValue = Integer.parseInt(y);
                    topic.setPosition(xValue, yValue);
                } catch (NumberFormatException e) {
                }
            }
            if (x != null || y != null) {
                Element newPositionEle = DOMUtils.ensureChildElement(
                        getElement(topic), TAG_POSITION);
                DOMUtils.setAttribute(newPositionEle, ATTR_X, x);
                DOMUtils.setAttribute(newPositionEle, ATTR_Y, y);
            }
        }
    }

    private void loadBoundaries(ITopic topic, Element topicEle,
            WorkbookImpl workbook) {
        Element boundariesEle = DOMUtils.getFirstChildElementByTag(topicEle,
                TAG_BOUNDARIES);
        if (boundariesEle == null)
            return;
        Iterator<Element> boundaryIter = DOMUtils.childElementIterByTag(
                boundariesEle, TAG_BOUNDARY);
        while (boundaryIter.hasNext()) {
            Element boundaryEle = boundaryIter.next();
            IBoundary boundary = workbook.createBoundary();
            topic.addBoundary(boundary);
            loadBoundary(boundary, boundaryEle, workbook);
        }
    }

    private void loadBoundary(IBoundary boundary, Element boundaryEle,
            WorkbookImpl workbook) {
        String startIndex = DOMUtils
                .getAttribute(boundaryEle, ATTR_START_INDEX);
        String endIndex = DOMUtils.getAttribute(boundaryEle, ATTR_END_INDEX);
        Element newBoundaryEle = getElement(boundary);
        DOMUtils.setAttribute(newBoundaryEle, ATTR_START_INDEX, startIndex);
        DOMUtils.setAttribute(newBoundaryEle, ATTR_END_INDEX, endIndex);
        loadStyle(boundary, boundaryEle);
    }

    private void loadSubTopics(ITopic topic, Element topicEle,
            String topicsTag, String topicType, WorkbookImpl workbook) {
        Element subTopicsEle = DOMUtils.getFirstChildElementByTag(topicEle,
                topicsTag);
        if (subTopicsEle == null)
            return;
        Iterator<Element> subTopicIter = DOMUtils.childElementIterByTag(
                subTopicsEle, TAG_TOPIC);
        while (subTopicIter.hasNext()) {
            Element subTopicEle = subTopicIter.next();
            ITopic subTopic = workbook.createTopic();
            topic.add(subTopic, topicType);
            loadTopic(subTopic, subTopicEle, workbook);
        }
    }

    private void loadNumbering(ITopic topic, Element topicEle) {
        Element numberingEle = DOMUtils.getFirstChildElementByTag(topicEle,
                TAG_NUMBERING);
        if (numberingEle == null)
            return;

        INumbering numbering = topic.getNumbering();
        String format = DOMUtils.getAttribute(numberingEle, ATTR_NUMBER_FORMAT);
        String prefix = DOMUtils.getAttribute(numberingEle, TAG_PREFIX);
        String suffix = DOMUtils.getAttribute(numberingEle, TAG_SUFFIX);
        String inherited = DOMUtils.getAttribute(numberingEle, ATTR_INHERITED);
        numbering.setFormat(format);
        numbering.setPrefix(prefix);
        numbering.setSuffix(suffix);

        boolean prependParentNumbering = inherited == null
                || Boolean.parseBoolean(inherited);
        numbering.setPrependsParentNumbers(prependParentNumbering);
    }

    private void loadNotes(ITopic topic, Element topicEle, WorkbookImpl workbook) {
        Element notesEle = DOMUtils.getFirstChildElementByTag(topicEle,
                TAG_NOTES);
        if (notesEle == null)
            return;

        INotes notes = topic.getNotes();
        Element plainEle = DOMUtils.getFirstChildElementByTag(notesEle,
                TAG_PLAIN);
        if (plainEle != null) {
            IPlainNotesContent content = (IPlainNotesContent) workbook
                    .createNotesContent(INotes.PLAIN);
            content.setTextContent(plainEle.getTextContent());
            notes.setContent(INotes.PLAIN, content);
        }

        Element richEle = DOMUtils
                .getFirstChildElementByTag(notesEle, TAG_RICH);
        if (richEle != null) {
            IHtmlNotesContent content = (IHtmlNotesContent) workbook
                    .createNotesContent(INotes.HTML);
            loadRichNotes(topic, topicEle, richEle, content, workbook);
            notes.setContent(INotes.HTML, content);
        }
    }

    private void loadRichNotes(ITopic topic, Element topicEle, Element richEle,
            IHtmlNotesContent content, WorkbookImpl workbook) {
        Iterator<Element> it = DOMUtils.childElementIterByTag(richEle,
                DOMConstants.TAG_P);
        while (it.hasNext()) {
            Element pEle = it.next();
            IParagraph p = content.createParagraph();
            loadParagraph(topic, topicEle, pEle, p, content, workbook);
            content.addParagraph(p);
        }
    }

    private void loadParagraph(ITopic topic, Element topicEle, Element pEle,
            IParagraph p, IHtmlNotesContent content, WorkbookImpl workbook) {
        NodeList ns = pEle.getChildNodes();
        for (int i = 0; i < ns.getLength(); i++) {
            Node n = ns.item(i);
            if (n instanceof Text) {
                ITextSpan span = content.createTextSpan(n.getTextContent());
                p.addSpan(span);
            } else if (n instanceof Element) {
                Element e = (Element) n;
                String tag = e.getTagName();
                if (TAG_IMG.equals(tag)) {
                    String url = findImageUrl(e, workbook);
                    if (url != null) {
                        IImageSpan span = content.createImageSpan(url);
                        p.addSpan(span);
                    }
                } else {
                    ITextSpan span = content.createTextSpan(e.getTextContent());
                    span.setStyleId(DOMUtils.getAttribute(e, ATTR_STYLE_ID));
                    p.addSpan(span);
                }
            }
        }
    }

    private void loadMarkers(ITopic topic, Element topicEle,
            WorkbookImpl workbook) {
        Element markersEle = DOMUtils.getFirstChildElementByTag(topicEle,
                TAG_MARKERS);
        if (markersEle == null)
            return;
        Iterator<Element> markerIter = DOMUtils.childElementIterByTag(
                markersEle, TAG_MARKER);
        while (markerIter.hasNext()) {
            Element markerEle = markerIter.next();
            loadMarker(topic, markerEle, workbook);
        }
    }

    private void loadMarker(ITopic topic, Element markerEle,
            WorkbookImpl workbook) {
        String id = DOMUtils.getAttribute(markerEle, ATTR_ID);
        if (id == null)
            return;

        String type = DOMUtils.getAttribute(markerEle, ATTR_TYPE);
        int sepIndex = id.indexOf('/');
        String markerId;
        String markerGroupId;
        if (sepIndex >= 0) {
            markerId = id.substring(sepIndex + 1);
            if (VAL_USER.equals(type)) {
                if (id.startsWith("#")) { //$NON-NLS-1$
                    markerGroupId = id.substring(1, sepIndex);
                } else {
                    markerGroupId = id.substring(0, sepIndex);
                }
            } else {
                markerGroupId = null;
            }
        } else {
            markerId = id;
            if (VAL_USER.equals(type) || VAL_ATTACHMENT.equals(type)) {
                markerGroupId = getDefaultMarkerGroupId();
            } else {
                markerGroupId = null;
            }
        }

        if (markerGroupId != null) {
            if (markerSheet == null) {
                markerSheet = (MarkerSheetImpl) workbook.getMarkerSheet();
            }
            IMarkerGroup group = markerSheet.findMarkerGroup(markerGroupId);
            if (group == null) {
                group = markerSheet.createMarkerGroup(false);
                markerSheet.getElementRegistry().unregister(group);
                DOMUtils.replaceId(
                        ((MarkerGroupImpl) group).getImplementation(),
                        markerGroupId);
                markerSheet.getElementRegistry().register(group);
                markerSheet.addMarkerGroup(group);
            }
            IMarker marker = group.getMarker(markerId);
            if (marker == null) {
                IFileEntry markerEntry = findAttachmentEntry(markerId,
                        workbook.getManifest());
                if (markerEntry != null) {
                    String path = markerEntry.getPath();
                    if (!path.startsWith("/")) //$NON-NLS-1$
                        path = "/" + path; //$NON-NLS-1$
                    marker = markerSheet.createMarker(path);
                    markerSheet.getElementRegistry().unregister(marker);
                    DOMUtils.replaceId(
                            ((MarkerImpl) marker).getImplementation(), markerId);
                    markerSheet.getElementRegistry().register(marker);
                }
            }
        }

        topic.addMarker(markerId);
    }

    private IFileEntry findAttachmentEntry(String attId, IManifest manifest) {
        List<IFileEntry> fileEntries = manifest.getFileEntries();
        for (IFileEntry entry : fileEntries) {
            if (entry.getPath().contains(attId))
                return entry;
        }
        return null;
    }

    private String getDefaultMarkerGroupId() {
        if (defaultMarkerGroupId == null) {
            defaultMarkerGroupId = Core.getIdFactory().createId();
        }
        return defaultMarkerGroupId;
    }

    private void loadImage(ITopic topic, Element topicEle, WorkbookImpl workbook) {
        Element imgEle = DOMUtils.getFirstChildElementByTag(topicEle, TAG_IMG);
        if (imgEle == null)
            return;

        IImage image = topic.getImage();
        String url = findImageUrl(imgEle, workbook);
        image.setSource(url);

        String width = DOMUtils.getAttribute(imgEle, ATTR_WIDTH);
        if (width != null) {
            int w = NumberUtils.safeParseInt(width, -1);
            if (w >= 0) {
                image.setWidth(w);
            }
        }
        String height = DOMUtils.getAttribute(imgEle, ATTR_HEIGHT);
        if (height != null) {
            int h = NumberUtils.safeParseInt(height, -1);
            if (h >= 0) {
                image.setHeight(h);
            }
        }

        String alignment = DOMUtils.getAttribute(imgEle, ATTR_ALIGN);
        image.setAlignment(alignment);
    }

    private String findImageUrl(Element imgEle, WorkbookImpl workbook) {
        String url = DOMUtils.getAttribute(imgEle, ATTR_SRC);
        return findAttachmentUrl(url);
    }

    private void loadLabels(ITopic topic, Element topicEle) {
        Iterator<Element> labelsIter = DOMUtils.childElementIterByTag(topicEle,
                TAG_LABELS);
        while (labelsIter.hasNext()) {
            Element labelsEle = labelsIter.next();
            Iterator<Element> labelIter = DOMUtils.childElementIterByTag(
                    labelsEle, TAG_LABEL);
            while (labelIter.hasNext()) {
                Element labelEle = labelIter.next();
                String label = labelEle.getTextContent();
                topic.addLabel(label);
            }
        }
    }

    private void loadStructureClass(ITopic topic, Element topicEle) {
        String floatingType = DOMUtils.getAttribute(topicEle,
                ATTR_FLOATING_TYPE);
        String structureClass;
        if (VAL_CENTRAL.equals(floatingType)) {
            structureClass = "org.xmind.branchPolicy.map.floating"; //$NON-NLS-1$
        } else {
            structureClass = upgradeStructureClass(DOMUtils.getAttribute(
                    topicEle, ATTR_STRUCTURE_CLASS));
        }
        topic.setStructureClass(structureClass);
    }

    private String upgradeStructureClass(String structureClass) {
        if (structureClass == null)
            return null;
        if ("org.xmind.branchPolicy.org-chart.left".equals(structureClass)) { //$NON-NLS-1$
            structureClass = "org.xmind.ui.logic.left"; //$NON-NLS-1$
        } else if ("org.xmind.branchPolicy.org-chart.right".equals(structureClass)) { //$NON-NLS-1$
            structureClass = "org.xmind.ui.logic.right"; //$NON-NLS-1$
        } else if ("org.xmind.branchPolicy.chart2d".equals(structureClass)) { //$NON-NLS-1$
            structureClass = "org.xmind.ui.spreadsheet"; //$NON-NLS-1$
        } else if (structureClass.startsWith("org.xmind.branchPolicy.")) { //$NON-NLS-1$
            structureClass = "org.xmind.ui." + structureClass.substring(23); //$NON-NLS-1$
        }
        return structureClass;
    }

    private void loadRelationship(IRelationship rel, Element relEle,
            WorkbookImpl workbook) {
        loadId(rel, relEle, workbook);
        loadStyle(rel, relEle);
        loadTitle(rel, relEle);

        rel.setEnd1Id(DOMUtils.getAttribute(relEle, ATTR_FROM));
        rel.setEnd2Id(DOMUtils.getAttribute(relEle, ATTR_TO));

        loadControlPoint(rel, 0, relEle, ATTR_FROM_POINT);
        loadControlPoint(rel, 1, relEle, ATTR_TO_POINT);
    }

    private void loadControlPoint(IRelationship rel, int index, Element relEle,
            String pointAttr) {
        String p = DOMUtils.getAttribute(relEle, pointAttr);
        if (p != null) {
            String[] xy = p.split(", "); //$NON-NLS-1$
            if (xy.length == 2) {
                int x;
                int y;
                try {
                    x = Integer.parseInt(xy[0]);
                    y = Integer.parseInt(xy[1]);
                } catch (Throwable e) {
                    return;
                }
                rel.getControlPoint(index).setPosition(x, y);
            }
        }
    }

    private String findAttachmentUrl(String url) {
        if (HyperlinkUtils.isAttachmentURL(url)) {
            String attId = HyperlinkUtils.toAttachmentPath(url);
            if (attId.startsWith("#")) { //$NON-NLS-1$
                attId = attId.substring(1, attId.length());
            }
            IFileEntry entry = findAttachmentEntry(attId);
            if (entry != null) {
                InputStream is = entry.getInputStream();
                String path = entry.getPath();
                try {
                    IFileEntry fileEntry = targetWorkbook.getManifest()
                            .createAttachmentFromStream(is, path);
                    return HyperlinkUtils.toAttachmentURL(fileEntry.getPath());
                } catch (IOException e) {
                    log(e, "failed to create attachment from: " + path); //$NON-NLS-1$
                }
            }
        }
        return url;
    }

    private IFileEntry findAttachmentEntry(String attId) {
        IManifest manifest = sourceWorkbook.getManifest();
        List<IFileEntry> fileEntries = manifest.getFileEntries();
        for (IFileEntry entry : fileEntries) {
            if (entry.getPath().contains(attId))
                return entry;
        }
        return null;
    }

    private Document parseStyles(String content) {
        DocumentBuilder builder = getDocumentBuilder();
        builder.setErrorHandler(this);
        List<IFileEntry> entries = sourceWorkbook.getManifest()
                .getFileEntries();
        for (IFileEntry entry : entries) {
            if (content.equals(entry.getPath())) {
                InputStream is = entry.getInputStream();
                try {
                    return builder.parse(is);
                } catch (SAXException e) {
                } catch (IOException e) {
                }
                break;
            }
        }
        return null;
    }

    private IStorage createStorage() {
        String tempFile = Core.getIdFactory().createId()
                + MindMapUI.FILE_EXT_XMIND_TEMP;
        String tempLocation = Core.getWorkspace().getTempDir(
                "workbooks" + "/" + tempFile); //$NON-NLS-1$ //$NON-NLS-2$
        File tempDir = new File(tempLocation);
        return new DirectoryStorage(tempDir);
    }

    private DocumentBuilder getDocumentBuilder() {
        DocumentBuilder builder = null;
        try {
            builder = DOMUtils.getDefaultDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return builder;
    }

    public void error(SAXParseException exception) throws SAXException {
        log(exception, null);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        log(exception, null);
    }

    public void warning(SAXParseException exception) throws SAXException {
        log(exception, null);
    }
}
