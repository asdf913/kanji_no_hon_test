package org.springframework.context.support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

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
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import com.google.common.reflect.Reflection;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.miginfocom.swing.MigLayout;

public class VocabularyList extends JFrame implements ActionListener, EnvironmentAware {

	private static final long serialVersionUID = -7531663604832571859L;

	private static final String WRAP = "wrap";

	private static final String GROWX = "growx";

	private PropertyResolver propertyResolver = null;

	private String templateFile = null;

	private AbstractButton btnExecute = null;

	private JTextComponent tfChapterStart, tfChapterEnd, tfSectionStart, tfSectionEnd = null;

	private VocabularyList() {
	}

	public void setTemplateFile(final String templateFile) {
		this.templateFile = templateFile;
	}

	@Override
	public void setEnvironment(final Environment environment) {
		propertyResolver = environment;
	}

	private void init() {
		//
		add(new JLabel("Chapter Range"));
		//
		add(tfSectionStart = new JTextField(
				getProperty(propertyResolver, "org.springframework.context.support.VocabularyList.chapterStart")),
				String.format("wmin %1$spx", 50));
		//
		add(new JLabel(" - "));
		//
		add(tfSectionEnd = new JTextField(
				getProperty(propertyResolver, "org.springframework.context.support.VocabularyList.chapterEnd")),
				String.format("%1$s,wmin %2$spx", WRAP, 50));
		//
		add(new JLabel("Section Range"));
		//
		add(tfChapterStart = new JTextField(
				getProperty(propertyResolver, "org.springframework.context.support.VocabularyList.sectionStart")),
				GROWX);
		//
		add(new JLabel(" - "));
		//
		add(tfChapterEnd = new JTextField(
				getProperty(propertyResolver, "org.springframework.context.support.VocabularyList.sectionEnd")),
				String.format("%1$s,%2$s", WRAP, GROWX));
		//
		add(new JLabel());
		//
		add(btnExecute = new JButton("Execute"), String.format("span %1$s", 3));
		//
		btnExecute.addActionListener(this);
		//
	}

	private static String getProperty(final PropertyResolver instance, final String key) {
		return instance != null ? instance.getProperty(key) : null;
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
			List<Text> texts = null;
			//
			try (final Workbook workbook = new XSSFWorkbook(file)) {
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
				final Integer chapterStart = valueOf(getText(tfChapterStart));
				//
				final Integer chapterEnd = valueOf(getText(tfChapterEnd));
				//
				final Integer sectionStart = valueOf(getText(tfSectionStart));
				//
				final Integer sectionEnd = valueOf(getText(tfSectionEnd));
				//
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
									intMap.setObject(cell.getColumnIndex(),
											orElse(findFirst(testAndApply(Objects::nonNull, fs, Arrays::stream, null)
													.filter(field -> Objects.equals(getName(field),
															cell.getStringCellValue()))),
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
							if (chapterStart != null && text.chapter != null
									&& chapterStart.intValue() > text.chapter.intValue()) {// chapter
								//
								continue;
								//
							} else if (chapterEnd != null && text.chapter != null
									&& chapterEnd.intValue() < text.chapter.intValue()) {// chapter
								//
								continue;
								//
							} else if (sectionStart != null && text.section != null
									&& sectionStart.intValue() > text.section.intValue()) {// section
								//
								continue;
								//
							} else if (sectionEnd != null && text.section != null
									&& sectionEnd.intValue() < text.section.intValue()) {// section
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
			if (template != null) {
				//
				try (final Writer w = new FileWriter(new File("VocabularyList.html"))) {// TODO
					//
					final Map<String, Object> data = new LinkedHashMap<>(Collections.singletonMap("texts", texts));
					//
					template.process(data, w);
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
		} // if
			//
	}

	private static String getText(final JTextComponent instance) {
		return instance != null ? instance.getText() : null;
	}

	private static Integer valueOf(final String instance) {
		try {
			return StringUtils.isNotBlank(instance) ? Integer.valueOf(instance) : null;
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	public static class Text {

		private String text, hiragana = null;

		private Integer chapter, section = null;

		public String getText() {
			return text;
		}

		public String getHiragana() {
			return hiragana;
		}

	}

	public static void main(final String[] args) {
		//
		final VocabularyList instance = new VocabularyList();
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