package org.springframework.context.support;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.QueryFunction;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertyResolverUtil;

import com.google.common.reflect.Reflection;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateUtil;
import net.miginfocom.swing.MigLayout;

public class KanjiNoHon extends JFrame implements ActionListener, KeyListener, EnvironmentAware {

	private static final long serialVersionUID = -7531663604832571859L;

	private static final String WRAP = "wrap";

	private static final String GROWX = "growx";

	private PropertyResolver propertyResolver = null;

	private String templateFile = null;

	private AbstractButton btnExecute = null;

	private JTextComponent tfNumberStart, tfNumberEnd, tfUnitStart, tfUnitEnd, tfFileName = null;

	private ComboBoxModel<?> cbmClass = null;

	private JComboBox<?> jcbClass = null;

	private KanjiNoHon() {
	}

	@Override
	public void setEnvironment(final Environment environment) {
		propertyResolver = environment;
	}

	public void setTemplateFile(final String templateFile) {
		this.templateFile = templateFile;
	}

	private void init() {
		//
		add(new JLabel("Unit Range"));
		//
		add(tfUnitStart = new JTextField(PropertyResolverUtil.getProperty(propertyResolver,
				"org.springframework.context.support.KanjiNoHon.unitStart")), String.format("wmin %1$spx", 50));
		//
		tfUnitStart.addKeyListener(this);
		//
		add(new JLabel(" - "));
		//
		final String span = String.format("%1$s,wmin %2$spx", WRAP, 50);
		//
		add(tfUnitEnd = new JTextField(PropertyResolverUtil.getProperty(propertyResolver,
				"org.springframework.context.support.KanjiNoHon.unitEnd")), span);
		//
		add(new JLabel("Number Range"));
		//
		add(tfNumberStart = new JTextField(PropertyResolverUtil.getProperty(propertyResolver,
				"org.springframework.context.support.KanjiNoHon.numberStart")), GROWX);
		//
		add(new JLabel(" - "));
		//
		add(tfNumberEnd = new JTextField(PropertyResolverUtil.getProperty(propertyResolver,
				"org.springframework.context.support.KanjiNoHon.numberEnd")), span);
		//
		add(new JLabel("File Name Generator"));
		//
		add(jcbClass = new JComboBox(cbmClass = testAndApply(Objects::nonNull,
				ArrayUtils.insert(0,
						toArray(new Reflections(getPackageName(getClass()))
								.get(asClass(Scanners.SubTypes.of(FileNameGenerator.class))), new Class<?>[] {}),
						(Class<?>) null),
				DefaultComboBoxModel::new, null)), String.format("%1$s,span %2$s", WRAP, 3));
		//
		jcbClass.addActionListener(this);
		//
		Class<?> clz = null;
		//
		for (int i = 0; cbmClass != null && i < cbmClass.getSize(); i++) {
			//
			if ((clz = cast(Class.class, cbmClass.getElementAt(i))) == null) {
				//
				continue;
				//
			} // if
				//
			if (Objects.equals(PropertyResolverUtil.getProperty(propertyResolver,
					"org.springframework.context.support.KanjiNoHon.FileNameGeneratorClass"), clz.getName())) {
				//
				cbmClass.setSelectedItem(clz);
				//
			} // if
				//
		} // for
			//
		add(new JLabel("File Name"));
		//
		add(tfFileName = new JTextField(), String.format("%1$s,%2$s,span %3$s", WRAP, GROWX, 3));
		//
		tfFileName.setEditable(false);
		//
		add(new JLabel());
		//
		add(btnExecute = new JButton("Execute"), String.format("span %1$s", 4));
		//
		btnExecute.addActionListener(this);
		//
		addKeyListener(this, tfUnitStart, tfUnitEnd, tfNumberStart, tfNumberEnd);
		//
	}

	private static void addKeyListener(final KeyListener keyListener, final Component a, final Component b,
			final Component... cs) {
		//
		addKeyListener(a, keyListener);
		//
		addKeyListener(b, keyListener);
		//
		for (int i = 0; cs != null && i < cs.length; i++) {
			//
			addKeyListener(cs[i], keyListener);
			//
		} // for
			//
	}

	private static void addKeyListener(final Component instance, final KeyListener keyListener) {
		//
		if (instance != null) {
			instance.addKeyListener(keyListener);
		} // if
			//
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

	private static <C, R> QueryFunction<C, Class<?>> asClass(final QueryFunction<C, ?> instance,
			final ClassLoader... loaders) {
		return instance != null ? instance.asClass(loaders) : null;
	}

	private static String getPackageName(final Class<?> instance) {
		return instance != null ? instance.getPackageName() : null;
	}

	private static interface FileNameGenerator {

		String genereate(final KanjiNoHon instance);

	}

	private static class FileNameGeneratorImpl implements FileNameGenerator {

		@Override
		public String genereate(final KanjiNoHon instance) {
			//
			final StringBuilder sb = new StringBuilder("KanjiNoHon");
			//
			if (instance != null) {
				//
				final String unitStart = getText(instance.tfUnitStart);
				//
				final String unitEnd = getText(instance.tfUnitEnd);
				//
				if (StringUtils.isNotBlank(unitStart) || StringUtils.isNotBlank(unitEnd)) {
					//
					append(sb, '(');
					//
					append(sb, StringUtils.trim(unitStart));
					//
					if (StringUtils.isNotBlank(unitEnd)) {
						//
						if (StringUtils.isNotBlank(unitStart) && sb.length() > 0) {
							//
							sb.append('-');
							//
						} // if
							//
						append(sb, StringUtils.trim(unitEnd));
						//
					} // if
						//
					append(sb, ')');
					//
				} // if
					//
			} // if
				//
			return toString(append(sb, ".html"));
			//
		}

		private static StringBuilder append(final StringBuilder instance, final char c) {
			return instance != null ? instance.append(c) : null;
		}

		private static StringBuilder append(final StringBuilder instance, final String str) {
			return instance != null ? instance.append(str) : null;
		}

		private static String toString(final Object instance) {
			return instance != null ? instance.toString() : null;
		}

	}

	@Override
	public void actionPerformed(final ActionEvent evt) {
		//
		final Object source = getSource(evt);
		//
		if (Objects.equals(source, btnExecute)) {
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
			List<Text> texts = null;
			//
			try (final Workbook workbook = file != null ? new XSSFWorkbook(file) : null) {
				//
				Text text = null;
				//
				int columnIndex;
				//
				boolean first = true;
				//
				Field[] fs = null;
				//
				Field f = null;
				//
				String string = null;
				//
				Integer integer = null;
				//
				IntMap<Field> intMap = null;
				//
				final Integer unitStart = valueOf(getText(tfUnitStart));
				//
				final Integer unitEnd = valueOf(getText(tfUnitEnd));
				//
				final Integer numberStart = valueOf(getText(tfNumberStart));
				//
				final Integer numberEnd = valueOf(getText(tfNumberEnd));
				//
				if (workbook != null) {
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
							text = new Text();
							//
							if (row == null || row.iterator() == null) {
								continue;
							} // if
								//
							for (final Cell cell : row) {
								//
								if (first) {
									//
									if (fs == null) {
										//
										fs = FieldUtils.getAllFields(Text.class);
										//
									} // if
										//
									if ((intMap = ObjectUtils.getIfNull(intMap,
											() -> Reflection.newProxy(IntMap.class, new IH()))) != null) {
										//
										intMap.setObject(cell.getColumnIndex(), orElse(findFirst(testAndApply(
												Objects::nonNull, fs, Arrays::stream, null)
												.filter(field -> Objects.equals(getName(field),
														Objects.equals(cell.getCellType(), CellType.NUMERIC)
																? Integer.toString(Double
																		.valueOf(cell.getNumericCellValue()).intValue())
																: cell.getStringCellValue()))),
												null));
										//
									} // if
										//
								} else if (intMap != null && intMap.containsKey(columnIndex = cell.getColumnIndex())
										&& (f = intMap.getObject(columnIndex)) != null) {
									// //
									f.setAccessible(true);
									//
									if (Objects.equals(f.getType(), String.class)) {
										//
										if (Objects.equals(cell.getCellType(), CellType.NUMERIC)) {
											//
											string = Double.toString(cell.getNumericCellValue());
											//
										} else {
											//
											string = cell.getStringCellValue();
											//
										} // if
											//
										f.set(text = ObjectUtils.getIfNull(text, Text::new), string);
										//
									} else if (Objects.equals(f.getType(), Integer.class)) {
										//
										if (Objects.equals(cell.getCellType(), CellType.NUMERIC)) {
											//
											integer = Integer
													.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
											//
										} else {
											//
											integer = valueOf(cell.getStringCellValue());
											//
										} // if
											//
										f.set(text = ObjectUtils.getIfNull(text, Text::new), integer);
										//
									} // if
										//
								} // if
									//
							} // for
								//
							if (first) {
								//
								first = false;
								//
							} else if ((texts = ObjectUtils.getIfNull(texts, ArrayList::new)) != null) {
								//
								if (unitStart != null && text.unit != null
										&& unitStart.intValue() > text.unit.intValue()) {// unit
									//
									continue;
									//
								} else if (unitEnd != null && text.unit != null
										&& unitEnd.intValue() < text.unit.intValue()) {// unit
									//
									continue;
									//
								} else if (numberStart != null && text.number != null
										&& numberStart.intValue() > text.number.intValue()) {// number
									//
									continue;
									//
								} else if (numberEnd != null && text.number != null
										&& numberEnd.intValue() < text.number.intValue()) {// number
									//
									continue;
									//
								} //
									//
								texts.add(text);
								//
							} // if
								//
						} // for
							//
					} // for
						//
				} // if
					//
			} catch (final InvalidFormatException | IOException | IllegalAccessException e) {
				//
				// TODO Auto-generated catch block
				//
				e.printStackTrace();
				//
			} // try
				//
			final Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
			//
			configuration.setTemplateLoader(new ClassTemplateLoader(KanjiNoHon.class, "/"));
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
			try (final Writer w = testAndApply(
					Objects::nonNull, testAndApply(Objects::nonNull,
							StringUtils.defaultIfBlank(getFileName(), "KanjiNoHon.html"), File::new, null),
					FileWriter::new, null)) {
				//
				if (w != null) {
					//
					TemplateUtil.process(template, new LinkedHashMap<>(Collections.singletonMap("texts", texts)), w);
					//
				} // if
					//
			} catch (final IOException | TemplateException | InstantiationException | IllegalAccessException e) {
				//
				// TODO Auto-generated catch block
				//
				e.printStackTrace();
				//
			} catch (final InvocationTargetException e) {
				//
				// TODO Auto-generated catch block
				//
				e.printStackTrace();
				//
			} // try
				//
		} else if (Objects.equals(source, jcbClass)) {
			//
			setFileName();
			//
		} // if
			//
	}

	@Override
	public void keyTyped(final KeyEvent evt) {
	}

	@Override
	public void keyPressed(final KeyEvent evt) {
	}

	@Override
	public void keyReleased(final KeyEvent evt) {
		//
		if (contains(Arrays.asList(tfUnitStart, tfUnitEnd, tfNumberStart, tfNumberEnd), getSource(evt))) {
			//
			setFileName();
			//
		} // if
			//
	}

	private void setFileName() {
		//
		final Class<?> clz = cbmClass != null ? cast(Class.class, cbmClass.getSelectedItem()) : null;
		//
		final List<Constructor<?>> cs = toList(
				filter(testAndApply(Objects::nonNull, clz != null ? clz.getDeclaredConstructors() : null,
						Arrays::stream, null), c -> c != null && c.getParameterCount() == 0));
		//
		Constructor<?> constructor = null;
		//
		if (cs != null && !cs.isEmpty()) {
			//
			if (cs.size() > 1) {
				//
				throw new IllegalStateException();
				//
			} // if
				//
			constructor = cs.get(0);
			//
		} // if
			//
		try {
			//
			setText(tfFileName, getFileName());
			//
		} catch (final InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // if
			//
	}

	private String getFileName() throws InstantiationException, IllegalAccessException, InvocationTargetException {
		//
		final Class<?> clz = cbmClass != null ? cast(Class.class, cbmClass.getSelectedItem()) : null;
		//
		final List<Constructor<?>> cs = toList(
				filter(testAndApply(Objects::nonNull, clz != null ? clz.getDeclaredConstructors() : null,
						Arrays::stream, null), c -> c != null && c.getParameterCount() == 0));
		//
		Constructor<?> constructor = null;
		//
		if (cs != null && !cs.isEmpty()) {
			//
			if (cs.size() > 1) {
				//
				throw new IllegalStateException();
				//
			} // if
				//
			constructor = cs.get(0);
			//
		} // if
			//
		final FileNameGenerator fileNameGenerator = cast(FileNameGenerator.class,
				constructor != null ? constructor.newInstance() : null);
		//
		return fileNameGenerator != null ? fileNameGenerator.genereate(this) : null;
		//
	}

	private static <T> List<T> toList(final Stream<T> instance) {
		return instance != null ? instance.toList() : null;
	}

	private static <T> Stream<T> filter(final Stream<T> instance, final Predicate<? super T> predicate) {
		//
		return instance != null && (predicate != null || Proxy.isProxyClass(getClass(instance)))
				? instance.filter(predicate)
				: null;
		//
	}

	private static <T> T cast(final Class<T> clz, final Object value) {
		return clz != null && clz.isInstance(value) ? clz.cast(value) : null;
	}

	private static boolean contains(final Collection<?> items, final Object item) {
		return items != null && items.contains(item);
	}

	private static Object getSource(final EventObject instance) {
		return instance != null ? instance.getSource() : null;
	}

	private static String getText(final JTextComponent instance) {
		return instance != null ? instance.getText() : null;
	}

	private static void setText(final JTextComponent instance, final String text) {
		if (instance != null) {
			instance.setText(text);
		}
	}

	private static Integer valueOf(final String instance) {
		try {
			return StringUtils.isNotBlank(instance) ? Integer.valueOf(instance) : null;
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	public static class Text {

		private String text, hiragana, hint = null;

		private Integer unit, number = null;

		public String getText() {
			return text;
		}

		public String getHiragana() {
			return hiragana;
		}

		public String getHint() {
			return hint;
		}

	}

	public static void main(final String[] args) {
		//
		final KanjiNoHon instance = new KanjiNoHon();
		//
		instance.setLayout(new MigLayout());// TODO
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

		private static <K, V> void put(final Map<K, V> instance, final K key, final V value) {
			if (instance != null) {
				instance.put(key, value);
			}
		}

	}

	private static interface IntMap<T> {

		T getObject(final int key);

		boolean containsKey(final int key);

		void setObject(final int key, final T value);

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

	private static <T> Optional<T> findFirst(final Stream<T> instance) {
		return instance != null ? instance.findFirst() : null;
	}

	private static <T> T orElse(final Optional<T> instance, final T other) {
		return instance != null ? instance.orElse(other) : null;
	}

	private static String getName(final Member instance) {
		return instance != null ? instance.getName() : null;
	}

}