package org.springframework.context.support;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertyResolverUtil;

public class Main {

	private Main() {
	}

	public static void main(final String[] args) {
		//
		try (final ConfigurableApplicationContext beanFactory = new ClassPathXmlApplicationContext(
				"applicationContext.xml")) {
			//
			final PropertyResolver environment = beanFactory.getEnvironment();
			//
			Class<?> clz = forName(
					PropertyResolverUtil.getProperty(environment, "org.springframework.context.support.Main.class"));
			//
			if (clz == null) {
				//
				final Map<String, Window> windows = getBeansOfType(beanFactory, Window.class);
				//
				final ComboBoxModel<String> cbm = testAndApply(Objects::nonNull,
						toArray(windows.keySet(), new String[] {}), DefaultComboBoxModel::new,
						x -> new DefaultComboBoxModel<>());
				//
				if (JOptionPane.showConfirmDialog(null, new JComboBox<>(cbm), null,
						JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
					//
					clz = getClass(get(windows, cbm != null ? cbm.getSelectedItem() : null));
					//
				} // if
					//
			} // if
				//
			if (clz == null) {
				//
				JOptionPane.showMessageDialog(null, "java.lang.Class is null");
				//
				return;
				//
			} // if
				//
			final Object instance = getInstance(beanFactory, clz, x -> JOptionPane.showMessageDialog(null, x));
			//
			if (instance instanceof Window) {
				//
				((Window) instance).pack();
				//
			} // if
				//
			if (instance instanceof Component) {
				//
				((Component) instance).setVisible(true);
				//
			} // if
				//
			final String key = String.format("%1$s.title", clz.getName());
			//
			if (instance instanceof Frame && environment != null && environment.containsProperty(key)) {
				//
				((Frame) instance).setTitle(PropertyResolverUtil.getProperty(environment, key));
				//
			} // if
				//
		} // try
			//
	}

	private static <V> V get(final Map<?, V> instance, final Object key) {
		return instance != null ? instance.get(key) : null;
	}

	private static String toString(final Object instance) {
		return instance != null ? instance.toString() : null;
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

	private static <T> Map<String, T> getBeansOfType(final ListableBeanFactory instance, final Class<T> type) {
		return instance != null ? instance.getBeansOfType(type) : null;
	}

	private static Class<?> forName(final String className) {
		try {
			return StringUtils.isNotBlank(className) ? Class.forName(className) : null;
		} catch (final ClassNotFoundException e) {
			return null;
		}
	}

	private static Object getInstance(final ListableBeanFactory beanFactory, final Class<?> clz,
			final Consumer<String> consumer) {
		//
		if (clz == null) {
			//
			accept(consumer, "java.lang.Class is null");
			//
			return null;
			//
		} // if
			//
		final Map<?, ?> beans = getBeansOfType(beanFactory, clz);
		//
		if (beans == null) {
			//
			accept(consumer, String.format(
					"org.springframework.beans.factory.ListableBeanFactory.getBeansOfType(%1$s) return null", clz));
			//
			return null;
			//
		} else if (beans.isEmpty()) {
			//
			accept(consumer, String.format(
					"org.springframework.beans.factory.ListableBeanFactory.getBeansOfType(%1$s) return empty", clz));
			//
			return null;
			//
		} else if (beans.size() > 1) {
			//
			accept(consumer, String.format(
					"org.springframework.beans.factory.ListableBeanFactory.getBeansOfType(%1$s).size()>1", clz));
			//
			return null;
			//
		} // if
			//
		return IterableUtils.first(beans.values());
		//
	}

	private static <T> void accept(final Consumer<T> instance, final T value) {
		if (instance != null) {
			instance.accept(value);
		}
	}

}