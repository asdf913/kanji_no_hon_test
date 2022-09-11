package org.apache.poi.ss.usermodel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.zip.ZipFile;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;

public class WorkbookUtil {

	private WorkbookUtil() {
	}

	public static Workbook getWorkbook(final File file)
			throws IOException, GeneralSecurityException, InvalidFormatException {
		//
		final ContentInfo ci = testAndApply(Objects::nonNull, file, new ContentInfoUtil()::findMatch, null);
		//
		final String message = ci != null ? ci.getMessage() : null;
		//
		final String mimeType = ci != null ? ci.getMimeType() : null;
		//
		if (Objects.equals(message, "OLE 2 Compound Document")) {
			//
			try (final POIFSFileSystem poifs = new POIFSFileSystem(file)) {
				//
				final List<String> oleEntryNames = getOleEntryNames(poifs);
				//
				if (Objects.equals(oleEntryNames, Arrays.asList("EncryptedPackage", "EncryptionInfo"))) {
					//
					final Decryptor decryptor = Decryptor.getInstance(new EncryptionInfo(poifs));
					//
					if (decryptor != null && decryptor.verifyPassword(getPassword())) {
						//
						try (final InputStream is = decryptor.getDataStream(poifs)) {
							//
							return new XSSFWorkbook(is);
							//
						} // try
							//
					} // if
						//
				} else if (contains(oleEntryNames, "Workbook")) {
					//
					try {
						//
						return new HSSFWorkbook(poifs);
						//
					} catch (final EncryptedDocumentException e) {
						//
						Biff8EncryptionKey.setCurrentUserPassword(getPassword());
						//
						try {
							//
							return new HSSFWorkbook(poifs);
							//
						} finally {
							//
							Biff8EncryptionKey.setCurrentUserPassword(null);
							//
						} // try
							//
					} // try
						//
				} // if
					//
			} // try
				//
		} else if (Objects.equals(message, "Microsoft Office Open XML")
				|| Objects.equals(mimeType, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
			//
			return new XSSFWorkbook(file);
			//
		} else if (Objects.equals(mimeType, "application/zip")) {
			//
			try (final ZipFile zf = new ZipFile(file)) {
				//
				try (final InputStream is = testAndApply(Objects::nonNull,
						testAndApply(Objects::nonNull, "[Content_Types].xml", zf::getEntry, null), zf::getInputStream,
						null)) {
					//
					final DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
					//
					final DocumentBuilder db = dbf != null ? dbf.newDocumentBuilder() : null;
					//
					final Document document = db != null ? db.parse(is) : null;
					//
					final Element documentElement = document != null ? document.getDocumentElement() : null;
					//
					final NodeList childNodes = documentElement != null ? documentElement.getChildNodes() : null;
					//
					Node node = null;
					//
					boolean isXlsx = false;
					//
					for (int i = 0; childNodes != null && i < childNodes.getLength(); i++) {
						//
						if ((node = childNodes.item(i)) == null) {
							//
							continue;
							//
						} // if
							//
						if (Objects.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml",
								getTextContent(getNamedItem(node.getAttributes(), "ContentType"))) && (isXlsx = true)) {
							//
							break;
							//
						} // if
							//
					} // for
						//
					if (isXlsx) {
						//
						return new XSSFWorkbook(file);
						//
					} // if
						//
				} catch (final ParserConfigurationException | SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} // try
					//
			} // try
				//
		} // if
			//
		return null;
		//
	}

	private static <T, R, E extends Throwable> R testAndApply(final Predicate<T> predicate, final T value,
			final FailableFunction<T, R, E> functionTrue, final FailableFunction<T, R, E> functionFalse) throws E {
		return test(predicate, value) ? apply(functionTrue, value) : apply(functionFalse, value);
	}

	private static final <T> boolean test(final Predicate<T> instance, final T value) {
		return instance != null && instance.test(value);
	}

	private static <T, R, E extends Throwable> R apply(final FailableFunction<T, R, E> instance, final T value)
			throws E {
		return instance != null ? instance.apply(value) : null;
	}

	private static List<String> getOleEntryNames(final POIFSFileSystem poifs) {
		//
		List<String> list = null;
		//
		final DirectoryNode root = poifs != null ? poifs.getRoot() : null;
		//
		final Iterator<Entry> entries = root != null ? root.getEntries() : null;
		//
		Entry entry = null;
		//
		while (entries != null && entries.hasNext()) {
			//
			if ((entry = entries.next()) == null) {
				//
				continue;
				//
			} // if
				//
			add(list = ObjectUtils.getIfNull(list, ArrayList::new), entry.getName());
			//
		} // while
			//
		return list;
		//
	}

	private static <E> void add(final Collection<E> items, final E item) {
		if (items != null) {
			items.add(item);
		}
	}

	private static boolean contains(final Collection<?> items, final Object item) {
		return items != null && items.contains(item);
	}

	private static Node getNamedItem(final NamedNodeMap instance, final String name) {
		return instance != null ? instance.getNamedItem(name) : null;
	}

	private static String getTextContent(final Node instance) {
		return instance != null ? instance.getTextContent() : null;
	}

	private static String getPassword() {
		//
		final JTextComponent jtc = new JPasswordField();
		//
		return JOptionPane.showConfirmDialog(null, jtc, "Password", JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION
				? getText(jtc)
				: null;
		//
	}

	private static String getText(final JTextComponent instance) {
		return instance != null ? instance.getText() : null;
	}

}