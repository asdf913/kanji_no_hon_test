package kanji_no_hon_test;

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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.JFileChooser;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.google.common.reflect.Reflection;

import freemarker.cache.ClassTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Main {

	public static class Text {

		private String text, hiragana = null;

		public String getText() {
			return text;
		}

		public String getHiragana() {
			return hiragana;
		}

	}

	public static void main(final String[] args)
			throws InvalidFormatException, IOException, TemplateException, IllegalAccessException {
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
			IntMap<Field> intMap = null;
			//
			for (final Sheet sheet : workbook) {
				//
				if (sheet == null || sheet.iterator() == null) {
					continue;
				} // if
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
								intMap.setObject(cell.getColumnIndex(), orElse(
										findFirst(testAndApply(Objects::nonNull, fs, Arrays::stream, null).filter(
												field -> Objects.equals(getName(field), cell.getStringCellValue()))),
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
							}
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
						texts.add(text);
						//
					} // if
						//
				} // for
					//
			} // for
				//
		} // try
			//
		final Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
		//
		configuration.setTemplateLoader(new ClassTemplateLoader(Main.class, "/"));
		//
		final Template template = configuration.getTemplate("template.html.ftl");
		//
		if (template != null) {
			//
			try (final Writer w = new FileWriter(new File("test.html"))) {// TODO
				//
				final Map<String, Object> data = new LinkedHashMap<>(Collections.singletonMap("texts", texts));
				//
				template.process(data, w);
				//
			} // try
				//
		} // if
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

	private static <E> void add(final Collection<E> items, final E item) {
		if (items != null) {
			items.add(item);
		}
	}

}