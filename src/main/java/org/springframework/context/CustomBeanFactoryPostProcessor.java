package org.springframework.context;

import java.util.Map;

import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;

public class CustomBeanFactoryPostProcessor implements EnvironmentAware, BeanFactoryPostProcessor {

	private Environment environment = null;

	@Override
	public void setEnvironment(final Environment environment) {
		this.environment = environment;
	}

	@Override
	public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) {
		//
		addPropertySourceToPropertySourcesToLast(environment,
				getBeansOfType(beanFactory, PropertySourcesPlaceholderConfigurer.class));
		//
	}

	private static <T> Map<String, T> getBeansOfType(final ListableBeanFactory instance, final Class<T> type) {
		return instance != null ? instance.getBeansOfType(type) : null;
	}

	private static void addPropertySourceToPropertySourcesToLast(final Environment environment,
			final Map<?, PropertySourcesPlaceholderConfigurer> propertySourcesPlaceholderConfigurers) {
		//
		if (propertySourcesPlaceholderConfigurers != null && propertySourcesPlaceholderConfigurers.values() != null) {
			//
			PropertySources pss = null;
			//
			for (final PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer : propertySourcesPlaceholderConfigurers
					.values()) {
				//
				if (propertySourcesPlaceholderConfigurer == null
						|| (pss = propertySourcesPlaceholderConfigurer.getAppliedPropertySources()) == null) {
					continue;
				} // if
					//
				for (final PropertySource<?> ps : pss) {
					//
					if (ps == null || ps.getSource() == environment) {
						continue;
					} // if
						//
					if (environment instanceof ConfigurableEnvironment) {
						//
						addLast(((ConfigurableEnvironment) environment).getPropertySources(), ps);
						//
					} // if
						//
				} // for
					//
			} // for
				//
		} // if
			//
	}

	private static void addLast(final MutablePropertySources instance, final PropertySource<?> propertySource) {
		if (instance != null) {
			instance.addLast(propertySource);
		}
	}

}