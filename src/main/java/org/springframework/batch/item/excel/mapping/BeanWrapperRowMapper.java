package org.springframework.batch.item.excel.mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.batch.item.excel.RowMapper;
import org.springframework.batch.item.excel.support.rowset.RowSet;
import org.springframework.batch.support.DefaultPropertyEditorRegistrar;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.beans.PropertyAccessor;
import org.springframework.beans.PropertyAccessorUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.DataBinder;

public class BeanWrapperRowMapper<T> extends DefaultPropertyEditorRegistrar
		implements RowMapper<T>, BeanFactoryAware, InitializingBean {

	private String name;

	private Class<? extends T> type;

	private BeanFactory beanFactory;

	private ConcurrentMap<DistanceHolder, ConcurrentMap<String, String>> propertiesMatched = new ConcurrentHashMap<DistanceHolder, ConcurrentMap<String, String>>();

	private int distanceLimit = 5;

	private boolean strict = true;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	public void setDistanceLimit(int distanceLimit) {
		this.distanceLimit = distanceLimit;
	}

	public void setPrototypeBeanName(String name) {
		this.name = name;
	}

	public void setTargetType(Class<? extends T> type) {
		this.type = type;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.state(name != null || type != null, "Either name or type must be provided.");
		Assert.state(name == null || type == null, "Both name and type cannot be specified together.");
	}

	@Override
	public T mapRow(RowSet rs) throws BindException {
		T copy = getBean();
		DataBinder binder = createBinder(copy);
		binder.bind(new MutablePropertyValues(getBeanProperties(copy, rs.getProperties())));
		if (binder.getBindingResult().hasErrors()) {
			throw new BindException(binder.getBindingResult());
		}
		return copy;
	}

	protected DataBinder createBinder(Object target) {
		DataBinder binder = new DataBinder(target);
		binder.setIgnoreUnknownFields(!this.strict);
		initBinder(binder);
		registerCustomEditors(binder);
		return binder;
	}

	protected void initBinder(DataBinder binder) {
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
	private T getBean() {
		if (name != null) {
			return (T) beanFactory.getBean(name);
		}
		try {
			return type.newInstance();
		} catch (InstantiationException e) {
			ReflectionUtils.handleReflectionException(e);
		} catch (IllegalAccessException e) {
			ReflectionUtils.handleReflectionException(e);
		}
		// should not happen
		throw new IllegalStateException("Internal error: could not create bean instance for mapping.");
	}

	private Properties getBeanProperties(Object bean, Properties properties) {

		Class<?> cls = bean.getClass();

		// Map from field names to property names
		DistanceHolder distanceKey = new DistanceHolder(cls, distanceLimit);
		if (!propertiesMatched.containsKey(distanceKey)) {
			propertiesMatched.putIfAbsent(distanceKey, new ConcurrentHashMap<String, String>());
		}
		Map<String, String> matches = new HashMap<String, String>(propertiesMatched.get(distanceKey));

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Set<String> keys = new HashSet(properties.keySet());
		for (String key : keys) {

			if (matches.containsKey(key)) {
				switchPropertyNames(properties, key, matches.get(key));
				continue;
			}

			String name = findPropertyName(bean, key);

			if (name != null) {
				if (matches.containsValue(name)) {
					throw new NotWritablePropertyException(cls, name, "Duplicate match with distance <= "
							+ distanceLimit + " found for this property in input keys: " + keys
							+ ". (Consider reducing the distance limit or changing the input key names to get a closer match.)");
				}
				matches.put(key, name);
				switchPropertyNames(properties, key, name);
			}
		}

		propertiesMatched.replace(distanceKey, new ConcurrentHashMap<String, String>(matches));
		return properties;
	}

	private String findPropertyName(Object bean, String key) {

		if (bean == null) {
			return null;
		}

		Class<?> cls = bean.getClass();

		int index = PropertyAccessorUtils.getFirstNestedPropertySeparatorIndex(key);
		String prefix;
		String suffix;

		// If the property name is nested recurse down through the properties
		// looking for a match.
		if (index > 0) {
			prefix = key.substring(0, index);
			suffix = key.substring(index + 1, key.length());
			String nestedName = findPropertyName(bean, prefix);
			if (nestedName == null) {
				return null;
			}

			Object nestedValue = getPropertyValue(bean, nestedName);
			String nestedPropertyName = findPropertyName(nestedValue, suffix);
			return nestedPropertyName == null ? null : nestedName + "." + nestedPropertyName;
		}

		String name = null;
		int distance = 0;
		index = key.indexOf(PropertyAccessor.PROPERTY_KEY_PREFIX_CHAR);

		if (index > 0) {
			prefix = key.substring(0, index);
			suffix = key.substring(index);
		} else {
			prefix = key;
			suffix = "";
		}

		while (name == null && distance <= distanceLimit) {
			String[] candidates = PropertyMatches.forProperty(prefix, cls, distance).getPossibleMatches();
			// If we find precisely one match, then use that one...
			if (candidates.length == 1) {
				String candidate = candidates[0];
				if (candidate.equals(prefix)) { // if it's the same don't
					// replace it...
					name = key;
				} else {
					name = candidate + suffix;
				}
			}
			distance++;
		}
		return name;
	}

	@SuppressWarnings("deprecation")
	private Object getPropertyValue(Object bean, String nestedName) {
		BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
		wrapper.setAutoGrowNestedPaths(true);

		Object nestedValue = wrapper.getPropertyValue(nestedName);
		if (nestedValue == null) {
			try {
				nestedValue = wrapper.getPropertyType(nestedName).newInstance();
				wrapper.setPropertyValue(nestedName, nestedValue);
			} catch (InstantiationException e) {
				ReflectionUtils.handleReflectionException(e);
			} catch (IllegalAccessException e) {
				ReflectionUtils.handleReflectionException(e);
			}
		}
		return nestedValue;
	}

	private void switchPropertyNames(Properties properties, String oldName, String newName) {
		String value = properties.getProperty(oldName);
		properties.remove(oldName);
		properties.setProperty(newName, value);
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	private static class DistanceHolder {
		private final Class<?> cls;

		private final int distance;

		public DistanceHolder(Class<?> cls, int distance) {
			this.cls = cls;
			this.distance = distance;

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((cls == null) ? 0 : cls.hashCode());
			result = prime * result + distance;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DistanceHolder other = (DistanceHolder) obj;
			if (cls == null) {
				if (other.cls != null)
					return false;
			} else if (!cls.equals(other.cls))
				return false;
			if (distance != other.distance)
				return false;
			return true;
		}
	}
}
