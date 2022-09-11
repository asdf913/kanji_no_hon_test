package org.springframework.context.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookUtil;
import org.javatuples.Unit;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertyResolverUtil;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gargoylesoftware.htmlunit.SgmlPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.google.common.reflect.Reflection;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateUtil;
import net.miginfocom.swing.MigLayout;

public class Katsuyou extends JFrame implements ActionListener, EnvironmentAware {

	private static final long serialVersionUID = -7531663604832571859L;

	private static final String WRAP = "wrap";

	private PropertyResolver propertyResolver = null;

	private String templateFile, katsuyouPageUrl = null;

	private AbstractButton btnExecute = null;

	private Unit<List<String>> japaneseVerbConjugations = null;

	private Map<String, AbstractButton> japaneseVerbConjugationCheckBoxes = null;

	private ComboBoxModel<String> cbmJapaneseVerbConjugation = null;

	private Katsuyou() {
	}

	public void setTemplateFile(final String templateFile) {
		this.templateFile = templateFile;
	}

	public void setKatsuyouPageUrl(final String katsuyouPageUrl) {
		this.katsuyouPageUrl = katsuyouPageUrl;
	}

	@Override
	public void setEnvironment(final Environment environment) {
		propertyResolver = environment;
	}

	private void init() {
		//
		try {
			//
			final List<String> japaneseVerbConjugations = getValue0(getJapaneseVerbConjugations());
			//
			add(new JLabel("Show"));
			//
			add(new JComboBox(cbmJapaneseVerbConjugation = new DefaultComboBoxModel<>(
					toArray(japaneseVerbConjugations, new String[] {}))), WRAP);
			//
			cbmJapaneseVerbConjugation.setSelectedItem(PropertyResolverUtil.getProperty(propertyResolver,
					"org.springframework.context.support.Katsuyou.conjugation"));
			//
			add(new JLabel("Input"));
			//
			String japaneseVerbConjugation = null;
			//
			AbstractButton abstractButton = null;
			//
			final List<Object> conjugations = getObjectList(new ObjectMapper(), PropertyResolverUtil
					.getProperty(propertyResolver, "org.springframework.context.support.Katsuyou.conjugations"));
			//
			for (int i = 0; japaneseVerbConjugations != null && i < japaneseVerbConjugations.size(); i++) {
				//
				put(japaneseVerbConjugationCheckBoxes = ObjectUtils.getIfNull(japaneseVerbConjugationCheckBoxes,
						LinkedHashMap::new), japaneseVerbConjugation = japaneseVerbConjugations.get(i),
						abstractButton = new JCheckBox(japaneseVerbConjugation));
				//
				if (i < japaneseVerbConjugations.size() - 1) {
					add(abstractButton);
				} else {
					add(abstractButton, WRAP);
				} // if
					//
				if (contains(conjugations, japaneseVerbConjugation)) {
					//
					abstractButton.setSelected(true);
					//
				} // if
					//
			} // for
				//
		} catch (final IOException e) {
			//
			// TODO Auto-generated catch block
			//
			e.printStackTrace();
			//
		} // try
			//
		add(new JLabel());
		//
		add(btnExecute = new JButton("Execute"), String.format("span %1$s", 3));
		//
		btnExecute.addActionListener(this);
		//
	}

	private static boolean contains(final Collection<?> items, final Object item) {
		return items != null && items.contains(item);
	}

	private static List<Object> getObjectList(final ObjectMapper objectMapper, final Object value) {
		//
		if (value == null) {
			//
			return null;
			//
		} // if
			//
		final Iterable<?> iterable = cast(Iterable.class, value);
		//
		if (iterable != null) {
			//
			if (iterable.iterator() == null) {
				//
				return null;
				//
			} //
				//
			List<Object> list = null;
			//
			for (final Object v : iterable) {
				//
				add(list = ObjectUtils.getIfNull(list, ArrayList::new), v);
				//
			} // for
				//
			return ObjectUtils.getIfNull(list, ArrayList::new);
			//
		} // if
			//
		try {
			//
			final Object object = objectMapper != null ? objectMapper.readValue(toString(value), Object.class) : null;
			//
			if (object instanceof Iterable || object == null) {
				//
				return getObjectList(objectMapper, object);
				//
			} else if (object instanceof String || object instanceof Boolean || object instanceof Number) {
				//
				return getObjectList(objectMapper, Collections.singleton(object));
				//
			} else {
				//
				throw new IllegalArgumentException(toString(getClass(object)));
				//
			} // if
		} catch (final JsonProcessingException e) {
			//
			return getObjectList(objectMapper, Collections.singleton(value));
			//
		} // try
			//
	}

	private static String toString(final Object instance) {
		return instance != null ? instance.toString() : null;
	}

	private static <A> A getValue0(final Unit<A> instance) {
		return instance != null ? instance.getValue0() : null;
	}

	private static <T> T[] toArray(final Collection<T> instance, final T[] array) {
		//
		return instance != null && (array != null || Proxy.isProxyClass(getClass(instance))) ? instance.toArray(array)
				: null;
		//
	}

	private static Class<?> getClass(final Object instance) {
		return instance != null ? instance.getClass() : null;
	}

	private static <K, V> void put(final Map<K, V> instance, final K key, final V value) {
		if (instance != null) {
			instance.put(key, value);
		}
	}

	private Unit<List<String>> getJapaneseVerbConjugations() throws IOException {
		//
		if (japaneseVerbConjugations == null) {
			//
			japaneseVerbConjugations = Unit.with(getJapaneseVerbConjugations(katsuyouPageUrl));// TODO
			//
		} // if
			//
		return japaneseVerbConjugations;
		//
	}

	private static List<String> getJapaneseVerbConjugations(final String url) throws IOException {
		//
		List<String> strings = null;
		//
		try (final WebClient webClient = new WebClient()) {
			//
			final SgmlPage sgmlPage = cast(SgmlPage.class, webClient.getPage(url));
			//
			final DomNodeList<DomElement> domNodeList = sgmlPage != null ? sgmlPage.getElementsByTagName("span") : null;
			//
			DomElement temp = null;
			//
			DomElement domElement = null;
			//
			for (int i = 0; domNodeList != null && i < domNodeList.size(); i++) {
				//
				if ((temp = domNodeList.get(i)) == null || !Objects.equals("教育文法の", getTextContent(temp))) {
					//
					continue;
					//
				} // if
					//
				if (domElement != null) {
					//
					throw new IllegalStateException();
					//
				} // if
					//
				domElement = temp;
				//
			} // for
				//
			DomNode parentNode = domElement != null ? domElement.getParentNode() : null;
			//
			while (parentNode != null) {
				//
				if (Objects.equals("table", parentNode.getNodeName())) {
					//
					break;
					//
				} else {
					//
					parentNode = parentNode.getParentNode();
					//
				} // if
					//
			} // while
				//
			DomNodeList<DomNode> domNodes = parentNode != null ? parentNode.getChildNodes() : null;
			//
			DomNode domNode = null;
			//
			DomNode tbody = null;
			//
			for (int i = 0; domNodes != null && i < domNodes.size(); i++) {
				//
				if ((domNode = domNodes.get(i)) == null || !Objects.equals("tbody", domNode.getNodeName())) {
					//
					continue;
					//
				} // if
					//
				if (tbody != null) {
					//
					throw new IllegalStateException();
					//
				} // if
					//
				tbody = domNode;
				//
			} // for
				//
			domNodes = tbody != null ? tbody.getChildNodes() : null;
			//
			int rowCounter = 0;
			//
			DomNodeList<DomNode> ths = null;
			//
			DomNode th = null;
			//
			String textContent = null;
			//
			int size = 0;
			//
			for (int i = 0; domNodes != null && i < domNodes.size(); i++) {
				//
				if ((domNode = domNodes.get(i)) == null || !Objects.equals("tr", domNode.getNodeName())
						|| (ths = domNode.getChildNodes()) == null) {
					//
					continue;
					//
				} // if
					//
				for (int j = 0; ths != null && j < ths.size(); j++) {
					//
					if ((th = ths.get(j)) == null) {
						continue;
					} // if
						//
					if (rowCounter == 0) {
						//
						if (getNamedItem(th.getAttributes(), "rowspan") != null
								&& StringUtils.endsWith(textContent = getTextContent(th.querySelector("span")), "形")) {
							//
							add(strings = ObjectUtils.getIfNull(strings, ArrayList::new), textContent);
							//
							size = CollectionUtils.size(strings);
							//
						} // if
							//
					} else if (rowCounter == 1
							&& StringUtils.endsWith(textContent = getTextContent(th.querySelector("span")), "形")
							&& (strings = ObjectUtils.getIfNull(strings, ArrayList::new)) != null) {
						//
						strings.add(CollectionUtils.size(strings) - size, textContent);
						//
					} // if
						//
				} // for
					//
				rowCounter++;
				//
			} // for
				//
		} // try
			//
		return strings;
		//
	}

	private static Node getNamedItem(final NamedNodeMap instance, final String name) {
		return instance != null ? instance.getNamedItem(name) : null;

	}

	private static <E> void add(final Collection<E> items, final E item) {
		if (items != null) {
			items.add(item);
		}
	}

	private static String getTextContent(final Node instance) {
		return instance != null ? instance.getTextContent() : null;
	}

	private static <T> T cast(final Class<T> clz, final Object value) {
		return clz != null && clz.isInstance(value) ? clz.cast(value) : null;
	}

	@Override
	public void actionPerformed(final ActionEvent evt) {
		//
		if (Objects.equals(evt != null ? evt.getSource() : null, btnExecute)) {
			//
			final JFileChooser jfc = new JFileChooser(".");
			//
			File file = null;
			//
			if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				//
				file = jfc.getSelectedFile();
				//
			} // if
				//
			List<Verb> verbs = null;
			//
			List<String> japaneseVerbConjugations = null;
			//
			try (final Workbook workbook = WorkbookUtil.getWorkbook(file)) {
				//
				Verb verb = null;
				//
				int columnIndex;
				//
				boolean first = true;
				//
//				Field[] fs = null;
				//
				Field f = null;
				//
				String string = null;
				//
				Integer integer = null;
				//
				IntMap<String> intMap = null;
				//
				String jvc = null;
				//
				for (final Sheet sheet : workbook) {
					//
					if (sheet == null || sheet.iterator() == null) {
						//
						continue;
						//
					} // if
						//
					first = true;
					//
					for (final Row row : sheet) {
						//
						verb = new Verb();
						//
						if (row == null || row.iterator() == null) {
							continue;
						} // if
							//
						for (final Cell cell : row) {
							//
							if (first) {
								//
								if ((intMap = ObjectUtils.getIfNull(intMap,
										() -> Reflection.newProxy(IntMap.class, new IH()))) != null) {
									//
									if (japaneseVerbConjugations == null) {
										//
										japaneseVerbConjugations = getValue0(getJapaneseVerbConjugations());
										//
									} // if
										//
									intMap.setObject(
											cell.getColumnIndex(), orElse(
													findFirst(stream(japaneseVerbConjugations)
															.filter(x -> Objects.equals(x, cell.getStringCellValue()))),
													null));
									//
								} // if
									//
							} else if (intMap != null && intMap.containsKey(columnIndex = cell.getColumnIndex())
									&& StringUtils.isNotBlank(jvc = intMap.getObject(columnIndex))) {
								//
								put(verb.getConjugations(), jvc, cell.getStringCellValue());
								//
							} // if
								//
						} // for
							//
						if (first) {
							//
							first = false;
							//
						} else if ((verbs = ObjectUtils.getIfNull(verbs, ArrayList::new)) != null) {
							//
							verbs.add(verb);
							//
						} // if
							//
					} // for
						//
				} // for
					//
			} catch (final InvalidFormatException | IOException | GeneralSecurityException e) {
				//
				// TODO Auto-generated catch block
				//
				e.printStackTrace();
				//
			} // try
				//
			final Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
			//
			configuration.setTemplateLoader(new ClassTemplateLoader(VocabularyList.class, "/"));
			//
			Template template = null;
			//
			try {
				//
				template = configuration.getTemplate(templateFile);
				//
			} catch (final IOException e) {
				//
				// TODO Auto-generated catch block
				//
				e.printStackTrace();
				//
			} // try
				//
			try (final Writer w = new FileWriter(new File("Katsuyou.html"))) {// TODO
				//
				final Map<String, Object> map = new LinkedHashMap<>(Collections.singletonMap("verbs", verbs));
				//
				final Object conjugationShown = cbmJapaneseVerbConjugation != null
						? cbmJapaneseVerbConjugation.getSelectedItem()
						: null;
				//
				put(map, "conjugationShown", conjugationShown);
				//
				if (japaneseVerbConjugations == null) {
					//
					japaneseVerbConjugations = getValue0(getJapaneseVerbConjugations());
					//
				} // if
					//
				final int index = japaneseVerbConjugations.indexOf(conjugationShown);
				//
				if (index > 0) {
					//
					japaneseVerbConjugations.add(0, japaneseVerbConjugations.remove(index));
					//
				} // if
					//
				if (japaneseVerbConjugationCheckBoxes != null && japaneseVerbConjugationCheckBoxes.entrySet() != null) {
					//
					AbstractButton ab = null;
					//
					for (final Entry<String, AbstractButton> en : japaneseVerbConjugationCheckBoxes.entrySet()) {
						//
						if (en == null || (ab = en.getValue()) == null || ab.isSelected()) {
							//
							continue;
							//
						} // if
							//
						if (!Objects.equals(conjugationShown, en.getKey())) {
							//
							japaneseVerbConjugations.remove(en.getKey());
							//
						} // if
							//
					} // for
						//
				} // if
					//
				put(map, "conjugations", japaneseVerbConjugations);
				//
				TemplateUtil.process(template, map, w);
				//
			} catch (final IOException | TemplateException e) {
				//
				// TODO Auto-generated catch block
				//
				e.printStackTrace();
				//
			} // try
				//
		} // if
			//
	}

	private static <E> Stream<E> stream(final Collection<E> instance) {
		return instance != null ? instance.stream() : null;
	}

	public static class Verb {

		private Map<String, String> conjugations = null;

		public Map<String, String> getConjugations() {
			return conjugations = ObjectUtils.getIfNull(conjugations, LinkedHashMap::new);
		}

	}

	public static void main(final String[] args) {
		//
		final Katsuyou instance = new Katsuyou();
		//
		instance.setLayout(new MigLayout());
		//
		instance.init();
		//
		instance.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//
		instance.pack();
		//
		instance.setVisible(true);
		//
	}

	private static class IH implements InvocationHandler {

		private Map<Object, Object> objects = null;

		private Map<Object, Object> getObjects() {
			if (objects == null) {
				objects = new LinkedHashMap<>();
			}
			return objects;
		}

		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			//
			final String methodName = method != null ? method.getName() : null;
			//
			if (proxy instanceof IntMap) {
				//
				if (Objects.equals(methodName, "getObject") && args != null && args.length > 0) {
					//
					final Object key = args[0];
					//
					if (!containsKey(getObjects(), key)) {
						//
						throw new IllegalStateException(String.format("Key [%1$s] Not Found", key));
						//
					} // if
						//
					return getObjects().get(key);
					//
				} else if (Objects.equals(methodName, "containsKey") && args != null && args.length > 0) {
					//
					return containsKey(getObjects(), args[0]);
					//
				} else if (Objects.equals(methodName, "setObject") && args != null && args.length > 1) {
					//
					put(getObjects(), args[0], args[1]);
					//
					return null;
					//
				} // if
					//
			} // if
				//
			throw new Throwable(methodName);
			//
		}

		private static boolean containsKey(final Map<?, ?> instance, final Object key) {
			return instance != null && instance.containsKey(key);
		}

	}

	private static interface IntMap<T> {

		T getObject(final int key);

		boolean containsKey(final int key);

		void setObject(final int key, final T value);

	}

	private static <T> Optional<T> findFirst(final Stream<T> instance) {
		return instance != null ? instance.findFirst() : null;
	}

	private static <T> T orElse(final Optional<T> instance, final T other) {
		return instance != null ? instance.orElse(other) : null;
	}

}